package com.personalfinance.repository;

import com.personalfinance.domain.Category;
import com.personalfinance.domain.CategoryType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByUserIdOrderByNameAsc(UUID userId);

    List<Category> findByUserIdAndTypeOrderByNameAsc(UUID userId, CategoryType type);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndNameAndType(UUID userId, String name, CategoryType type);
}
