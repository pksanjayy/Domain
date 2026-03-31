package com.hyundai.dms.module.inventory.filter;

import com.hyundai.dms.common.filter.FilterCriteria;
import com.hyundai.dms.common.filter.QueryDslPredicateBuilder;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryDslPredicateBuilderTest {

    private QueryDslPredicateBuilder<Object> builder;

    @BeforeEach
    void setUp() {
        builder = new QueryDslPredicateBuilder<>(Object.class);
    }

    @Test
    @DisplayName("Empty filters returns non-null Predicate")
    void emptyFiltersReturnsPredicate() {
        Predicate predicate = builder.build(List.of());
        assertNotNull(predicate);
    }

    @Test
    @DisplayName("Null filters returns non-null Predicate")
    void nullFiltersReturnsPredicate() {
        Predicate predicate = builder.build(null);
        assertNotNull(predicate);
    }

    @Test
    @DisplayName("Single eq filter builds successfully")
    void singleEqFilter() {
        FilterCriteria criteria = new FilterCriteria("status", "eq", "AVAILABLE");
        Predicate predicate = builder.build(List.of(criteria));
        assertNotNull(predicate);
    }

    @Test
    @DisplayName("Like filter builds successfully")
    void likeFilter() {
        FilterCriteria criteria = new FilterCriteria("brand", "like", "Hyundai");
        Predicate predicate = builder.build(List.of(criteria));
        assertNotNull(predicate);
    }

    @Test
    @DisplayName("Between filter with proper values builds successfully")
    void betweenFilter() {
        FilterCriteria criteria = new FilterCriteria("msrp", "between", "500000,1500000");
        Predicate predicate = builder.build(List.of(criteria));
        assertNotNull(predicate);
    }

    @Test
    @DisplayName("In filter with comma-separated values builds successfully")
    void inFilter() {
        FilterCriteria criteria = new FilterCriteria("status", "in", "AVAILABLE,HOLD,BOOKED");
        Predicate predicate = builder.build(List.of(criteria));
        assertNotNull(predicate);
    }

    @Test
    @DisplayName("Multiple filters build compound Predicate")
    void multipleFilters() {
        List<FilterCriteria> criteria = List.of(
                new FilterCriteria("brand", "eq", "Hyundai"),
                new FilterCriteria("status", "eq", "AVAILABLE"),
                new FilterCriteria("ageDays", "gte", "30")
        );
        Predicate predicate = builder.build(criteria);
        assertNotNull(predicate);
    }

    @Test
    @DisplayName("Unsupported operator throws IllegalArgumentException")
    void unsupportedOperator() {
        assertThrows(IllegalArgumentException.class, () ->
                new FilterCriteria("brand", "regex", ".*"));
    }
}
