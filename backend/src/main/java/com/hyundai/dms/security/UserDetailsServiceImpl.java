package com.hyundai.dms.security;

import com.hyundai.dms.module.user.entity.User;
import com.hyundai.dms.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        if (!user.getIsActive()) {
            log.warn("Inactive user attempted login: {}", username);
            throw new UsernameNotFoundException("User account is deactivated: " + username);
        }

        if (user.isAccountLocked()) {
            log.warn("Locked user attempted login: {}", username);
            throw new org.springframework.security.authentication.LockedException(
                    "Account is locked due to too many failed attempts. Please try again later.");
        }

        return new CustomUserDetails(user);
    }
}
