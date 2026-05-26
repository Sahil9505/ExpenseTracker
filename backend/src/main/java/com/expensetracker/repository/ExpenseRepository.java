package com.expensetracker.repository;

import com.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ExpenseRepository - Handles all database operations for the Expense entity.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Get all expenses for a specific user, ordered by newest date first
    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    /**
     * Search expenses by keyword (title/description) and filter by category.
     * Supports dynamic sorting via Spring Data's Sort object.
     */
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR :category = '' OR e.category = :category)")
    List<Expense> searchExpenses(@Param("userId") Long userId, 
                                 @Param("keyword") String keyword, 
                                 @Param("category") String category, 
                                 Sort sort);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
           "AND (:category IS NULL OR :category = '' OR e.category = :category) " +
           "AND (:month IS NULL OR MONTH(e.date) = :month) " +
           "AND (:year IS NULL OR YEAR(e.date) = :year) " +
           "AND (:paymentMethod IS NULL OR :paymentMethod = '' OR e.paymentMethod = :paymentMethod)")
    List<Expense> findByFilters(@Param("userId") Long userId,
                                @Param("category") String category,
                                @Param("month") Integer month,
                                @Param("year") Integer year,
                                @Param("paymentMethod") String paymentMethod,
                                Sort sort);

    // Calculate total spending for a user
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getTotalAmountByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND MONTH(e.date) = :month AND YEAR(e.date) = :year")
    BigDecimal getTotalAmountByUserIdAndMonth(Long userId, Integer month, Integer year);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId")
    Long getTransactionCountByUserId(Long userId);

    // Get total spending per category for a user (used in pie chart)
    @Query("SELECT e.category as category, SUM(e.amount) as total FROM Expense e WHERE e.user.id = :userId GROUP BY e.category")
    List<Map<String, Object>> getCategoryWiseTotals(Long userId);

    @Query("SELECT e.category as category, SUM(e.amount) as total FROM Expense e WHERE e.user.id = :userId AND MONTH(e.date) = :month AND YEAR(e.date) = :year GROUP BY e.category")
    List<Map<String, Object>> getCategoryWiseTotalsByMonth(Long userId, Integer month, Integer year);

    @Query("SELECT MONTH(e.date) as month, SUM(e.amount) as total FROM Expense e WHERE e.user.id = :userId AND YEAR(e.date) = :year GROUP BY MONTH(e.date)")
    List<Map<String, Object>> getMonthlyTotalsByYear(Long userId, Integer year);
}
