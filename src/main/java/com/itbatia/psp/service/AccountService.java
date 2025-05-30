package com.itbatia.psp.service;

import com.itbatia.psp.entity.AccountEntity;
import com.itbatia.psp.exception.AccountNotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * @author Batsian_SV
 */
public interface AccountService {

    Flux<AccountEntity> findByUserId(long userId) throws AccountNotFoundException;

    Mono<AccountEntity> findByUserIdAndCurrency(long userId, String currency) throws AccountNotFoundException;

    Mono<Void> upBalance(Long accountId, BigDecimal transactionAccount);

    Mono<AccountEntity> update(AccountEntity accountEntity);
}
