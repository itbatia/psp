package com.itbatia.psp.repository;

import com.itbatia.psp.entity.CardEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface CardRepository extends R2dbcRepository<CardEntity, Long> {

    Mono<CardEntity> findByCardNumber(String cardNumber);

    Mono<CardEntity> findByCardNumberAndExpDateAndCvv(String cardNumber, String expDate, int cvv);
}
