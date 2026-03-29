package com.hyundai.dms.module.user.service;

import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.user.dto.RoleDto;
import com.hyundai.dms.module.user.entity.Menu;
import com.hyundai.dms.module.user.entity.Permission;
import com.hyundai.dms.module.user.entity.Role;
import com.hyundai.dms.module.user.repository.MenuRepository;
import com.hyundai.dms.module.user.repository.PermissionRepository;
import com.hyundai.dms.module.user.repository.RoleRepository;
import com.hyundai.dms.security.dto.MenuDto;
import com.hyundai.dms.security.dto.PermissionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAllWithPermissionsAndMenus();
        return roles.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public RoleDto updatePermissions(Long roleId, List<PermissionDto> permissionDtos) {
        String correlationId = MDC.get("correlationId");

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        // Explicitly delete all existing permissions by role ID
        // and flush so the DELETE hits the DB before the INSERTs.
        // This avoids a unique constraint violation on (role_id, module_name)
        // caused by Hibernate batching INSERTs before orphanRemoval DELETEs.
        permissionRepository.deleteByRoleId(roleId);
        permissionRepository.flush();
        role.getPermissions().clear();

        // Add new permissions
        for (PermissionDto dto : permissionDtos) {
            Permission permission = Permission.builder()
                    .role(role)
                    .moduleName(dto.getModuleName())
                    .canCreate(Boolean.TRUE.equals(dto.isCanCreate()))
                    .canRead(Boolean.TRUE.equals(dto.isCanRead()))
                    .canUpdate(Boolean.TRUE.equals(dto.isCanUpdate()))
                    .canDelete(Boolean.TRUE.equals(dto.isCanDelete()))
                    .build();
            role.getPermissions().add(permission);
        }

        role = roleRepository.save(role);
        log.info("[{}] Updated permissions for role: {}", correlationId, role.getName());
        return toDto(role);
    }


    @Transactional
    public RoleDto updateMenus(Long roleId, List<Long> menuIds) {
        String correlationId = MDC.get("correlationId");

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        Set<Menu> menus = new HashSet<>(menuRepository.findAllById(menuIds));
        role.setMenus(menus);

        role = roleRepository.save(role);
        log.info("[{}] Updated menus for role: {}", correlationId, role.getName());
        return toDto(role);
    }

    private RoleDto toDto(Role role) {
        // Flatten permissions into {id, module, action} pairs matching frontend PermissionEntry
        List<Map<String, Object>> flatPermissions = new ArrayList<>();
        for (Permission p : role.getPermissions()) {
            if (Boolean.TRUE.equals(p.getCanCreate())) {
                flatPermissions.add(Map.of("id", p.getId(), "module", p.getModuleName(), "action", "CREATE"));
            }
            if (Boolean.TRUE.equals(p.getCanRead())) {
                flatPermissions.add(Map.of("id", p.getId(), "module", p.getModuleName(), "action", "READ"));
            }
            if (Boolean.TRUE.equals(p.getCanUpdate())) {
                flatPermissions.add(Map.of("id", p.getId(), "module", p.getModuleName(), "action", "UPDATE"));
            }
            if (Boolean.TRUE.equals(p.getCanDelete())) {
                flatPermissions.add(Map.of("id", p.getId(), "module", p.getModuleName(), "action", "DELETE"));
            }
        }

        List<MenuDto> menus = role.getMenus().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .map(m -> MenuDto.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .path(m.getPath())
                        .icon(m.getIcon())
                        .displayOrder(m.getDisplayOrder())
                        .parentId(m.getParent() != null ? m.getParent().getId() : null)
                        .build())
                .sorted(Comparator.comparingInt(MenuDto::getDisplayOrder))
                .collect(Collectors.toList());

        List<Long> menuIds = role.getMenus().stream()
                .map(Menu::getId)
                .collect(Collectors.toList());

        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName().name())
                .displayName(role.getName().name().replace("_", " "))
                .description(role.getDescription())
                .permissions(flatPermissions)
                .menus(menus)
                .menuIds(menuIds)
                .build();
    }
}
