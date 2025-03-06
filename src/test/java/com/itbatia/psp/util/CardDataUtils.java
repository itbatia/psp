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
                .id(1L)
                .accountId(AccountDataUtils.CUSTOMER_PETROV_BYN_ACCOUNT_ID)
                .cardNumber("4102778822334893")
                .expDate("08/26")
                .cvv(211)
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

    public static CardDto getIvanIvanovCardDto(TranType tranType) {
        return switch (tranType) {
            case TOPUP -> buildIvanIvanovCardDto1();
            case PAYOUT -> buildIvanIvanovCardDto2();
        };
    }

    private static CardDto buildIvanIvanovCardDto1() {
        return CardDto.builder()
                .cardNumber(IVANOV_CARD_NUMBER)
                .expDate(IVANOV_CARD_EXP_DATE)
                .cvv(IVANOV_CARD_CVV)
                .build();
    }

    private static CardDto buildIvanIvanovCardDto2() {
        return CardDto.builder()
                .cardNumber(IVANOV_CARD_NUMBER)
                .build();
    }
}
