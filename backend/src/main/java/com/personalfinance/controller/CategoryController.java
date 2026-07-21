package com.personalfinance.controller;

import com.personalfinance.config.SecurityUtils;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.dto.request.CreateCategoryRequest;
import com.personalfinance.dto.request.UpdateCategoryRequest;
import com.personalfinance.dto.response.CategoryResponse;
import com.personalfinance.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public List<CategoryResponse> getAll(@RequestParam(required = false) CategoryType type) {
        return categoryService.getAll(securityUtils.getCurrentUserId(), type);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.create(securityUtils.getCurrentUserId(), request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
        return categoryService.update(securityUtils.getCurrentUserId(), id, request);
    }
}
