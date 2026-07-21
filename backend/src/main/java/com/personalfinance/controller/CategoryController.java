package com.personalfinance.controller;

import com.personalfinance.config.SecurityUtils;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.dto.request.CreateCategoryRequest;
import com.personalfinance.dto.request.UpdateCategoryRequest;
import com.personalfinance.dto.response.CategoryResponse;
import com.personalfinance.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(
        name = "Categories",
        description = "Operations for managing expense and income categories"
)
public class CategoryController {

    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    @Operation(
            summary = "Get all categories",
            description = "Returns all categories belonging to the authenticated user. "
                    + "Can be filtered by category type."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categories successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = CategoryResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            )
    })
    @GetMapping
    public List<CategoryResponse> getAll(
            @Parameter(
                    description = "Filter categories by type",
                    example = "EXPENSE"
            )
            @RequestParam(required = false) CategoryType type) {
        return categoryService.getAll(securityUtils.getCurrentUserId(), type);
    }

    @Operation(
            summary = "Create category",
            description = "Creates a new category for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Category successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CategoryResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid category data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.create(securityUtils.getCurrentUserId(), request);
    }

    @Operation(
            summary = "Update category",
            description = "Updates an existing category belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Category successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CategoryResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid category data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    public CategoryResponse update(
            @Parameter(
                    description = "Category identifier",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
        return categoryService.update(securityUtils.getCurrentUserId(), id, request);
    }
}
