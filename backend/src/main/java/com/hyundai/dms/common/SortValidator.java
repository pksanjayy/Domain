package com.hyundai.dms.common;

import com.hyundai.dms.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates sort field names against entity class fields using reflection.
 * Prevents injection of arbitrary field names into ORDER BY clauses.
 */
@Slf4j
public final class SortValidator {

    private SortValidator() {
        // Utility class — do not instantiate
    }

    /**
     * Checks whether the given field name exists as a declared field in the entity class
     * or any of its superclasses.
     *
     * @param entityClass the JPA entity class
     * @param fieldName   the field name to validate
     * @throws ValidationException if the field does not exist
     */
    public static void validate(Class<?> entityClass, String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            throw new ValidationException("Sort field name must not be blank");
        }

        // Support dot notation — only validate first-level field for join paths
        String rootField = fieldName.contains(".") ? fieldName.split("\\.")[0] : fieldName;

        Set<String> allFields = getAllFieldNames(entityClass);
        if (!allFields.contains(rootField)) {
            throw new ValidationException(
                    "Invalid sort field '" + rootField + "' for entity " + entityClass.getSimpleName()
                            + ". Valid fields: " + allFields
            );
        }
    }

    /**
     * Gets all declared field names from the entity class and all its superclasses.
     */
    public static Set<String> getAllFieldNames(Class<?> clazz) {
        Set<String> fields = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            for (Field f : superClass.getDeclaredFields()) {
                fields.add(f.getName());
            }
            superClass = superClass.getSuperclass();
        }

        return fields;
    }
}
