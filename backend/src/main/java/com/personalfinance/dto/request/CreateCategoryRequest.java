package com.personalfinance.dto.request;

import com.personalfinance.domain.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(
        @NotBlank String name,
        @NotNull CategoryType type) {
}
