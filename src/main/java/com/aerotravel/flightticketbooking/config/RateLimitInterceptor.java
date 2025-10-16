package com.aerotravel.flightticketbooking.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_API_REQUESTS_PER_STANDARD_WINDOW = 20;
    private static final int MAX_PAGE_REQUESTS_PER_STANDARD_WINDOW = 20;
    private static final long RATE_LIMIT_STANDARD_WINDOW = 60;

    // Store request counts per IP address
    private final ConcurrentHashMap<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIpAddress(request);

        if (isRateLimited(clientIp, request)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(429); // Too Many Requests

            // Check if this is an API request or UI request
            String requestURI = request.getRequestURI();
            String acceptHeader = request.getHeader("Accept");

            if (requestURI.startsWith("/api/") ||
                (acceptHeader != null && acceptHeader.contains("application/json"))) {
                // Return JSON response for API requests
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Maximum " + MAX_API_REQUESTS_PER_STANDARD_WINDOW + " API requests per standard window allowed.\",\"retryAfter\":60}");
            } else {
                // Redirect to user-friendly error page for UI requests
                response.sendRedirect("/rate-limit-exceeded");
            }
            return false;
        }

        return true;
    }

    private boolean isRateLimited(String clientIp, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        boolean isApiRequest = isApiRequest(request);
        String key = clientIp + (isApiRequest ? ":api" : ":page");
        RequestInfo requestInfo = requestCounts.computeIfAbsent(key, k -> new RequestInfo());

        synchronized (requestInfo) {
            // Reset counter if window has passed
            if (requestInfo.windowStart.isBefore(now.minus(RATE_LIMIT_STANDARD_WINDOW, ChronoUnit.SECONDS))) {
                requestInfo.count.set(0);
                requestInfo.windowStart = now;
            }

            // Increment and check limit
            int currentCount = requestInfo.count.incrementAndGet();
            int maxRequests = isApiRequest ? MAX_API_REQUESTS_PER_STANDARD_WINDOW : MAX_PAGE_REQUESTS_PER_STANDARD_WINDOW;
            return currentCount > maxRequests;
        }
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");

        return requestURI.startsWith("/api/") ||
               (acceptHeader != null && acceptHeader.contains("application/json"));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        // Check for IP address in various headers (for proxy scenarios)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    // Clean up old entries periodically to prevent memory leaks
    public void cleanupOldEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minus(RATE_LIMIT_STANDARD_WINDOW * 2, ChronoUnit.SECONDS);
        requestCounts.entrySet().removeIf(entry -> entry.getValue().windowStart.isBefore(cutoff));
    }

    private static class RequestInfo {
        private volatile LocalDateTime windowStart = LocalDateTime.now();
        private final AtomicInteger count = new AtomicInteger(0);
    }
}