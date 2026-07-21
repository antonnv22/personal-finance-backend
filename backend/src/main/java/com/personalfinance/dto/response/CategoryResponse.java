package com.personalfinance.dto.response;

import com.personalfinance.domain.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Category response")
public record CategoryResponse(

        @Schema(
                description = "Unique identifier of the category",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID id,

        @Schema(
                description = "Category name",
                example = "Food"
        )
        String name,

        @Schema(
                description = "Category type: expense or income",
                example = "EXPENSE"
        )
        CategoryType type,

        @Schema(
                description = "Date and time when the category was created",
                example = "2026-07-21T15:30:00"
        )
        LocalDateTime createdAt
) {
}