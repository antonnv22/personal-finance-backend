package com.personalfinance.repository;

import com.personalfinance.domain.Transaction;
import com.personalfinance.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Необязательные фильтры выражены через COALESCE, а не через
     * {@code :param IS NULL}: в последнем случае PostgreSQL не может вывести тип
     * параметра и падает с «could not determine data type». Внутри COALESCE тип
     * берётся от колонки. Все участвующие колонки объявлены NOT NULL, поэтому
     * при null-фильтре условие вырождается в тождество.
     */
    @Query("""
            SELECT t FROM Transaction t
            JOIN FETCH t.account
            JOIN FETCH t.category
            WHERE t.user.id = :userId
            AND t.account.id = COALESCE(:accountId, t.account.id)
            AND t.category.id = COALESCE(:categoryId, t.category.id)
            AND t.type = COALESCE(:type, t.type)
            AND t.transactionDate >= COALESCE(:from, t.transactionDate)
            AND t.transactionDate <= COALESCE(:to, t.transactionDate)
            ORDER BY t.transactionDate DESC, t.createdAt DESC
            """)
    Page<Transaction> findFiltered(
            @Param("userId") UUID userId,
            @Param("accountId") UUID accountId,
            @Param("categoryId") UUID categoryId,
            @Param("type") TransactionType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.user.id = :userId
            AND t.type = :type
            AND t.transactionDate BETWEEN :from AND :to
            """)
    BigDecimal sumByTypeAndPeriod(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT c.name, COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            JOIN t.category c
            WHERE t.user.id = :userId
            AND t.type = com.personalfinance.domain.TransactionType.EXPENSE
            AND t.transactionDate BETWEEN :from AND :to
            GROUP BY c.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> sumExpensesByCategory(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
