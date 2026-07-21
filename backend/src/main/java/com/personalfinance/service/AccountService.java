package com.personalfinance.service;

import com.personalfinance.domain.Account;
import com.personalfinance.domain.Currency;
import com.personalfinance.domain.User;
import com.personalfinance.dto.request.CreateAccountRequest;
import com.personalfinance.dto.request.UpdateAccountRequest;
import com.personalfinance.dto.response.AccountResponse;
import com.personalfinance.exception.ResourceNotFoundException;
import com.personalfinance.mapper.EntityMapper;
import com.personalfinance.repository.AccountRepository;
import com.personalfinance.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<AccountResponse> getAll(UUID userId) {
        return accountRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponseWithBalance)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(UUID userId, UUID accountId) {
        Account account = findAccount(userId, accountId);
        return toResponseWithBalance(account);
    }

    @Transactional
    public AccountResponse create(UUID userId, CreateAccountRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Currency currency = request.currency() != null ? request.currency() : Currency.RUB;
        Account account = new Account(user, request.name(), request.type(), currency);
        accountRepository.save(account);
        return toResponseWithBalance(account);
    }

    @Transactional
    public AccountResponse update(UUID userId, UUID accountId, UpdateAccountRequest request) {
        Account account = findAccount(userId, accountId);
        account.setName(request.name());
        account.setType(request.type());
        if (request.currency() != null) {
            account.setCurrency(request.currency());
        }
        return toResponseWithBalance(account);
    }

    @Transactional
    public void delete(UUID userId, UUID accountId) {
        Account account = findAccount(userId, accountId);
        if (accountRepository.hasTransactions(accountId)) {
            account.setArchived(true);
        } else {
            accountRepository.delete(account);
        }
    }

    private Account findAccount(UUID userId, UUID accountId) {
        return accountRepository
                .findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private AccountResponse toResponseWithBalance(Account account) {
        BigDecimal balance = accountRepository.calculateBalance(account.getId());
        AccountResponse base = entityMapper.toAccountResponse(account);
        return new AccountResponse(
                base.id(),
                base.name(),
                base.type(),
                base.currency(),
                balance,
                base.archived(),
                base.createdAt(),
                base.updatedAt());
    }
}
