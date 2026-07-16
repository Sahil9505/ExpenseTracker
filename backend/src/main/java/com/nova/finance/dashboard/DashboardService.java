package com.nova.finance.dashboard;

import com.nova.common.exception.ResourceNotFoundException;
import com.nova.finance.account.Account;
import com.nova.finance.account.AccountRepository;
import com.nova.finance.dashboard.DashboardSummaryResponse.CategoryBreakdownItem;
import com.nova.finance.dashboard.DashboardSummaryResponse.MonthlyPoint;
import com.nova.finance.transaction.Transaction;
import com.nova.finance.transaction.TransactionRepository;
import com.nova.finance.transaction.TransactionMapper;
import com.nova.finance.transaction.Transaction.Type;
import com.nova.finance.transaction.web.dto.TransactionResponse;
import com.nova.user.User;
import com.nova.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builds the dashboard summary from the authenticated user's accounts and
 * transactions. All totals are scoped to that user and computed in a read-only
 * transaction so the snapshot is consistent.
 */
@Service
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;

    public DashboardService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper,
            UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(UUID userId) {
        User user = loadUser(userId);

        BigDecimal totalBalance = accountRepository.sumActiveBalanceByUserId(userId);

        YearMonth current = YearMonth.now();
        OffsetDateTime monthStart = current.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime monthEnd = current.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        BigDecimal monthlyIncome = transactionRepository.sumAmount(userId, Type.INCOME, monthStart, monthEnd);
        BigDecimal monthlyExpenses = transactionRepository.sumAmount(userId, Type.EXPENSE, monthStart, monthEnd);
        BigDecimal netCashFlow = monthlyIncome.subtract(monthlyExpenses);

        List<CategoryBreakdownItem> categoryBreakdown = transactionRepository
                .sumByCategory(userId, Type.EXPENSE, monthStart, monthEnd)
                .stream()
                .map(row -> new CategoryBreakdownItem(
                        (String) row[0],
                        (String) row[1],
                        (String) row[2],
                        (BigDecimal) row[3]))
                .toList();

        List<TransactionResponse> recentTransactions =
                transactionRepository.findTop8ByUserIdOrderByOccurredAtDesc(userId).stream()
                        .map(transactionMapper::toResponse)
                        .toList();

        OffsetDateTime trendStart = current.minusMonths(5).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        List<Transaction> trendTransactions =
                transactionRepository.findByUserIdAndOccurredAtGreaterThanOrderByOccurredAtDesc(userId, trendStart);
        List<MonthlyPoint> monthlyTrend = buildTrend(trendTransactions, current);

        List<Account> accounts = accountRepository.findByUserIdOrderByCreatedAtDesc(userId);
        int accountsCount = accounts.size();
        int activeAccountsCount = (int) accounts.stream().filter(Account::isActive).count();

        return new DashboardSummaryResponse(
                totalBalance,
                monthlyIncome,
                monthlyExpenses,
                netCashFlow,
                user.getPreferredCurrency(),
                accountsCount,
                activeAccountsCount,
                recentTransactions,
                categoryBreakdown,
                monthlyTrend);
    }

    /** Fills all six months (oldest first), then overlays the real income/expenses. */
    private List<MonthlyPoint> buildTrend(List<Transaction> transactions, YearMonth current) {
        Map<YearMonth, BigDecimal[]> byMonth = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            byMonth.put(ym, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }
        for (Transaction transaction : transactions) {
            YearMonth ym = YearMonth.from(transaction.getOccurredAt());
            BigDecimal[] bucket = byMonth.get(ym);
            if (bucket != null) {
                if (transaction.getType() == Type.INCOME) {
                    bucket[0] = bucket[0].add(transaction.getAmount());
                } else if (transaction.getType() == Type.EXPENSE) {
                    bucket[1] = bucket[1].add(transaction.getAmount());
                }
            }
        }
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM");
        List<MonthlyPoint> points = new ArrayList<>();
        for (Map.Entry<YearMonth, BigDecimal[]> entry : byMonth.entrySet()) {
            YearMonth ym = entry.getKey();
            BigDecimal[] bucket = entry.getValue();
            points.add(new MonthlyPoint(ym.toString(), ym.format(labelFormat), bucket[0], bucket[1]));
        }
        return points;
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }
}
