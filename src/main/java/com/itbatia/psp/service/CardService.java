package com.itbatia.psp.service;

import com.itbatia.psp.entity.CardEntity;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface CardService {

    Mono<CardEntity> findByCondition(String cardNumber);

    Mono<CardEntity> findByCondition(String cardNumber, String expDate, int cvv);
}
