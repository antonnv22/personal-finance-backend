package com.personalfinance.service;

import com.personalfinance.domain.Category;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.domain.User;
import com.personalfinance.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultCategoryInitializer {

    private static final List<String> INCOME_CATEGORIES = List.of("Зарплата", "Инвестиции", "Фриланс");
    private static final List<String> EXPENSE_CATEGORIES =
            List.of("Еда", "Транспорт", "Жильё", "Здоровье", "Развлечения");

    private final CategoryRepository categoryRepository;

    @Transactional
    public void createDefaults(User user) {
        INCOME_CATEGORIES.forEach(name -> saveIfAbsent(user, name, CategoryType.INCOME));
        EXPENSE_CATEGORIES.forEach(name -> saveIfAbsent(user, name, CategoryType.EXPENSE));
    }

    private void saveIfAbsent(User user, String name, CategoryType type) {
        if (!categoryRepository.existsByUserIdAndNameAndType(user.getId(), name, type)) {
            categoryRepository.save(new Category(user, name, type));
        }
    }
}
