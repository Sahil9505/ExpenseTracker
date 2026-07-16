package com.nova.finance.account;

import com.nova.common.exception.BadRequestException;
import com.nova.common.exception.ConflictException;
import com.nova.common.exception.ResourceNotFoundException;
import com.nova.finance.account.web.dto.AccountResponse;
import com.nova.finance.account.web.dto.CreateAccountRequest;
import com.nova.finance.account.web.dto.UpdateAccountRequest;
import com.nova.user.User;
import com.nova.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Account lifecycle and ownership rules. Every operation is scoped to the
 * authenticated user; accounts are never shared between users. Deletion is a
 * soft deactivation so transaction history that references the account survives.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountMapper = accountMapper;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts(UUID userId) {
        return accountRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID userId, UUID id) {
        return accountMapper.toResponse(loadOwned(id, userId));
    }

    @Transactional
    public AccountResponse createAccount(UUID userId, CreateAccountRequest request) {
        User user = loadUser(userId);
        String name = request.name().trim();
        String currency = normalizeCurrency(request.currency());
        if (accountRepository.existsByUserIdAndNameAndActiveTrue(userId, name)) {
            throw new ConflictException("You already have an active account named '" + name + "'.");
        }
        BigDecimal balance = request.balance() != null ? request.balance() : BigDecimal.ZERO;
        Account account = new Account(user, name, request.type(), currency, balance);
        account.setInstitution(blankToNull(request.institution()));
        account.setColor(blankToNull(request.color()));
        account.setIcon(blankToNull(request.icon()));
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse updateAccount(UUID userId, UUID id, UpdateAccountRequest request) {
        Account account = loadOwned(id, userId);

        if (request.name() != null && !request.name().isBlank()) {
            String name = request.name().trim();
            if (!name.equals(account.getName())
                    && accountRepository.existsByUserIdAndNameAndActiveTrueAndIdNot(userId, name, id)) {
                throw new ConflictException("You already have an active account named '" + name + "'.");
            }
            account.setName(name);
        }
        if (request.type() != null) {
            account.setType(request.type());
        }
        if (request.currency() != null && !request.currency().isBlank()) {
            account.setCurrency(normalizeCurrency(request.currency()));
        }
        if (request.balance() != null) {
            account.setBalance(request.balance());
        }
        if (request.active() != null) {
            account.setActive(request.active());
        }
        if (request.institution() != null) {
            account.setInstitution(blankToNull(request.institution()));
        }
        if (request.color() != null) {
            account.setColor(blankToNull(request.color()));
        }
        if (request.icon() != null) {
            account.setIcon(blankToNull(request.icon()));
        }
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public void deleteAccount(UUID userId, UUID id) {
        Account account = loadOwned(id, userId);
        // Keep financial history intact: deactivate instead of hard-deleting.
        account.setActive(false);
        accountRepository.save(account);
    }

    private Account loadOwned(UUID id, UUID userId) {
        return accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id.toString()));
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    private String normalizeCurrency(String currency) {
        String normalized = currency.trim().toUpperCase();
        if (!normalized.matches("[A-Z]{3}")) {
            throw new BadRequestException("Currency must be a 3-letter ISO 4217 code.");
        }
        return normalized;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
