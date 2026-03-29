package com.hyundai.dms.security;

import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.user.entity.Menu;
import com.hyundai.dms.module.user.entity.Permission;
import com.hyundai.dms.module.user.entity.User;
import com.hyundai.dms.module.user.repository.UserRepository;
import com.hyundai.dms.security.dto.*;
import com.hyundai.dms.security.entity.RefreshToken;
import com.hyundai.dms.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String correlationId = MDC.get("correlationId");

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Reset failed attempts on successful login
            user.resetFailedAttempts();
            userRepository.save(user);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Generate tokens
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole().getName().name());
            claims.put("userId", user.getId());

            String accessToken = jwtUtil.generateAccessToken(userDetails, claims);
            String refreshTokenValue = jwtUtil.generateRefreshToken(user.getUsername());

            // Persist refresh token
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenValue)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpiryMs() / 1000))
                    .revoked(false)
                    .build();
            refreshTokenRepository.save(refreshToken);

            log.info("[{}] User logged in successfully: {}", correlationId, user.getUsername());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenValue)
                    .user(buildUserProfile(user))
                    .build();

        } catch (BadCredentialsException e) {
            // Increment failed attempts
            user.incrementFailedAttempts();
            userRepository.save(user);
            log.warn("[{}] Failed login attempt for user: {} (attempt #{})",
                    correlationId, user.getUsername(), user.getFailedLoginAttempts());
            throw e;
        }
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        String correlationId = MDC.get("correlationId");

        RefreshToken existingToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token"));

        if (!existingToken.isUsable()) {
            throw new BusinessRuleException("Refresh token is expired or revoked");
        }

        User user = existingToken.getUser();

        // Revoke old refresh token (rotation)
        existingToken.setRevoked(true);
        refreshTokenRepository.save(existingToken);

        // Generate new tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getName().name());
        claims.put("userId", user.getId());

        String newAccessToken = jwtUtil.generateAccessToken(userDetails, claims);
        String newRefreshTokenValue = jwtUtil.generateRefreshToken(user.getUsername());

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpiryMs() / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        log.info("[{}] Token refreshed for user: {}", correlationId, user.getUsername());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .user(buildUserProfile(user))
                .build();
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.revokeByToken(request.getRefreshToken());
        log.info("[{}] User logged out", MDC.get("correlationId"));
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BusinessRuleException("User not authenticated or session expired");
        }
        User user = userDetails.getUser();
        return buildUserProfile(user);
    }

    private UserProfileDto buildUserProfile(User user) {
        List<PermissionDto> permissions = user.getRole().getPermissions().stream()
                .map(this::toPermissionDto)
                .collect(Collectors.toList());

        List<MenuDto> menus = buildMenuTree(user.getRole().getMenus());

        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().getName().name())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .forcePasswordChange(user.getForcePasswordChange())
                .menus(menus)
                .permissions(permissions)
                .build();
    }

    private List<MenuDto> buildMenuTree(Set<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, MenuDto> menuMap = new LinkedHashMap<>();
        List<MenuDto> rootMenus = new ArrayList<>();

        // First pass: convert all menus to DTOs
        for (Menu menu : menus) {
            if (Boolean.TRUE.equals(menu.getIsActive())) {
                MenuDto dto = MenuDto.builder()
                        .id(menu.getId())
                        .name(menu.getName())
                        .path(menu.getPath())
                        .icon(menu.getIcon())
                        .displayOrder(menu.getDisplayOrder())
                        .parentId(menu.getParent() != null ? menu.getParent().getId() : null)
                        .children(new ArrayList<>())
                        .build();
                menuMap.put(menu.getId(), dto);
            }
        }

        // Second pass: build hierarchy
        for (Menu menu : menus) {
            if (!Boolean.TRUE.equals(menu.getIsActive())) continue;
            MenuDto dto = menuMap.get(menu.getId());
            if (menu.getParent() == null || !menuMap.containsKey(menu.getParent().getId())) {
                rootMenus.add(dto);
            } else {
                menuMap.get(menu.getParent().getId()).getChildren().add(dto);
            }
        }

        rootMenus.sort(Comparator.comparing(MenuDto::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)));
        return rootMenus;
    }

    private PermissionDto toPermissionDto(Permission p) {
        return PermissionDto.builder()
                .id(p.getId())
                .moduleName(p.getModuleName())
                .canCreate(p.getCanCreate())
                .canRead(p.getCanRead())
                .canUpdate(p.getCanUpdate())
                .canDelete(p.getCanDelete())
                .build();
    }
}
