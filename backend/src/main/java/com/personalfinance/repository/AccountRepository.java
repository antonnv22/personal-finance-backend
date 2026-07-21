package com.personalfinance.repository;

import com.personalfinance.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserIdAndArchivedFalseOrderByCreatedAtDesc(UUID userId);

    List<Account> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
            SELECT COALESCE(SUM(
                CASE WHEN t.type = com.personalfinance.domain.TransactionType.INCOME
                     THEN t.amount ELSE -t.amount END
            ), 0)
            FROM Transaction t
            WHERE t.account.id = :accountId
            """)
    BigDecimal calculateBalance(@Param("accountId") UUID accountId);

    @Query("""
            SELECT COUNT(t) > 0 FROM Transaction t WHERE t.account.id = :accountId
            """)
    boolean hasTransactions(@Param("accountId") UUID accountId);
}
