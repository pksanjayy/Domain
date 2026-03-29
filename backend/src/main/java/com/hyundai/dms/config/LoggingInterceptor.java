package com.hyundai.dms.config;

import com.hyundai.dms.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Intercepts every HTTP request to:
 * <ul>
 *   <li>Extract or generate X-Correlation-ID and put it into MDC</li>
 *   <li>Extract userId from SecurityContext and put it into MDC</li>
 *   <li>Log request completion with method, path, status, and duration</li>
 * </ul>
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_USER_ID = "userId";
    private static final String ATTR_START_TIME = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_CORRELATION_ID, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        // User ID from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            MDC.put(MDC_USER_ID, String.valueOf(userDetails.getId()));
        }

        // Start time for duration tracking
        request.setAttribute(ATTR_START_TIME, System.currentTimeMillis());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        long startTime = (long) request.getAttribute(ATTR_START_TIME);
        long durationMs = System.currentTimeMillis() - startTime;

        log.info("[COMPLETED] {} {} status={} durationMs={}",
                request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);

        // Clean up MDC to prevent memory leaks in thread pools
        MDC.remove(MDC_CORRELATION_ID);
        MDC.remove(MDC_USER_ID);
    }
}
