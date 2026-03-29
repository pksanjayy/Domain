package com.hyundai.dms.common;

import com.hyundai.dms.common.filter.SortCriteria;
import com.hyundai.dms.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for pagination operations.
 */
public final class PageUtils {

    private PageUtils() {
        // Utility class — do not instantiate
    }

    /**
     * Converts a Spring Data {@link Page} into a {@link PageResponse}.
     */
    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        List<SortCriteria> appliedSorts = page.getSort().stream()
                .map(order -> new SortCriteria(order.getProperty(), order.getDirection().name().toLowerCase()))
                .collect(Collectors.toList());

        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .appliedSorts(appliedSorts)
                .build();
    }

    /**
     * Builds a {@link PageRequest} from pagination parameters.
     * Validates sort field names against an allowlist to prevent injection.
     *
     * @param page       zero-based page number
     * @param size       page size (capped at 100)
     * @param sorts      list of sort criteria
     * @param allowedFields set of allowed field names for sorting (null = allow all)
     * @return a validated PageRequest
     */
    public static PageRequest buildPageRequest(int page, int size, List<SortCriteria> sorts, Set<String> allowedFields) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;

        if (sorts == null || sorts.isEmpty()) {
            return PageRequest.of(page, size);
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (SortCriteria sc : sorts) {
            String field = sc.field();

            // Validate field against allowlist to prevent SQL injection through sort fields
            if (allowedFields != null && !allowedFields.contains(field)) {
                throw new ValidationException(
                        "Invalid sort field: '" + field + "'. Allowed fields: " + allowedFields
                );
            }

            Sort.Direction direction = "desc".equalsIgnoreCase(sc.direction())
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, field));
        }

        return PageRequest.of(page, size, Sort.by(orders));
    }

    /**
     * Convenience overload without allowlist validation.
     */
    public static PageRequest buildPageRequest(int page, int size, List<SortCriteria> sorts) {
        return buildPageRequest(page, size, sorts, null);
    }
}
