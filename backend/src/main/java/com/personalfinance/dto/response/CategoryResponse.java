package com.personalfinance.dto.response;

import com.personalfinance.domain.CategoryType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        CategoryType type,
        LocalDateTime createdAt) {
}
