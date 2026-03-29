package com.hyundai.dms.common.filter;

import com.hyundai.dms.exception.ValidationException;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Builds a JPA {@link Specification} from a list of {@link FilterCriteria}.
 * <p>
 * Supports:
 * - All 11 operators: eq, neq, like, gt, gte, lt, lte, in, between, isNull, isNotNull
 * - Nested joins via dot notation (e.g., "vehicle.model" → join on "vehicle", filter on "model")
 * - Type coercion: String values are parsed to LocalDate, LocalDateTime, Long, BigDecimal
 *   based on the target field's metamodel type.
 */
public class SpecificationBuilder<T> {

    public Specification<T> build(List<FilterCriteria> filters) {
        if (filters == null || filters.isEmpty()) {
            return Specification.where(null);
        }

        Specification<T> result = Specification.where(toSpecification(filters.get(0)));
        for (int i = 1; i < filters.size(); i++) {
            result = result.and(toSpecification(filters.get(i)));
        }
        return result;
    }

    private Specification<T> toSpecification(FilterCriteria criteria) {
        return (root, query, cb) -> {
            Path<?> path = resolvePath(root, criteria.field());
            String operator = criteria.operator().toLowerCase();
            Object value = criteria.value();

            return switch (operator) {
                case "eq", "equal" -> cb.equal(path, coerce(value, path.getJavaType()));
                case "neq", "not_equal" -> cb.notEqual(path, coerce(value, path.getJavaType()));
                case "like" -> cb.like(cb.lower(path.as(String.class)),
                        "%" + value.toString().toLowerCase() + "%");
                case "gt", "greater_than" -> cb.greaterThan(path.as(Comparable.class),
                        (Comparable) coerce(value, path.getJavaType()));
                case "gte", "greater_than_or_equal" -> cb.greaterThanOrEqualTo(path.as(Comparable.class),
                        (Comparable) coerce(value, path.getJavaType()));
                case "lt", "less_than" -> cb.lessThan(path.as(Comparable.class),
                        (Comparable) coerce(value, path.getJavaType()));
                case "lte", "less_than_or_equal" -> cb.lessThanOrEqualTo(path.as(Comparable.class),
                        (Comparable) coerce(value, path.getJavaType()));
                case "in" -> buildInPredicate(path, value, cb);
                case "between" -> buildBetweenPredicate(path, value, cb);
                case "isnull" -> cb.isNull(path);
                case "isnotnull", "not_null" -> cb.isNotNull(path);
                default -> throw new ValidationException("Unsupported operator: " + operator);
            };
        };
    }

    /**
     * Resolves a dot-notation field path, creating JOINs for nested fields.
     * E.g., "vehicle.model" → root.join("vehicle").get("model")
     */
    @SuppressWarnings("unchecked")
    private Path<?> resolvePath(Root<T> root, String field) {
        if (!field.contains(".")) {
            return root.get(field);
        }
        String[] parts = field.split("\\.");
        Join<?, ?> join = root.join(parts[0], JoinType.LEFT);
        for (int i = 1; i < parts.length - 1; i++) {
            join = join.join(parts[i], JoinType.LEFT);
        }
        return join.get(parts[parts.length - 1]);
    }

    private Predicate buildInPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        Collection<?> values;
        if (value instanceof Collection<?> col) {
            values = col;
        } else if (value instanceof String str) {
            values = Arrays.asList(str.split(","));
        } else {
            values = List.of(value);
        }

        CriteriaBuilder.In<Object> inClause = cb.in(path.as(Object.class));
        Class<?> fieldType = path.getJavaType();
        for (Object v : values) {
            inClause.value(coerce(v, fieldType));
        }
        return inClause;
    }

    @SuppressWarnings("unchecked")
    private Predicate buildBetweenPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
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

        Class<?> fieldType = path.getJavaType();
        Comparable lower = (Comparable) coerce(parts[0].trim(), fieldType);
        Comparable upper = (Comparable) coerce(parts[1].trim(), fieldType);
        return cb.between(path.as(Comparable.class), lower, upper);
    }

    /**
     * Coerces a String value to the target field type using reflection.
     * Handles: LocalDate, LocalDateTime, Long, Integer, BigDecimal, Boolean, Enum.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object coerce(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        String str = value.toString().trim();

        try {
            if (targetType == Long.class || targetType == long.class) {
                return Long.valueOf(str);
            }
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.valueOf(str);
            }
            if (targetType == BigDecimal.class) {
                return new BigDecimal(str);
            }
            if (targetType == Double.class || targetType == double.class) {
                return Double.valueOf(str);
            }
            if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.valueOf(str);
            }
            if (targetType == LocalDate.class) {
                return LocalDate.parse(str);
            }
            if (targetType == LocalDateTime.class) {
                return LocalDateTime.parse(str);
            }
            if (targetType.isEnum()) {
                return Enum.valueOf((Class<Enum>) targetType, str.toUpperCase());
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new ValidationException(
                    "Cannot coerce value '" + str + "' to type " + targetType.getSimpleName() + ": " + e.getMessage()
            );
        }

        return str;
    }
}
