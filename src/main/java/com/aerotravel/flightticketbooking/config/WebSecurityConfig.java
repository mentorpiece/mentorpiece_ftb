package com.aerotravel.flightticketbooking.config;

import com.aerotravel.flightticketbooking.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true, proxyTargetClass = true)
public class WebSecurityConfig {

    @Qualifier("userServiceImpl")
    @Autowired
    private UserDetailsService userService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public static BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/", "/register", "/login", "/promo", "/rate-limit-exceeded").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/", "/swagger-ui", "/v3/api-docs/**", "/v3/api-docs.yaml", "/v3/api-docs-compact", "/v3/api-docs-compact.yaml", "/database/ftb.sql", "/swagger-resources/**", "/webjars/**").permitAll()
                        
                        // Static resources - favicon and other assets
                        .requestMatchers("/favicon.ico", "/robots.txt").permitAll()
                        .requestMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/img/**", "/webjars/**").permitAll()
                        .requestMatchers("/*.js", "/*.css", "/*.html", "/*.png", "/*.jpg", "/*.gif", "/*.svg", "/*.ico").permitAll()
                        
                        // OAuth 2.0 / JWT Authentication endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()

                        // Public API endpoints
                        .requestMatchers("/api/version").permitAll()

                        // Promo-related API endpoints
                        .requestMatchers("/api/promo/**").permitAll()
                        
                        // API endpoints requiring authentication - JWT or Basic Auth
                        .requestMatchers("/api/auth/me", "/api/auth/logout").authenticated()

                        // Specific API endpoints with custom role requirements (must come before general /api/**)
                        .requestMatchers("/api/switch-role", "/api/current-user").hasAnyRole("ADMIN", "AGENT", "USER")

                        // General API endpoints requiring ADMIN or AGENT roles
                        .requestMatchers("/api/**").hasAnyRole("ADMIN", "AGENT")

                        // Web endpoints with role-based access - Session-based
                        .requestMatchers("/switch-role", "/current-user").hasAnyRole("ADMIN", "AGENT", "USER")
                        .requestMatchers("/flights", "/flight/search", "/flight/book/verify", "/flight/book/cancel").hasAnyRole("ADMIN", "AGENT", "USER")

                        // Admin-only endpoints
                        .requestMatchers("/airport/**", "/aircraft/**", "/aircrafts/**", "/airports/**", "/flight/new", "/flight/delete", "/flight/edit").hasRole("ADMIN")

                        // Authenticated user endpoints
                        .requestMatchers("/flight/book/**", "/passengers").hasAnyRole("ADMIN", "AGENT", "USER")

                        .anyRequest().authenticated()
                )
                // Session management - Allow sessions for web interface, but JWT endpoints are stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // Form login for web interface
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                // HTTP Basic auth for API compatibility
                .httpBasic(basic -> basic
                        .realmName("FTB API")
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestURI = request.getRequestURI();
                            String authHeader = request.getHeader("Authorization");
                            
                            // Check if this is an API request or has JWT token
                            if (requestURI.startsWith("/api/") || 
                                requestURI.startsWith("/swagger-ui/") ||
                                requestURI.startsWith("/v3/api-docs/") ||
                                (authHeader != null && authHeader.startsWith("Bearer "))) {
                                // For API and JWT requests, return JSON response
                                response.setStatus(401);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                            } else {
                                // For web pages, redirect to login page
                                response.sendRedirect("/login");
                            }
                        })
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(authenticationProvider())
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}