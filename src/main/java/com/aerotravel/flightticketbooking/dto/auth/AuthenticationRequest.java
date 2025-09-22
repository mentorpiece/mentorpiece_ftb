package com.aerotravel.flightticketbooking.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication request")
public class AuthenticationRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "john")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password", example = "admin123")
    private String password;
}