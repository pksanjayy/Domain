package com.hyundai.dms.config;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.PageUtils;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.common.filter.QueryDslPredicateBuilder;
import com.hyundai.dms.common.logging.LogExecution;
import com.hyundai.dms.module.user.dto.UserDto;
import com.hyundai.dms.module.user.entity.User;
import com.hyundai.dms.module.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.querydsl.core.types.Predicate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Demo endpoints at /api/demo (SUPER_ADMIN only) that exercise:
 * - JPA Specification engine (filter-test)
 * - Multi-tier caching (cache-test)
 * - Custom validators (validation-test)
 */
@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
@Tag(name = "Demo", description = "Phase 2 demonstration endpoints")
public class DemoController {

    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    /**
     * GET /api/demo/filter-test
     * Exercises the Specification engine.
     * Query params: filter (JSON array), page, size, sort
     */
    @GetMapping("/filter-test")
    @Operation(summary = "Filter test", description = "Demonstrates JPA Specification engine with FilterRequest")
    @LogExecution
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> filterTest(FilterRequest filterRequest) {
        QueryDslPredicateBuilder<User> predicateBuilder = new QueryDslPredicateBuilder<>(User.class);
        Predicate predicate = predicateBuilder.build(filterRequest.filters());

        PageRequest pageRequest = PageUtils.buildPageRequest(
                filterRequest.page(), filterRequest.size(), filterRequest.sorts()
        );

        Page<User> page = userRepository.findAll(predicate, pageRequest);
        Page<UserDto> dtoPage = page.map(this::toDto);
        PageResponse<UserDto> response = PageUtils.toPageResponse(dtoPage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/demo/cache-test
     * First call hits DB, second returns from cache.
     * Sets X-Cache-Hit header.
     */
    @GetMapping("/cache-test")
    @Operation(summary = "Cache test", description = "Demonstrates multi-tier caching with X-Cache-Hit header")
    @LogExecution
    public ResponseEntity<ApiResponse<String>> cacheTest(HttpServletResponse response) {
        String cacheKey = "demo-cache-test";
        String cacheName = "codes";

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper cached = cache.get(cacheKey);
            if (cached != null) {
                response.setHeader("X-Cache-Hit", "true");
                return ResponseEntity.ok(ApiResponse.success((String) cached.get()));
            }
        }

        // Simulate DB call
        long userCount = userRepository.count();
        String result = "Total users in DB: " + userCount + " (fetched from database)";

        if (cache != null) {
            cache.put(cacheKey, result);
        }

        response.setHeader("X-Cache-Hit", "false");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * POST /api/demo/validation-test
     * Demonstrates field-level validation error response with all custom validators.
     */
    @PostMapping("/validation-test")
    @Operation(summary = "Validation test", description = "Demonstrates custom validators — send invalid body to see errors")
    @LogExecution
    public ResponseEntity<ApiResponse<String>> validationTest(@Valid @RequestBody DemoValidationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Validation passed for: " + request.getName()));
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
