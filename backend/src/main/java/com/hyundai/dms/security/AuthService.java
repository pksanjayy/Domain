package com.hyundai.dms.security;

import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.user.entity.Menu;
import com.hyundai.dms.module.user.entity.Permission;
import com.hyundai.dms.module.user.entity.Role;
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

    @Transactional(noRollbackFor = {BadCredentialsException.class, 
            com.hyundai.dms.exception.InvalidCredentialsException.class,
            com.hyundai.dms.exception.AccountLockedException.class})
    public LoginResponse login(LoginRequest request) {
        String correlationId = MDC.get("correlationId");

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            long lockTimeRemaining = user.getLockTimeRemainingSeconds();
            log.warn("[{}] Locked account login attempt: {} ({}s remaining)", 
                    correlationId, user.getUsername(), lockTimeRemaining);
            throw new com.hyundai.dms.exception.AccountLockedException(
                    "Account is locked due to multiple failed login attempts", 
                    lockTimeRemaining);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Reset failed attempts on successful login
            user.resetFailedAttempts();
            userRepository.save(user);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Collect all role names for JWT
            List<String> roleNames = user.getRoles().stream()
                    .map(r -> r.getName().name())
                    .collect(Collectors.toList());

            // Generate tokens
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", roleNames);
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
            
            int remainingAttempts = user.getRemainingAttempts();
            log.warn("[{}] Failed login attempt for user: {} (attempt #{}, {} remaining)",
                    correlationId, user.getUsername(), user.getFailedLoginAttempts(), remainingAttempts);
            
            if (user.isAccountLocked()) {
                long lockTimeRemaining = user.getLockTimeRemainingSeconds();
                throw new com.hyundai.dms.exception.AccountLockedException(
                        "Account locked due to multiple failed login attempts", 
                        lockTimeRemaining);
            }
            
            throw new com.hyundai.dms.exception.InvalidCredentialsException(
                    "Invalid username or password", 
                    remainingAttempts);
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

        // Collect all role names for JWT
        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        // Generate new tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roleNames);
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
        // Re-fetch from DB to ensure an active Hibernate session for lazy collections
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", userDetails.getId()));
        return buildUserProfile(user);
    }

    private UserProfileDto buildUserProfile(User user) {
        // Collect roles list
        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        // Merge permissions from all roles (deduplicated by module)
        Map<String, PermissionDto> permissionMap = new LinkedHashMap<>();
        for (Role role : user.getRoles()) {
            for (Permission p : role.getPermissions()) {
                permissionMap.merge(p.getModuleName(), toPermissionDto(p), (existing, incoming) ->
                    PermissionDto.builder()
                        .id(existing.getId())
                        .moduleName(existing.getModuleName())
                        .canCreate(existing.isCanCreate() || incoming.isCanCreate())
                        .canRead(existing.isCanRead() || incoming.isCanRead())
                        .canUpdate(existing.isCanUpdate() || incoming.isCanUpdate())
                        .canDelete(existing.isCanDelete() || incoming.isCanDelete())
                        .build()
                );
            }
        }

        // Merge menus from all roles (deduplicated by menu id)
        Set<Menu> allMenus = new HashSet<>();
        for (Role role : user.getRoles()) {
            allMenus.addAll(role.getMenus());
        }
        List<MenuDto> menus = buildMenuTree(allMenus);

        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roleNames)
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .forcePasswordChange(user.getForcePasswordChange())
                .menus(menus)
                .permissions(new ArrayList<>(permissionMap.values()))
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
