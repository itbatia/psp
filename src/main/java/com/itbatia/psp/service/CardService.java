package com.itbatia.psp.service;

import com.itbatia.psp.entity.CardEntity;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface CardService {

    Mono<CardEntity> findByCardNumber(String cardNumber);

    Mono<CardEntity> findByCardNumberAndExpDateAndCvv(String cardNumber, String expDate, int cvv);
}
