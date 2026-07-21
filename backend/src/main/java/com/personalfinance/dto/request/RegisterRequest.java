package com.personalfinance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request")
public record RegisterRequest(
        @Schema(
                description = "User email address",
                example = "john@example.com"
        )
        @NotBlank
        @Email String email,

        @Schema(
                description = "User password. Minimum 8 characters",
                example = "Password123!"
        )
        @NotBlank
        @Size(min = 6, max = 100) String password) {
}
