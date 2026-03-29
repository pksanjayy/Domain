package com.hyundai.dms.common.filter;

import java.util.List;

/**
 * Encapsulates filter criteria, pagination, and sort parameters for list endpoints.
 * Can be resolved from query params via {@link FilterRequestArgumentResolver}
 * or accepted as a JSON request body.
 */
public record FilterRequest(
        List<FilterCriteria> filters,
        int page,
        int size,
        List<SortCriteria> sorts
) {
    public FilterRequest {
        if (filters == null) {
            filters = List.of();
        }
        if (sorts == null) {
            sorts = List.of();
        }
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 20;
        }
    }

    public static FilterRequest of(List<FilterCriteria> filters, int page, int size, List<SortCriteria> sorts) {
        return new FilterRequest(filters, page, size, sorts);
    }
}
