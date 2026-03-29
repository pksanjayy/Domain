package com.hyundai.dms.common.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves {@link FilterRequest} from query parameters:
 * <ul>
 *   <li>{@code filter} — JSON array of FilterCriteria objects</li>
 *   <li>{@code page} — page number (default 0)</li>
 *   <li>{@code size} — page size (default 20, max 100)</li>
 *   <li>{@code sort} — comma-separated field,direction pairs (e.g., {@code sort=createdAt,desc})</li>
 * </ul>
 */
@RequiredArgsConstructor
public class FilterRequestArgumentResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // Only resolve from query params when @RequestBody is NOT present.
        // When @RequestBody is used, let Jackson handle the JSON deserialization.
        return FilterRequest.class.isAssignableFrom(parameter.getParameterType())
                && !parameter.hasParameterAnnotation(RequestBody.class);
    }

    @Override
    public FilterRequest resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return new FilterRequest(List.of(), 0, 20, List.of());
        }

        // Parse filters from JSON
        List<FilterCriteria> filters = new ArrayList<>();
        String filterParam = request.getParameter("filter");
        if (filterParam != null && !filterParam.isBlank()) {
            filters = objectMapper.readValue(filterParam, new TypeReference<List<FilterCriteria>>() {});
        }

        // Parse page and size
        int page = parseIntOrDefault(request.getParameter("page"), 0);
        int size = parseIntOrDefault(request.getParameter("size"), 20);

        // Parse sort params: sort=field,direction (can be repeated)
        List<SortCriteria> sorts = new ArrayList<>();
        String[] sortParams = request.getParameterValues("sort");
        if (sortParams != null) {
            for (String sortParam : sortParams) {
                String[] parts = sortParam.split(",");
                String field = parts[0].trim();
                String direction = parts.length > 1 ? parts[1].trim() : "asc";
                sorts.add(new SortCriteria(field, direction));
            }
        }

        return new FilterRequest(filters, page, size, sorts);
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
