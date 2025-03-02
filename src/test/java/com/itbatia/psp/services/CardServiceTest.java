package com.itbatia.psp.services;

import com.itbatia.psp.entity.CardEntity;
import com.itbatia.psp.exception.CardNotFoundException;
import com.itbatia.psp.repository.CardRepository;
import com.itbatia.psp.service.impl.CardServiceImpl;
import com.itbatia.psp.util.CardDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author Batsian_SV
 */
@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardServiceUnderTest;

    @Test
    @DisplayName("Test get card by cardNumber functionality")
    public void givenCardNumber_whenGetByCardNumber_thenCardIsReturned() {
        //given
        BDDMockito.given(cardRepository.findByCardNumber(anyString()))
                .willReturn(Mono.just(CardDataUtils.getIvanIvanovCardPersisted()));
        //when
        CardEntity obtainedCard = cardServiceUnderTest.findByCardNumber("9112831820410277").block();
        //then
        assertThat(obtainedCard).isNotNull();
        assertThat(obtainedCard.getCardNumber()).isEqualTo("9112831820410277");
    }

    @Test
    @DisplayName("Test get card by incorrect cardNumber functionality")
    public void givenIncorrectCardNumber_whenGetByCardNumber_thenExceptionIsThrown() {
        //given
        BDDMockito.given(cardRepository.findByCardNumber(anyString()))
                .willThrow(CardNotFoundException.class);
        //when
        assertThrows(CardNotFoundException.class, () -> cardServiceUnderTest.findByCardNumber("9112831820410277").block());
        //then
    }

    @Test
    @DisplayName("Test get card by cardNumber and expDate and cvv functionality")
    public void givenCardNumberAndExpDateAndCvv_whenGetCardByCardNumberAndExpDateAndCvv_thenCardIsReturned() {
        //given
        BDDMockito.given(cardRepository.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.just(CardDataUtils.getIvanIvanovCardPersisted()));
        //when
        CardEntity obtainedCard = cardServiceUnderTest.findByCardNumberAndExpDateAndCvv("9112831820410277", "11/25", 566).block();
        //then
        assertThat(obtainedCard).isNotNull();
        assertThat(obtainedCard.getCardNumber()).isEqualTo("9112831820410277");
    }

    @Test
    @DisplayName("Test get card by incorrect cvv functionality")
    public void givenCardNumberAndExpDateAndIncorrectCvv_whenGetByCardNumberAndExpDateAndCvv_thenExceptionIsThrown() {
        //given
        BDDMockito.given(cardRepository.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willThrow(CardNotFoundException.class);
        //when
        assertThrows(CardNotFoundException.class, () -> cardServiceUnderTest.findByCardNumberAndExpDateAndCvv("9112831820410277", "11/25", 472).block());
        //then
    }
}
