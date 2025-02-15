package com.itbatia.psp.repository;

import com.itbatia.psp.entity.AccountEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * @author Batsian_SV
 */
public interface AccountRepository extends R2dbcRepository<AccountEntity, Long> {

    @Query("UPDATE data.accounts AS a SET balance = (balance + :amount), updated_at = CURRENT_TIMESTAMP WHERE a.id = :accountId")
    Mono<Void> upBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal transactionAmount);

    Flux<AccountEntity> findByUserId(long userId);

    Mono<AccountEntity> findByUserIdAndCurrency(long userId, String currency);
}
