package com.hyundai.dms.module.user.service;

import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.PageUtils;
import com.hyundai.dms.exception.DuplicateResourceException;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.user.dto.*;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.entity.Role;
import com.hyundai.dms.module.user.entity.User;
import com.hyundai.dms.module.user.repository.BranchRepository;
import com.hyundai.dms.module.user.repository.RoleRepository;
import com.hyundai.dms.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";

    @Transactional(readOnly = true)
    public PageResponse<UserDto> getAllUsers(String search, Pageable pageable) {
        Page<User> page = userRepository.findAllWithSearch(search, pageable);
        Page<UserDto> dtoPage = page.map(this::toDto);
        return PageUtils.toPageResponse(dtoPage);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toDto(user);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        String correlationId = MDC.get("correlationId");

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", request.getRoleId()));

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .branch(branch)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .failedLoginAttempts(0)
                .forcePasswordChange(false)
                .build();

        user = userRepository.save(user);
        log.info("[{}] Created user: {} with role: {}", correlationId, user.getUsername(), role.getName());
        return toDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        String correlationId = MDC.get("correlationId");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", request.getRoleId()));
        user.setRole(role);

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
            user.setBranch(branch);
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        user = userRepository.save(user);
        log.info("[{}] Updated user: {}", correlationId, user.getUsername());
        return toDto(user);
    }

    @Transactional
    public UserDto lockUnlockUser(Long id, boolean lock) {
        String correlationId = MDC.get("correlationId");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (lock) {
            user.setLockedAt(LocalDateTime.now());
            user.setFailedLoginAttempts(5);
            log.info("[{}] Manually locked user: {}", correlationId, user.getUsername());
        } else {
            user.resetFailedAttempts();
            log.info("[{}] Manually unlocked user: {}", correlationId, user.getUsername());
        }

        user = userRepository.save(user);
        return toDto(user);
    }

    @Transactional
    public String resetPassword(Long id) {
        String correlationId = MDC.get("correlationId");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        String tempPassword = generateTempPassword();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setForcePasswordChange(true);
        user.resetFailedAttempts();
        userRepository.save(user);

        log.info("[{}] Password reset for user: {}", correlationId, user.getUsername());
        return tempPassword;
    }

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleName(user.getRole().getName().name())
                .roleId(user.getRole().getId())
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .isActive(user.getIsActive())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lockedAt(user.getLockedAt())
                .forcePasswordChange(user.getForcePasswordChange())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
