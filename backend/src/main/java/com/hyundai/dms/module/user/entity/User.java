package com.hyundai.dms.module.user.entity;

import com.hyundai.dms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_email", columnList = "email")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "force_password_change", nullable = false)
    @Builder.Default
    private Boolean forcePasswordChange = false;

    public boolean isAccountLocked() {
        if (lockedAt == null) {
            return false;
        }
        return lockedAt.plusMinutes(2).isAfter(LocalDateTime.now());
    }

    public long getLockTimeRemainingSeconds() {
        if (lockedAt == null) {
            return 0;
        }
        LocalDateTime unlockTime = lockedAt.plusMinutes(2);
        long secondsRemaining = java.time.Duration.between(LocalDateTime.now(), unlockTime).getSeconds();
        return Math.max(0, secondsRemaining);
    }

    public int getRemainingAttempts() {
        return Math.max(0, 5 - this.failedLoginAttempts);
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.lockedAt = LocalDateTime.now();
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedAt = null;
    }
}
