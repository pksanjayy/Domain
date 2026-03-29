package com.hyundai.dms.common.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SortCriteriaTest {

    // ── Valid cases ──

    @Test
    @DisplayName("Create with 'asc' direction")
    void ascDirection() {
        SortCriteria sc = new SortCriteria("brand", "asc");
        assertEquals("brand", sc.field());
        assertEquals("asc", sc.direction());
    }

    @Test
    @DisplayName("Create with 'desc' direction")
    void descDirection() {
        SortCriteria sc = new SortCriteria("msrp", "desc");
        assertEquals("desc", sc.direction());
    }

    @Test
    @DisplayName("Uppercase 'ASC' is normalized to lowercase")
    void uppercaseAsc() {
        SortCriteria sc = new SortCriteria("field", "ASC");
        assertEquals("asc", sc.direction());
    }

    @Test
    @DisplayName("Uppercase 'DESC' is normalized to lowercase")
    void uppercaseDesc() {
        SortCriteria sc = new SortCriteria("field", "DESC");
        assertEquals("desc", sc.direction());
    }

    @Test
    @DisplayName("Mixed case 'Desc' is normalized")
    void mixedCaseDesc() {
        SortCriteria sc = new SortCriteria("field", "Desc");
        assertEquals("desc", sc.direction());
    }

    @Test
    @DisplayName("Null direction defaults to 'asc'")
    void nullDirection() {
        SortCriteria sc = new SortCriteria("field", null);
        assertEquals("asc", sc.direction());
    }

    @Test
    @DisplayName("Blank direction defaults to 'asc'")
    void blankDirection() {
        SortCriteria sc = new SortCriteria("field", "  ");
        assertEquals("asc", sc.direction());
    }

    // ── Invalid cases ──

    @Test
    @DisplayName("Null field throws IllegalArgumentException")
    void nullField() {
        assertThrows(IllegalArgumentException.class,
                () -> new SortCriteria(null, "asc"));
    }

    @Test
    @DisplayName("Blank field throws IllegalArgumentException")
    void blankField() {
        assertThrows(IllegalArgumentException.class,
                () -> new SortCriteria("  ", "asc"));
    }

    @Test
    @DisplayName("Empty field throws IllegalArgumentException")
    void emptyField() {
        assertThrows(IllegalArgumentException.class,
                () -> new SortCriteria("", "asc"));
    }

    @Test
    @DisplayName("Invalid direction 'up' throws IllegalArgumentException")
    void invalidDirection_up() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new SortCriteria("field", "up"));
        assertTrue(ex.getMessage().contains("must be 'asc' or 'desc'"));
    }

    @Test
    @DisplayName("Invalid direction 'ascending' throws IllegalArgumentException")
    void invalidDirection_ascending() {
        assertThrows(IllegalArgumentException.class,
                () -> new SortCriteria("field", "ascending"));
    }
}
