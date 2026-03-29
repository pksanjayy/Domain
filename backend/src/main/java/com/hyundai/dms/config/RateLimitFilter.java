package com.hyundai.dms.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple token-bucket rate limiter for /api/auth/login.
 * Allows max 5 requests per IP per 60-second window.
 * Returns 429 Too Many Requests with Retry-After header when limit exceeded.
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_TOKENS = 5;
    private static final long WINDOW_MS = 60_000L; // 60 seconds
    private static final String LOGIN_PATH = "/api/auth/login";

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Only rate-limit POST /api/auth/login
        if (!LOGIN_PATH.equals(request.getRequestURI()) || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        TokenBucket bucket = buckets.computeIfAbsent(clientIp, k -> new TokenBucket());

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            long retryAfterSeconds = bucket.getSecondsUntilRefill();
            log.warn("Rate limit exceeded for IP: {} on login endpoint. Retry after {} seconds.", clientIp, retryAfterSeconds);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many login attempts. Please try again after " +
                    retryAfterSeconds + " seconds.\",\"data\":null}"
            );
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Simple token bucket: refills to MAX_TOKENS every WINDOW_MS.
     */
    private static class TokenBucket {
        private final AtomicInteger tokens = new AtomicInteger(MAX_TOKENS);
        private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

        boolean tryConsume() {
            refillIfNeeded();
            return tokens.getAndUpdate(t -> t > 0 ? t - 1 : 0) > 0;
        }

        long getSecondsUntilRefill() {
            long elapsed = System.currentTimeMillis() - windowStart.get();
            long remaining = WINDOW_MS - elapsed;
            return Math.max(1, (remaining + 999) / 1000);
        }

        private void refillIfNeeded() {
            long now = System.currentTimeMillis();
            long start = windowStart.get();
            if (now - start >= WINDOW_MS) {
                if (windowStart.compareAndSet(start, now)) {
                    tokens.set(MAX_TOKENS);
                }
            }
        }
    }
}
