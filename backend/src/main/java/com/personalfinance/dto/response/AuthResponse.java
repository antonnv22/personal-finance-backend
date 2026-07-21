package com.personalfinance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Authentication response containing JWT token and user information")
public record AuthResponse(
        @Schema(
                description = "JWT access token used for authenticated requests",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String token,

        @Schema(
                description = "Unique identifier of the authenticated user",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID userId,

        @Schema(
                description = "User email address",
                example = "user@example.com"
        )
        String email) {
}
