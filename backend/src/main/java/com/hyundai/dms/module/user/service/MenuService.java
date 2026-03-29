package com.hyundai.dms.module.user.service;

import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.user.dto.CreateMenuRequest;
import com.hyundai.dms.module.user.dto.UpdateMenuRequest;
import com.hyundai.dms.module.user.entity.Menu;
import com.hyundai.dms.module.user.repository.MenuRepository;
import com.hyundai.dms.security.dto.MenuDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<MenuDto> getMenuTree() {
        List<Menu> rootMenus = menuRepository.findAllRootMenus();
        return rootMenus.stream()
                .map(this::toTreeDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuDto createMenu(CreateMenuRequest request) {
        String correlationId = MDC.get("correlationId");

        Menu menu = Menu.builder()
                .name(request.getName())
                .path(request.getPath())
                .icon(request.getIcon())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .children(new ArrayList<>())
                .build();

        if (request.getParentId() != null) {
            Menu parent = menuRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu", request.getParentId()));
            menu.setParent(parent);
        }

        menu = menuRepository.save(menu);
        log.info("[{}] Created menu: {}", correlationId, menu.getName());
        return toFlatDto(menu);
    }

    @Transactional
    public MenuDto updateMenu(Long id, UpdateMenuRequest request) {
        String correlationId = MDC.get("correlationId");

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", id));

        if (request.getName() != null) menu.setName(request.getName());
        if (request.getPath() != null) menu.setPath(request.getPath());
        if (request.getIcon() != null) menu.setIcon(request.getIcon());
        if (request.getDisplayOrder() != null) menu.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) menu.setIsActive(request.getIsActive());

        if (request.getParentId() != null) {
            Menu parent = menuRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu", request.getParentId()));
            menu.setParent(parent);
        }

        menu = menuRepository.save(menu);
        log.info("[{}] Updated menu: {}", correlationId, menu.getName());
        return toFlatDto(menu);
    }

    @Transactional
    public void deleteMenu(Long id) {
        String correlationId = MDC.get("correlationId");

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", id));

        menuRepository.delete(menu);
        log.info("[{}] Deleted menu: {}", correlationId, menu.getName());
    }

    private MenuDto toTreeDto(Menu menu) {
        List<MenuDto> children = menu.getChildren() != null
                ? menu.getChildren().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                    .map(this::toTreeDto)
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return MenuDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .path(menu.getPath())
                .icon(menu.getIcon())
                .displayOrder(menu.getDisplayOrder())
                .parentId(menu.getParent() != null ? menu.getParent().getId() : null)
                .children(children)
                .build();
    }

    private MenuDto toFlatDto(Menu menu) {
        return MenuDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .path(menu.getPath())
                .icon(menu.getIcon())
                .displayOrder(menu.getDisplayOrder())
                .parentId(menu.getParent() != null ? menu.getParent().getId() : null)
                .build();
    }
}
