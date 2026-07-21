package com.personalfinance.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryRequest(@NotBlank String name) {
}
