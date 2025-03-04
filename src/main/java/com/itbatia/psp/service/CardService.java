package com.itbatia.psp.service;

import com.itbatia.psp.entity.CardEntity;
import com.itbatia.psp.exception.CardNotFoundException;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface CardService {

    Mono<CardEntity> findByCardNumber(String cardNumber) throws CardNotFoundException;

    Mono<CardEntity> findByCardNumberAndExpDateAndCvv(String cardNumber, String expDate, int cvv) throws CardNotFoundException;
}
