package com.hyundai.dms.module.inventory.filter;

import com.hyundai.dms.common.filter.FilterCriteria;
import com.hyundai.dms.common.filter.SpecificationBuilder;
import com.hyundai.dms.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpecificationBuilderTest {

    private SpecificationBuilder<Object> builder;

    @BeforeEach
    void setUp() {
        builder = new SpecificationBuilder<>();
    }

    @Test
    @DisplayName("Empty filters returns non-null Specification")
    void emptyFiltersReturnsSpec() {
        Specification<Object> spec = builder.build(List.of());
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Null filters returns non-null Specification")
    void nullFiltersReturnsSpec() {
        Specification<Object> spec = builder.build(null);
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Single eq filter builds successfully")
    void singleEqFilter() {
        FilterCriteria criteria = new FilterCriteria("status", "eq", "AVAILABLE");
        Specification<Object> spec = builder.build(List.of(criteria));
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Like filter builds successfully")
    void likeFilter() {
        FilterCriteria criteria = new FilterCriteria("brand", "like", "Hyundai");
        Specification<Object> spec = builder.build(List.of(criteria));
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Between filter with proper values builds successfully")
    void betweenFilter() {
        FilterCriteria criteria = new FilterCriteria("msrp", "between", "500000,1500000");
        Specification<Object> spec = builder.build(List.of(criteria));
        assertNotNull(spec);
    }

    @Test
    @DisplayName("In filter with comma-separated values builds successfully")
    void inFilter() {
        FilterCriteria criteria = new FilterCriteria("status", "in", "AVAILABLE,HOLD,BOOKED");
        Specification<Object> spec = builder.build(List.of(criteria));
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Multiple filters build compound Specification")
    void multipleFilters() {
        List<FilterCriteria> criteria = List.of(
                new FilterCriteria("brand", "eq", "Hyundai"),
                new FilterCriteria("status", "eq", "AVAILABLE"),
                new FilterCriteria("ageDays", "gte", "30")
        );
        Specification<Object> spec = builder.build(criteria);
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Unsupported operator throws IllegalArgumentException")
    void unsupportedOperator() {
        assertThrows(IllegalArgumentException.class, () ->
                new FilterCriteria("brand", "regex", ".*"));
    }
}
