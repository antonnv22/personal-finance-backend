package com.personalfinance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for updating an existing category")
public record UpdateCategoryRequest(

        @Schema(
                description = "New category name",
                example = "Groceries",
                maxLength = 50
        )
        @NotBlank
        @Size(max = 50)
        String name
) {
}
