package com.nova.finance.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndNameAndActiveTrue(UUID userId, String name);

    boolean existsByUserIdAndNameAndActiveTrueAndIdNot(UUID userId, String name, UUID id);

    /** Total of all active accounts for a user — the basis for the dashboard total balance. */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.user.id = :userId AND a.active = true")
    BigDecimal sumActiveBalanceByUserId(UUID userId);
}
