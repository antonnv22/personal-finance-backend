package com.personalfinance.service;

import com.personalfinance.domain.Category;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.domain.User;
import com.personalfinance.dto.request.CreateCategoryRequest;
import com.personalfinance.dto.request.UpdateCategoryRequest;
import com.personalfinance.dto.response.CategoryResponse;
import com.personalfinance.exception.ConflictException;
import com.personalfinance.exception.ResourceNotFoundException;
import com.personalfinance.mapper.EntityMapper;
import com.personalfinance.repository.CategoryRepository;
import com.personalfinance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll(UUID userId, CategoryType type) {
        List<Category> categories = type == null
                ? categoryRepository.findByUserIdOrderByNameAsc(userId)
                : categoryRepository.findByUserIdAndTypeOrderByNameAsc(userId, type);
        return categories.stream().map(entityMapper::toCategoryResponse).toList();
    }

    @Transactional
    public CategoryResponse create(UUID userId, CreateCategoryRequest request) {
        if (categoryRepository.existsByUserIdAndNameAndType(userId, request.name(), request.type())) {
            throw new ConflictException("Category with this name already exists");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Category category = new Category(user, request.name(), request.type());
        categoryRepository.save(category);
        return entityMapper.toCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse update(UUID userId, UUID categoryId, UpdateCategoryRequest request) {
        Category category = findCategory(userId, categoryId);
        if (categoryRepository.existsByUserIdAndNameAndType(userId, request.name(), category.getType())
                && !category.getName().equals(request.name())) {
            throw new ConflictException("Category with this name already exists");
        }
        category.setName(request.name());
        return entityMapper.toCategoryResponse(category);
    }

    Category findCategory(UUID userId, UUID categoryId) {
        return categoryRepository
                .findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}
