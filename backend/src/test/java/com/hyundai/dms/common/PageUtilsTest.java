package com.hyundai.dms.common;

import com.hyundai.dms.common.filter.SortCriteria;
import com.hyundai.dms.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PageUtilsTest {

    // ── toPageResponse ──

    @Test
    @DisplayName("toPageResponse converts Page to PageResponse correctly")
    void toPageResponse_basic() {
        Page<String> page = new PageImpl<>(
                List.of("A", "B", "C"),
                PageRequest.of(0, 10, Sort.by("name")),
                30
        );
        PageResponse<String> response = PageUtils.toPageResponse(page);

        assertEquals(3, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(30, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    @DisplayName("toPageResponse captures applied sorts")
    void toPageResponse_withSorts() {
        Page<String> page = new PageImpl<>(
                List.of("A"),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                1
        );
        PageResponse<String> response = PageUtils.toPageResponse(page);

        assertEquals(1, response.getAppliedSorts().size());
        assertEquals("createdAt", response.getAppliedSorts().get(0).field());
        assertEquals("desc", response.getAppliedSorts().get(0).direction());
    }

    @Test
    @DisplayName("toPageResponse with empty page")
    void toPageResponse_empty() {
        Page<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        PageResponse<String> response = PageUtils.toPageResponse(page);

        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    @DisplayName("toPageResponse middle page has both hasNext and hasPrevious")
    void toPageResponse_middlePage() {
        Page<String> page = new PageImpl<>(
                List.of("B"),
                PageRequest.of(1, 1),
                3
        );
        PageResponse<String> response = PageUtils.toPageResponse(page);

        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }

    // ── buildPageRequest ──

    @Test
    @DisplayName("buildPageRequest with valid page and size")
    void buildPageRequest_basic() {
        PageRequest pr = PageUtils.buildPageRequest(0, 20, null);
        assertEquals(0, pr.getPageNumber());
        assertEquals(20, pr.getPageSize());
    }

    @Test
    @DisplayName("buildPageRequest negative page defaults to 0")
    void buildPageRequest_negativePage() {
        PageRequest pr = PageUtils.buildPageRequest(-5, 20, null);
        assertEquals(0, pr.getPageNumber());
    }

    @Test
    @DisplayName("buildPageRequest size 0 defaults to 20")
    void buildPageRequest_zeroSize() {
        PageRequest pr = PageUtils.buildPageRequest(0, 0, null);
        assertEquals(20, pr.getPageSize());
    }

    @Test
    @DisplayName("buildPageRequest size > 100 defaults to 20")
    void buildPageRequest_overMaxSize() {
        PageRequest pr = PageUtils.buildPageRequest(0, 200, null);
        assertEquals(20, pr.getPageSize());
    }

    @Test
    @DisplayName("buildPageRequest negative size defaults to 20")
    void buildPageRequest_negativeSize() {
        PageRequest pr = PageUtils.buildPageRequest(0, -10, null);
        assertEquals(20, pr.getPageSize());
    }

    @Test
    @DisplayName("buildPageRequest with sort criteria")
    void buildPageRequest_withSort() {
        List<SortCriteria> sorts = List.of(
                new SortCriteria("brand", "asc"),
                new SortCriteria("msrp", "desc")
        );
        PageRequest pr = PageUtils.buildPageRequest(0, 10, sorts);

        Sort sort = pr.getSort();
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("brand").getDirection());
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("msrp").getDirection());
    }

    @Test
    @DisplayName("buildPageRequest with empty sort list returns unsorted")
    void buildPageRequest_emptySort() {
        PageRequest pr = PageUtils.buildPageRequest(0, 10, List.of());
        assertTrue(pr.getSort().isUnsorted());
    }

    @Test
    @DisplayName("buildPageRequest rejects invalid sort field per allowlist")
    void buildPageRequest_invalidSortField() {
        List<SortCriteria> sorts = List.of(new SortCriteria("hackedField", "asc"));
        Set<String> allowed = Set.of("brand", "model", "status");

        assertThrows(ValidationException.class,
                () -> PageUtils.buildPageRequest(0, 10, sorts, allowed));
    }

    @Test
    @DisplayName("buildPageRequest accepts valid sort field per allowlist")
    void buildPageRequest_validSortField() {
        List<SortCriteria> sorts = List.of(new SortCriteria("brand", "asc"));
        Set<String> allowed = Set.of("brand", "model", "status");

        PageRequest pr = PageUtils.buildPageRequest(0, 10, sorts, allowed);
        assertNotNull(pr.getSort().getOrderFor("brand"));
    }

    @Test
    @DisplayName("buildPageRequest with null allowlist allows all fields")
    void buildPageRequest_nullAllowlist() {
        List<SortCriteria> sorts = List.of(new SortCriteria("anyField", "desc"));
        PageRequest pr = PageUtils.buildPageRequest(0, 10, sorts, null);
        assertNotNull(pr.getSort().getOrderFor("anyField"));
    }

    @Test
    @DisplayName("buildPageRequest size exactly 100 is valid")
    void buildPageRequest_maxValidSize() {
        PageRequest pr = PageUtils.buildPageRequest(0, 100, null);
        assertEquals(100, pr.getPageSize());
    }

    @Test
    @DisplayName("buildPageRequest size exactly 1 is valid")
    void buildPageRequest_minValidSize() {
        PageRequest pr = PageUtils.buildPageRequest(0, 1, null);
        assertEquals(1, pr.getPageSize());
    }
}
