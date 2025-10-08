package com.aerotravel.flightticketbooking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableScheduling
public class RateLimitConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**") // Apply to all endpoints
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/img/**",
                        "/static/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/robots.txt",
                        "/rate-limit-exceeded" // Exclude rate limit error page to prevent redirect loops
                ); // Exclude static resources and error pages from rate limiting
    }

    // Clean up old rate limit entries every 5 minutes to prevent memory leaks
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void cleanupOldRateLimitEntries() {
        rateLimitInterceptor.cleanupOldEntries();
    }
}