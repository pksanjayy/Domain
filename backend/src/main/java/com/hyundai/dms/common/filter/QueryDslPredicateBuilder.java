package com.hyundai.dms.common.filter;

import com.hyundai.dms.exception.ValidationException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Builds a QueryDSL {@link com.querydsl.core.types.Predicate} from a list of {@link FilterCriteria}.
 * <p>
 * This is the QueryDSL replacement for the JPA Specification-based {@code SpecificationBuilder}.
 * It supports:
 * - All 11 operators: eq, neq, like, gt, gte, lt, lte, in, between, isNull, isNotNull
 * - Nested joins via dot notation (e.g., "vehicle.model" → path on "vehicle"."model")
 * - Type coercion via Reflection to ensure Hibernate receives the correct data types (Enum, Long, etc.)
 *
 * @param <T> The root entity type
 */
public class QueryDslPredicateBuilder<T> {

    private final Class<T> entityType;

    public QueryDslPredicateBuilder(Class<T> entityType) {
        this.entityType = entityType;
    }

    /**
     * Builds a combined predicate from a list of filter criteria (AND logic).
     */
    public com.querydsl.core.types.Predicate build(List<FilterCriteria> filters) {
        BooleanBuilder builder = new BooleanBuilder();
        if (filters == null || filters.isEmpty()) {
            return builder;
        }

        for (FilterCriteria criteria : filters) {
            builder.and(toPredicate(criteria));
        }
        return builder;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private com.querydsl.core.types.Predicate toPredicate(FilterCriteria criteria) {
        PathBuilder<T> entityPath = new PathBuilder<>(entityType, resolveEntityVariable());
        String field = criteria.field();
        String operator = criteria.operator().toLowerCase();
        Object value = criteria.value();

        // Resolve the path (supports dot notation for nested fields)
        PathBuilder<?> path = resolvePath(entityPath, field);
        String leafField = getLeafField(field);

        return switch (operator) {
            case "eq", "equal" -> buildEqPredicate(path, field, leafField, value);
            case "neq", "not_equal" -> buildNeqPredicate(path, field, leafField, value);
            case "like" -> buildLikePredicate(path, leafField, value);
            case "gt", "greater_than" -> buildComparisonPredicate(path, field, leafField, value, "gt");
            case "gte", "greater_than_or_equal" -> buildComparisonPredicate(path, field, leafField, value, "gte");
            case "lt", "less_than" -> buildComparisonPredicate(path, field, leafField, value, "lt");
            case "lte", "less_than_or_equal" -> buildComparisonPredicate(path, field, leafField, value, "lte");
            case "in" -> buildInPredicate(path, field, leafField, value);
            case "between" -> buildBetweenPredicate(path, field, leafField, value);
            case "isnull" -> path.get(leafField).isNull();
            case "isnotnull", "not_null" -> path.get(leafField).isNotNull();
            default -> throw new ValidationException("Unsupported operator: " + operator);
        };
    }

    private PathBuilder<?> resolvePath(PathBuilder<T> root, String field) {
        if (!field.contains(".")) {
            return root;
        }
        String[] parts = field.split("\\.");
        PathBuilder<?> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            current = current.get(parts[i], Object.class);
        }
        return current;
    }

    private String getLeafField(String field) {
        if (!field.contains(".")) {
            return field;
        }
        String[] parts = field.split("\\.");
        return parts[parts.length - 1];
    }

    private String resolveEntityVariable() {
        String simpleName = entityType.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    // ── Predicate Builders ──

    private com.querydsl.core.types.Predicate buildEqPredicate(PathBuilder<?> path, String fullField, String leafField, Object value) {
        if (value == null) {
            return path.get(leafField).isNull();
        }
        Object coerced = coerceValue(fullField, value.toString());
        return path.get(leafField).eq(coerced);
    }

    private com.querydsl.core.types.Predicate buildNeqPredicate(PathBuilder<?> path, String fullField, String leafField, Object value) {
        if (value == null) {
            return path.get(leafField).isNotNull();
        }
        Object coerced = coerceValue(fullField, value.toString());
        return path.get(leafField).ne(coerced);
    }

    private com.querydsl.core.types.Predicate buildLikePredicate(PathBuilder<?> path, String leafField, Object value) {
        StringPath stringPath = path.getString(leafField);
        return stringPath.containsIgnoreCase(value.toString());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private com.querydsl.core.types.Predicate buildComparisonPredicate(
            PathBuilder<?> path, String fullField, String leafField, Object value, String op) {
        
        Object coerced = coerceValue(fullField, value.toString());

        if (coerced instanceof LocalDate date) {
            DatePath<LocalDate> datePath = path.getDate(leafField, LocalDate.class);
            return switch (op) {
                case "gt" -> datePath.gt(date);
                case "gte" -> datePath.goe(date);
                case "lt" -> datePath.lt(date);
                case "lte" -> datePath.loe(date);
                default -> throw new ValidationException("Invalid operator: " + op);
            };
        } else if (coerced instanceof LocalDateTime dateTime) {
            DateTimePath<LocalDateTime> dateTimePath = path.getDateTime(leafField, LocalDateTime.class);
            return switch (op) {
                case "gt" -> dateTimePath.gt(dateTime);
                case "gte" -> dateTimePath.goe(dateTime);
                case "lt" -> dateTimePath.lt(dateTime);
                case "lte" -> dateTimePath.loe(dateTime);
                default -> throw new ValidationException("Invalid operator: " + op);
            };
        } else if (coerced instanceof Long num) {
            NumberPath<Long> numberPath = path.getNumber(leafField, Long.class);
            return switch (op) {
                case "gt" -> numberPath.gt(num);
                case "gte" -> numberPath.goe(num);
                case "lt" -> numberPath.lt(num);
                case "lte" -> numberPath.loe(num);
                default -> throw new ValidationException("Invalid operator: " + op);
            };
        } else if (coerced instanceof BigDecimal num) {
            NumberPath<BigDecimal> numberPath = path.getNumber(leafField, BigDecimal.class);
            return switch (op) {
                case "gt" -> numberPath.gt(num);
                case "gte" -> numberPath.goe(num);
                case "lt" -> numberPath.lt(num);
                case "lte" -> numberPath.loe(num);
                default -> throw new ValidationException("Invalid operator: " + op);
            };
        } else if (coerced instanceof Integer num) {
            NumberPath<Integer> numberPath = path.getNumber(leafField, Integer.class);
            return switch (op) {
                case "gt" -> numberPath.gt(num);
                case "gte" -> numberPath.goe(num);
                case "lt" -> numberPath.lt(num);
                case "lte" -> numberPath.loe(num);
                default -> throw new ValidationException("Invalid operator: " + op);
            };
        } else {
            ComparablePath<String> comparablePath = path.getComparable(leafField, String.class);
            String strValue = coerced.toString();
            return switch (op) {
                case "gt" -> comparablePath.gt(strValue);
                case "gte" -> comparablePath.goe(strValue);
                case "lt" -> comparablePath.lt(strValue);
                case "lte" -> comparablePath.loe(strValue);
                default -> throw new ValidationException("Invalid operator: " + op);
            };
        }
    }

    private com.querydsl.core.types.Predicate buildInPredicate(PathBuilder<?> path, String fullField, String leafField, Object value) {
        Collection<?> values;
        if (value instanceof Collection<?> col) {
            values = col;
        } else if (value instanceof String str) {
            values = Arrays.asList(str.split(","));
        } else {
            values = List.of(value);
        }

        List<Object> coercedValues = values.stream()
                .map(v -> coerceValue(fullField, v.toString()))
                .toList();

        return path.get(leafField).in(coercedValues);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private com.querydsl.core.types.Predicate buildBetweenPredicate(PathBuilder<?> path, String fullField, String leafField, Object value) {
        String[] parts;
        if (value instanceof String str) {
            parts = str.split(",");
        } else if (value instanceof Collection<?> col) {
            parts = col.stream().map(Object::toString).toArray(String[]::new);
        } else {
            throw new ValidationException("Between operator requires two comma-separated values");
        }

        if (parts.length != 2) {
            throw new ValidationException("Between operator requires exactly two values, got: " + parts.length);
        }

        Object coercedLower = coerceValue(fullField, parts[0]);
        Object coercedUpper = coerceValue(fullField, parts[1]);

        if (coercedLower instanceof LocalDate) {
            DatePath<LocalDate> datePath = path.getDate(leafField, LocalDate.class);
            return datePath.between((LocalDate) coercedLower, (LocalDate) coercedUpper);
        } else if (coercedLower instanceof LocalDateTime) {
            DateTimePath<LocalDateTime> dateTimePath = path.getDateTime(leafField, LocalDateTime.class);
            return dateTimePath.between((LocalDateTime) coercedLower, (LocalDateTime) coercedUpper);
        } else if (coercedLower instanceof Long) {
            NumberPath<Long> numberPath = path.getNumber(leafField, Long.class);
            return numberPath.between((Long) coercedLower, (Long) coercedUpper);
        } else if (coercedLower instanceof BigDecimal) {
            NumberPath<BigDecimal> numberPath = path.getNumber(leafField, BigDecimal.class);
            return numberPath.between((BigDecimal) coercedLower, (BigDecimal) coercedUpper);
        } else if (coercedLower instanceof Integer) {
            NumberPath<Integer> numberPath = path.getNumber(leafField, Integer.class);
            return numberPath.between((Integer) coercedLower, (Integer) coercedUpper);
        } else {
            ComparablePath<String> comparablePath = path.getComparable(leafField, String.class);
            return comparablePath.between(coercedLower.toString(), coercedUpper.toString());
        }
    }

    // ── Type Coercion ──

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object coerceValue(String fieldPath, String value) {
        if (value == null) return null;
        String str = value.trim();

        try {
            Class<?> fieldType = getFieldType(entityType, fieldPath);
            if (fieldType != null) {
                if (fieldType.isEnum()) {
                    return Enum.valueOf((Class<Enum>) fieldType, str);
                } else if (fieldType == Long.class || fieldType == long.class) {
                    return Long.valueOf(str);
                } else if (fieldType == Integer.class || fieldType == int.class) {
                    return Integer.valueOf(str);
                } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                    return Boolean.valueOf(str);
                } else if (fieldType == BigDecimal.class) {
                    return new BigDecimal(str);
                } else if (fieldType == LocalDate.class) {
                    return LocalDate.parse(str);
                } else if (fieldType == LocalDateTime.class) {
                    return LocalDateTime.parse(str);
                }
            }
        } catch (Exception e) {
            // Log warning or ignore -> fallback to heuristic coercion below
        }

        // Fallback: heuristic
        try { return LocalDate.parse(str); } catch (DateTimeParseException ignored) {}
        try { return LocalDateTime.parse(str); } catch (DateTimeParseException ignored) {}
        try { return Long.valueOf(str); } catch (NumberFormatException ignored) {}
        try { return Integer.valueOf(str); } catch (NumberFormatException ignored) {}
        try { return new BigDecimal(str); } catch (NumberFormatException ignored) {}
        if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
            return Boolean.valueOf(str);
        }
        return str;
    }

    private Class<?> getFieldType(Class<?> clazz, String fieldPath) {
        try {
            String[] parts = fieldPath.split("\\.");
            Class<?> current = clazz;
            for (String part : parts) {
                Field field = getDeclaredFieldRecursive(current, part);
                if (field == null) return null;
                current = field.getType();
            }
            return current;
        } catch (Exception e) {
            return null;
        }
    }

    private Field getDeclaredFieldRecursive(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
