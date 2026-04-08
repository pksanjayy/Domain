package com.hyundai.dms.security;

import com.hyundai.dms.module.user.entity.Permission;
import com.hyundai.dms.module.user.entity.Role;
import com.hyundai.dms.module.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final Collection<? extends GrantedAuthority> authorities;
    private final User user;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.enabled = user.getIsActive();
        this.accountNonLocked = !user.isAccountLocked();
        this.user = user;

        Set<GrantedAuthority> auths = new HashSet<>();

        for (Role role : user.getRoles()) {
            // Add ROLE_ authority for each assigned role
            auths.add(new SimpleGrantedAuthority("ROLE_" + role.getName().name()));

            // Add fine-grained permission authorities from each role
            if (role.getPermissions() != null) {
                for (Permission p : role.getPermissions()) {
                    if (Boolean.TRUE.equals(p.getCanCreate())) {
                        auths.add(new SimpleGrantedAuthority(p.getModuleName() + "_CREATE"));
                    }
                    if (Boolean.TRUE.equals(p.getCanRead())) {
                        auths.add(new SimpleGrantedAuthority(p.getModuleName() + "_READ"));
                    }
                    if (Boolean.TRUE.equals(p.getCanUpdate())) {
                        auths.add(new SimpleGrantedAuthority(p.getModuleName() + "_UPDATE"));
                    }
                    if (Boolean.TRUE.equals(p.getCanDelete())) {
                        auths.add(new SimpleGrantedAuthority(p.getModuleName() + "_DELETE"));
                    }
                }
            }
        }

        this.authorities = auths;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
