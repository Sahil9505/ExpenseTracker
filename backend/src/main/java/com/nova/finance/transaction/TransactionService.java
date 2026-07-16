package com.nova.finance.transaction;

import com.nova.common.exception.BadRequestException;
import com.nova.common.exception.ResourceNotFoundException;
import com.nova.finance.account.Account;
import com.nova.finance.account.AccountRepository;
import com.nova.finance.category.Category;
import com.nova.finance.category.CategoryRepository;
import com.nova.finance.transaction.web.dto.CreateTransactionRequest;
import com.nova.finance.transaction.web.dto.TransactionResponse;
import com.nova.finance.transaction.web.dto.UpdateTransactionRequest;
import com.nova.user.User;
import com.nova.user.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Transaction lifecycle and the balance consistency rules that keep every account
 * in sync. All balance changes happen inside a single transaction so a create,
 * update, or delete can never leave an account's running balance wrong.
 *
 * <p>Balance rules:</p>
 * <ul>
 *   <li>INCOME adds to the account balance.</li>
 *   <li>EXPENSE subtracts from the account balance.</li>
 *   <li>TRANSFER subtracts from the source and adds to the destination.</li>
 *   <li>Update reverses the previous effect, then applies the new one.</li>
 *   <li>Delete reverses the effect.</li>
 * </ul>
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionMapper = transactionMapper;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> list(UUID userId, TransactionFilter filter) {
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }
            if (filter.accountId() != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("account").get("id"), filter.accountId()),
                        cb.equal(root.get("destinationAccount").get("id"), filter.accountId())));
            }
            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }
            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("occurredAt"), filter.to()));
            }
            if (filter.search() != null && !filter.search().isBlank()) {
                String like = "%" + filter.search().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("merchant")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return transactionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "occurredAt")).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(UUID userId, UUID id) {
        return transactionMapper.toResponse(loadOwned(id, userId));
    }

    @Transactional
    public TransactionResponse create(UUID userId, CreateTransactionRequest request) {
        User user = loadUser(userId);
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setMerchant(blankToNull(request.merchant()));
        transaction.setDescription(blankToNull(request.note()));
        transaction.setTags(blankToNull(request.tags()));
        transaction.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : OffsetDateTime.now());

        Account account;
        Account destination = null;
        Category category = null;

        if (request.type() == Transaction.Type.TRANSFER) {
            if (request.accountId() == null || request.destinationAccountId() == null) {
                throw new BadRequestException("Transfers require a source and a destination account.");
            }
            if (request.categoryId() != null) {
                throw new BadRequestException("Transfers do not use a category.");
            }
            account = loadOwnedAccount(request.accountId(), userId);
            destination = loadOwnedAccount(request.destinationAccountId(), userId);
            if (account.getId().equals(destination.getId())) {
                throw new BadRequestException("Transfer source and destination must be different accounts.");
            }
            requireActive(account, destination);
            transaction.setCurrency(resolveCurrency(request.currency(), account));
        } else {
            if (request.accountId() == null) {
                throw new BadRequestException("A source account is required.");
            }
            if (request.categoryId() == null) {
                throw new BadRequestException("A " + request.type().name().toLowerCase() + " category is required.");
            }
            account = loadOwnedAccount(request.accountId(), userId);
            if (!account.isActive()) {
                throw new BadRequestException("That account is deactivated.");
            }
            category = loadOwnedCategory(request.categoryId(), userId);
            if (!categoryMatchesType(category, request.type())) {
                throw new BadRequestException(
                        "The selected category is not an " + request.type().name().toLowerCase() + " category.");
            }
            transaction.setCurrency(resolveCurrency(request.currency(), account));
        }

        transaction.setAccount(account);
        transaction.setDestinationAccount(destination);
        transaction.setCategory(category);

        applyBalanceEffect(transaction, 1);
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(UUID userId, UUID id, UpdateTransactionRequest request) {
        Transaction transaction = loadOwned(id, userId);
        // Undo the previous balance effect while the old references are still attached.
        applyBalanceEffect(transaction, -1);

        Transaction.Type newType = request.type() != null ? request.type() : transaction.getType();
        BigDecimal newAmount = request.amount() != null ? request.amount() : transaction.getAmount();

        Account newAccount = request.accountId() != null ? loadOwnedAccount(request.accountId(), userId) : transaction.getAccount();
        Account newDestination = request.destinationAccountId() != null
                ? loadOwnedAccount(request.destinationAccountId(), userId) : transaction.getDestinationAccount();
        Category newCategory = request.categoryId() != null ? loadOwnedCategory(request.categoryId(), userId) : transaction.getCategory();

        if (newType == Transaction.Type.TRANSFER) {
            if (newAccount == null || newDestination == null) {
                throw new BadRequestException("Transfers require a source and a destination account.");
            }
            if (newAccount.getId().equals(newDestination.getId())) {
                throw new BadRequestException("Transfer source and destination must be different accounts.");
            }
            requireActive(newAccount, newDestination);
            newCategory = null;
        } else {
            if (newAccount == null) {
                throw new BadRequestException("A source account is required.");
            }
            if (!newAccount.isActive()) {
                throw new BadRequestException("That account is deactivated.");
            }
            if (newCategory == null) {
                throw new BadRequestException("A " + newType.name().toLowerCase() + " category is required.");
            }
            if (!categoryMatchesType(newCategory, newType)) {
                throw new BadRequestException(
                        "The selected category is not an " + newType.name().toLowerCase() + " category.");
            }
            newDestination = null;
        }

        transaction.setType(newType);
        transaction.setAmount(newAmount);
        transaction.setAccount(newAccount);
        transaction.setDestinationAccount(newDestination);
        transaction.setCategory(newCategory);

        if (request.merchant() != null) {
            transaction.setMerchant(blankToNull(request.merchant()));
        }
        if (request.note() != null) {
            transaction.setDescription(blankToNull(request.note()));
        }
        if (request.tags() != null) {
            transaction.setTags(blankToNull(request.tags()));
        }
        if (request.occurredAt() != null) {
            transaction.setOccurredAt(request.occurredAt());
        }
        if (request.currency() != null) {
            transaction.setCurrency(normalizeCurrency(request.currency()));
        } else if (request.accountId() != null) {
            transaction.setCurrency(newAccount.getCurrency());
        }

        applyBalanceEffect(transaction, 1);
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Transaction transaction = loadOwned(id, userId);
        applyBalanceEffect(transaction, -1);
        transactionRepository.delete(transaction);
    }

    /** Applies (+1) or reverses (-1) the balance effect of a transaction. */
    private void applyBalanceEffect(Transaction transaction, int sign) {
        BigDecimal delta = transaction.getAmount().multiply(BigDecimal.valueOf(sign));
        switch (transaction.getType()) {
            case INCOME -> transaction.getAccount().setBalance(transaction.getAccount().getBalance().add(delta));
            case EXPENSE -> transaction.getAccount().setBalance(transaction.getAccount().getBalance().subtract(delta));
            case TRANSFER -> {
                transaction.getAccount().setBalance(transaction.getAccount().getBalance().subtract(delta));
                transaction.getDestinationAccount().setBalance(transaction.getDestinationAccount().getBalance().add(delta));
            }
        }
    }

    /** Income/expense transactions require a category of the same kind. */
    private boolean categoryMatchesType(Category category, Transaction.Type type) {
        return category.getType().name().equals(type.name());
    }

    private void requireActive(Account... accounts) {
        for (Account account : accounts) {
            if (!account.isActive()) {
                throw new BadRequestException("That account is deactivated.");
            }
        }
    }

    private String resolveCurrency(String requested, Account account) {
        return requested != null && !requested.isBlank() ? normalizeCurrency(requested) : account.getCurrency();
    }

    private String normalizeCurrency(String currency) {
        String normalized = currency.trim().toUpperCase();
        if (!normalized.matches("[A-Z]{3}")) {
            throw new BadRequestException("Currency must be a 3-letter ISO 4217 code.");
        }
        return normalized;
    }

    private Account loadOwnedAccount(UUID id, UUID userId) {
        return accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id.toString()));
    }

    private Category loadOwnedCategory(UUID id, UUID userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id.toString()));
    }

    private Transaction loadOwned(UUID id, UUID userId) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id.toString()));
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
