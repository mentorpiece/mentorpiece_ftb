package com.aerotravel.flightticketbooking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Skip JWT processing for certain paths
        String requestPath = request.getServletPath();
        if (shouldSkipJwtProcessing(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Only process JWT if Authorization header with Bearer token is present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Let other authentication mechanisms handle this request
            filterChain.doFilter(request, response);
            return;
        }

        // Only proceed with JWT authentication if no authentication is already set
        // This prevents conflicts with session-based authentication
        if (SecurityContextHolder.getContext().getAuthentication() != null && 
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            username = jwtTokenUtil.extractUsername(jwt);

            if (username != null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtTokenUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("JWT authentication successful for user: {}", username);
                } else {
                    log.warn("Invalid JWT token for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipJwtProcessing(String requestPath) {
        return requestPath.equals("/api/auth/login") ||
               requestPath.equals("/api/auth/register") ||
               requestPath.equals("/api/auth/refresh") ||
               requestPath.equals("/login") ||
               requestPath.equals("/register") ||
               requestPath.equals("/promo") ||
               requestPath.equals("/switch-role") ||  // Allow session-based role switching
               requestPath.startsWith("/css/") ||
               requestPath.startsWith("/js/") ||
               requestPath.startsWith("/img/") ||
               requestPath.startsWith("/static/") ||
               requestPath.startsWith("/swagger-ui/") ||
               requestPath.startsWith("/v3/api-docs/") ||
               requestPath.equals("/v3/api-docs.yaml") ||
               requestPath.equals("/v3/api-docs-compact") ||
               requestPath.equals("/v3/api-docs-compact.yaml") ||
               requestPath.equals("/database/ftb.sql") ||
               requestPath.equals("/favicon.ico");
    }
}