package com.hyundai.dms.common;

import com.hyundai.dms.exception.ValidationException;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SortValidatorTest {

    // ── Valid fields ──

    @Test
    @DisplayName("Validate existing field on Vehicle entity")
    void validate_existingField() {
        assertDoesNotThrow(() -> SortValidator.validate(Vehicle.class, "vin"));
    }

    @Test
    @DisplayName("Validate another existing field")
    void validate_brandField() {
        assertDoesNotThrow(() -> SortValidator.validate(Vehicle.class, "brand"));
    }

    @Test
    @DisplayName("Validate inherited field from BaseEntity (id)")
    void validate_inheritedField() {
        assertDoesNotThrow(() -> SortValidator.validate(Vehicle.class, "id"));
    }

    @Test
    @DisplayName("Validate inherited field from BaseEntity (createdAt)")
    void validate_createdAtField() {
        assertDoesNotThrow(() -> SortValidator.validate(Vehicle.class, "createdAt"));
    }

    @Test
    @DisplayName("Validate dot-notation field checks root only")
    void validate_dotNotation() {
        assertDoesNotThrow(() -> SortValidator.validate(Vehicle.class, "branch.name"));
    }

    // ── Invalid fields ──

    @Test
    @DisplayName("Validate non-existent field throws ValidationException")
    void validate_nonExistentField() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> SortValidator.validate(Vehicle.class, "hackedColumn"));
        assertTrue(ex.getMessage().contains("Invalid sort field"));
    }

    @Test
    @DisplayName("Null field throws ValidationException")
    void validate_nullField() {
        assertThrows(ValidationException.class,
                () -> SortValidator.validate(Vehicle.class, null));
    }

    @Test
    @DisplayName("Blank field throws ValidationException")
    void validate_blankField() {
        assertThrows(ValidationException.class,
                () -> SortValidator.validate(Vehicle.class, "  "));
    }

    @Test
    @DisplayName("Empty field throws ValidationException")
    void validate_emptyField() {
        assertThrows(ValidationException.class,
                () -> SortValidator.validate(Vehicle.class, ""));
    }

    // ── getAllFieldNames ──

    @Test
    @DisplayName("getAllFieldNames returns declared + inherited fields")
    void getAllFieldNames_includesBoth() {
        Set<String> fields = SortValidator.getAllFieldNames(Vehicle.class);

        // Vehicle's own fields
        assertTrue(fields.contains("vin"));
        assertTrue(fields.contains("brand"));
        assertTrue(fields.contains("model"));
        assertTrue(fields.contains("status"));
        assertTrue(fields.contains("msrp"));

        // BaseEntity's fields
        assertTrue(fields.contains("id"));
        assertTrue(fields.contains("createdAt"));
    }

    @Test
    @DisplayName("getAllFieldNames does not include non-entity fields")
    void getAllFieldNames_excludesNonExistent() {
        Set<String> fields = SortValidator.getAllFieldNames(Vehicle.class);
        assertFalse(fields.contains("notAField"));
    }
}
