package com.itbatia.psp.util;

import com.itbatia.psp.dto.CardDto;
import com.itbatia.psp.entity.CardEntity;
import com.itbatia.psp.enums.CardStatus;

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

    public static CardDto getIvanIvanovCardDto() {
        return CardDto.builder()
                .cardNumber(IVANOV_CARD_NUMBER)
                .expDate(IVANOV_CARD_EXP_DATE)
                .cvv(IVANOV_CARD_CVV)
                .build();
    }
}
