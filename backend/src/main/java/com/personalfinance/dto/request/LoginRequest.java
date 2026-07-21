package com.personalfinance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login request")
public record LoginRequest(
        @Schema(
                description = "User email address",
                example = "user@example.com"
        )
        @NotBlank
        @Email String email,

        @Schema(
                description = "User password",
                example = "Password123!"
        )
        @NotBlank String password) {
}
