package com.itbatia.psp.service.impl;

import com.itbatia.psp.entity.CardEntity;
import com.itbatia.psp.exception.CardNotFoundException;
import com.itbatia.psp.repository.CardRepository;
import com.itbatia.psp.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Override
    public Mono<CardEntity> findByCardNumber(String cardNumber) throws CardNotFoundException {
        return cardRepository
                .findByCardNumber(cardNumber)
                .switchIfEmpty(Mono.error(new CardNotFoundException("IN findByCardNumber - Card not found")));
    }

    @Override
    public Mono<CardEntity> findByCardNumberAndExpDateAndCvv(String cardNumber, String expDate, int cvv) throws CardNotFoundException {
        return cardRepository
                .findByCardNumberAndExpDateAndCvv(cardNumber, expDate, cvv)
                .switchIfEmpty(Mono.error(new CardNotFoundException("IN findByCardNumberAndExpDateAndCvv - Card not found")));
    }
}
