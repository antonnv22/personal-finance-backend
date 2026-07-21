package com.personalfinance.dto.request;

import com.personalfinance.domain.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for creating a new category")
public record CreateCategoryRequest(

        @Schema(
                description = "Category name",
                example = "Food",
                maxLength = 50
        )
        @NotBlank
        @Size(max = 50)
        String name,

        @Schema(
                description = "Category type",
                example = "EXPENSE"
        )
        @NotNull
        CategoryType type
) {
}