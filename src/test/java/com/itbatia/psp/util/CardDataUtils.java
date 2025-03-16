package com.itbatia.psp.util;

import com.itbatia.psp.dto.CardDto;
import com.itbatia.psp.entity.CardEntity;
import com.itbatia.psp.enums.CardStatus;
import com.itbatia.psp.enums.TranType;

/**
 * @author Batsian_SV
 */
public class CardDataUtils {

    public static final String IVANOV_CARD_NUMBER = "9112831820410277";
    public static final String IVANOV_CARD_EXP_DATE = "11/25";
    public static final int IVANOV_CARD_CVV = 566;
    public static final String PETROV_CARD_NUMBER = "4102778822334893";
    public static final String PETROV_CARD_EXP_DATE = "08/26";
    public static final int PETROV_CARD_CVV = 211;

    public static CardEntity getIvanIvanovCardTransient() {
        return CardEntity.builder()
                .cardNumber(IVANOV_CARD_NUMBER)
                .expDate(IVANOV_CARD_EXP_DATE)
                .cvv(IVANOV_CARD_CVV)
                .cardStatus(CardStatus.ACTIVE)
                .build();
    }

    public static CardEntity getIvanIvanovCardPersisted() {
        return CardEntity.builder()
                .id(1L)
                .accountId(AccountDataUtils.CUSTOMER_IVANOV_BYN_ACCOUNT_ID)
                .cardNumber(IVANOV_CARD_NUMBER)
                .expDate(IVANOV_CARD_EXP_DATE)
                .cvv(IVANOV_CARD_CVV)
                .cardStatus(CardStatus.ACTIVE)
                .build();
    }

    public static CardEntity getPetrPetrovCardPersisted() {
        return CardEntity.builder()
                .id(2L)
                .accountId(AccountDataUtils.CUSTOMER_PETROV_BYN_ACCOUNT_ID)
                .cardNumber(PETROV_CARD_NUMBER)
                .expDate(PETROV_CARD_EXP_DATE)
                .cvv(PETROV_CARD_CVV)
                .cardStatus(CardStatus.ACTIVE)
                .build();
    }

    public static CardEntity getBlockedIvanIvanovCardPersisted() {
        return CardEntity.builder()
                .id(1L)
                .accountId(AccountDataUtils.CUSTOMER_IVANOV_BYN_ACCOUNT_ID)
                .cardNumber(IVANOV_CARD_NUMBER)
                .expDate(IVANOV_CARD_EXP_DATE)
                .cvv(IVANOV_CARD_CVV)
                .cardStatus(CardStatus.BLOCKED)
                .build();
    }

    public static CardDto getPetrovCardDto(TranType tranType) {
        return switch (tranType) {
            case TOPUP -> buildPetrovCardDto1();
            case PAYOUT -> buildPetrovCardDto2();
        };
    }

    public static CardDto getIvanovCardDto(TranType tranType) {
        return switch (tranType) {
            case TOPUP -> buildIvanovCardDto1();
            case PAYOUT -> buildIvanovCardDto2();
        };
    }

    private static CardDto buildIvanovCardDto1() {
        return CardDto.builder()
                .cardNumber(IVANOV_CARD_NUMBER)
                .expDate(IVANOV_CARD_EXP_DATE)
                .cvv(IVANOV_CARD_CVV)
                .build();
    }

    private static CardDto buildIvanovCardDto2() {
        return CardDto.builder()
                .cardNumber(IVANOV_CARD_NUMBER)
                .build();
    }

    private static CardDto buildPetrovCardDto1() {
        return CardDto.builder()
                .cardNumber(PETROV_CARD_NUMBER)
                .expDate(PETROV_CARD_EXP_DATE)
                .cvv(PETROV_CARD_CVV)
                .build();
    }

    private static CardDto buildPetrovCardDto2() {
        return CardDto.builder()
                .cardNumber(PETROV_CARD_NUMBER)
                .build();
    }
}
