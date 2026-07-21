package com.personalfinance.controller;

import com.personalfinance.config.SecurityUtils;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.dto.request.CreateCategoryRequest;
import com.personalfinance.dto.request.UpdateCategoryRequest;
import com.personalfinance.dto.response.CategoryResponse;
import com.personalfinance.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    public CategoryController(CategoryService categoryService, SecurityUtils securityUtils) {
        this.categoryService = categoryService;
        this.securityUtils = securityUtils;
    }

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
