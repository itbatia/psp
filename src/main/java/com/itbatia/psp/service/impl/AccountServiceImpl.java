package com.itbatia.psp.service.impl;

import com.itbatia.psp.entity.AccountEntity;
import com.itbatia.psp.exception.AccountNotFoundException;
import com.itbatia.psp.repository.AccountRepository;
import com.itbatia.psp.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * @author Batsian_SV
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public Flux<AccountEntity> findByUserId(long userId) {
        return accountRepository
                .findByUserId(userId)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("IN findByUserId - Account not found")));
    }

    @Override
    public Mono<AccountEntity> findByUserIdAndCurrency(long userId, String currency) throws AccountNotFoundException {
        return accountRepository
                .findByUserIdAndCurrency(userId, currency)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("IN findByUserIdAndCurrency - Account not found")));
    }

    @Override
    @Transactional
    public Mono<Void> upBalance(Long accountId, BigDecimal transactionAccount) {
        return accountRepository.upBalance(accountId, transactionAccount);
    }

    @Override
    @Transactional
    public Mono<AccountEntity> update(AccountEntity accountEntity) {
        return accountRepository.save(accountEntity);
    }
}
