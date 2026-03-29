package com.hyundai.dms.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hyundai.dms.common.filter.SortCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private List<SortCriteria> appliedSorts;

    // Backward compat aliases
    public boolean isLast() {
        return !hasNext;
    }

    public boolean isFirst() {
        return !hasPrevious;
    }
}
