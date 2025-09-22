package com.aerotravel.flightticketbooking.controller.api;

import com.aerotravel.flightticketbooking.dto.auth.AuthenticationRequest;
import com.aerotravel.flightticketbooking.dto.auth.AuthenticationResponse;
import com.aerotravel.flightticketbooking.model.User;
import com.aerotravel.flightticketbooking.security.JwtTokenUtil;
import com.aerotravel.flightticketbooking.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "JWT Authentication", description = "OAuth 2.0 Bearer Token Authentication API")
public class JwtAuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @Operation(summary = "Authenticate user and get JWT tokens", 
               description = "Login with username/password to receive Bearer tokens for API access")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.getUserByUsername(userDetails.getUsername());
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found", "status", 401));
            }

            String accessToken = jwtTokenUtil.generateToken(userDetails);
            String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

            AuthenticationResponse.UserInfo userInfo = AuthenticationResponse.UserInfo.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstname())
                    .lastName(user.getLastname())
                    .currentRole(user.getCurrentRole())
                    .roles(user.getRoles().stream()
                            .map(role -> role.getName())
                            .collect(Collectors.toList()))
                    .build();

            AuthenticationResponse response = AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400L) // 24 hours
                    .user(userInfo)
                    .build();

            log.info("User {} authenticated successfully via API", request.getUsername());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Failed authentication attempt for user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials", "status", 401));
        } catch (Exception e) {
            log.error("Authentication error for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed", "status", 500));
        }
    }

    @Operation(summary = "Refresh JWT token", 
               description = "Use refresh token to get new access token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid refresh token", "status", 401));
        }

        try {
            String refreshToken = authHeader.substring(7);
            String username = jwtTokenUtil.extractUsername(refreshToken);
            
            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtTokenUtil.isTokenValid(refreshToken, userDetails)) {
                    String newAccessToken = jwtTokenUtil.generateToken(userDetails);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("accessToken", newAccessToken);
                    response.put("tokenType", "Bearer");
                    response.put("expiresIn", 86400L);
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token", "status", 401));
                    
        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token refresh failed", "status", 401));
        }
    }

    @Operation(summary = "Get current user info", 
               description = "Get current authenticated user information")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User info retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required", "status", 401));
        }

        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found", "status", 404));
            }

            AuthenticationResponse.UserInfo userInfo = AuthenticationResponse.UserInfo.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstname())
                    .lastName(user.getLastname())
                    .currentRole(user.getCurrentRole())
                    .roles(user.getRoles().stream()
                            .map(role -> role.getName())
                            .collect(Collectors.toList()))
                    .build();

            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "status", 500));
        }
    }

    @Operation(summary = "Logout", 
               description = "Invalidate current session (for session-based auth)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Note: JWT tokens are stateless and cannot be invalidated server-side
        // In a production environment, you might want to implement a token blacklist
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}