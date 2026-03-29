package com.hyundai.dms.common.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilterCriteriaTest {

    // ── Valid operators ──

    @Test
    @DisplayName("Create with 'eq' operator succeeds")
    void eqOperator() {
        FilterCriteria fc = new FilterCriteria("status", "eq", "AVAILABLE");
        assertEquals("status", fc.field());
        assertEquals("eq", fc.operator());
        assertEquals("AVAILABLE", fc.value());
    }

    @Test
    @DisplayName("Create with 'neq' operator succeeds")
    void neqOperator() {
        assertDoesNotThrow(() -> new FilterCriteria("status", "neq", "INVOICED"));
    }

    @Test
    @DisplayName("Create with 'like' operator succeeds")
    void likeOperator() {
        assertDoesNotThrow(() -> new FilterCriteria("brand", "like", "Hyu"));
    }

    @Test
    @DisplayName("Create with 'gt' operator succeeds")
    void gtOperator() {
        assertDoesNotThrow(() -> new FilterCriteria("ageDays", "gt", "30"));
    }

    @Test
    @DisplayName("Create with 'gte' operator succeeds")
    void gteOperator() {
        assertDoesNotThrow(() -> new FilterCriteria("msrp", "gte", "1000000"));
    }

    @Test
    @DisplayName("Create with 'lt' operator succeeds")
    void ltOperator() {
        assertDoesNotThrow(() -> new FilterCriteria("ageDays", "lt", "60"));
    }

    @Test
    @DisplayName("Create with 'lte' operator succeeds")
    void lteOperator() {
        assertDoesNotThrow(() -> new FilterCriteria("msrp", "lte", "2000000"));
    }

    @Test
    @DisplayName("Create with 'in' operator succeeds")
    void inOperator() {
        assertDoesNotThrow(() -> new FilterCriteria("status", "in", "AVAILABLE,HOLD,BOOKED"));
    }

    @Test
    @DisplayName("Create with 'between' operator succeeds")
    void betweenOperator() {
        assertDoesNotThrow(() -> new FilterCriteria("msrp", "between", "500000,1500000"));
    }

    @Test
    @DisplayName("Create with 'isNull' operator succeeds")
    void isNullOperator() {
        assertDoesNotThrow(() -> new FilterCriteria("branch", "isNull", null));
    }

    @Test
    @DisplayName("Create with 'isNotNull' operator succeeds")
    void isNotNullOperator() {
        assertDoesNotThrow(() -> new FilterCriteria("branch", "isNotNull", null));
    }

    // ── Field validation ──

    @Test
    @DisplayName("Null field throws IllegalArgumentException")
    void nullField() {
        assertThrows(IllegalArgumentException.class,
                () -> new FilterCriteria(null, "eq", "test"));
    }

    @Test
    @DisplayName("Blank field throws IllegalArgumentException")
    void blankField() {
        assertThrows(IllegalArgumentException.class,
                () -> new FilterCriteria("  ", "eq", "test"));
    }

    @Test
    @DisplayName("Empty field throws IllegalArgumentException")
    void emptyField() {
        assertThrows(IllegalArgumentException.class,
                () -> new FilterCriteria("", "eq", "test"));
    }

    // ── Operator validation ──

    @Test
    @DisplayName("Null operator throws IllegalArgumentException")
    void nullOperator() {
        assertThrows(IllegalArgumentException.class,
                () -> new FilterCriteria("status", null, "test"));
    }

    @Test
    @DisplayName("Unsupported operator 'regex' throws IllegalArgumentException")
    void unsupportedOperator_regex() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new FilterCriteria("brand", "regex", ".*"));
        assertTrue(ex.getMessage().contains("Unsupported filter operator"));
    }

    @Test
    @DisplayName("Unsupported operator 'contains' throws IllegalArgumentException")
    void unsupportedOperator_contains() {
        assertThrows(IllegalArgumentException.class,
                () -> new FilterCriteria("brand", "contains", "test"));
    }

    @Test
    @DisplayName("Empty operator throws IllegalArgumentException")
    void emptyOperator() {
        assertThrows(IllegalArgumentException.class,
                () -> new FilterCriteria("field", "", "test"));
    }

    // ── Case sensitivity ──

    @Test
    @DisplayName("Uppercase operator 'EQ' is accepted (case-insensitive)")
    void uppercaseOperator() {
        // operator validation uses toLowerCase()
        assertDoesNotThrow(() -> new FilterCriteria("field", "EQ", "value"));
    }

    @Test
    @DisplayName("Mixed case operator 'Like' is accepted")
    void mixedCaseOperator() {
        assertDoesNotThrow(() -> new FilterCriteria("field", "Like", "value"));
    }
}
