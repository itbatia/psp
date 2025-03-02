package com.itbatia.psp.util;

import com.itbatia.psp.dto.CardDto;
import com.itbatia.psp.entity.CardEntity;
import com.itbatia.psp.enums.CardStatus;

public class CardDataUtils {

    public static CardEntity getIvanIvanovCardTransient() {
        return CardEntity.builder()
                .cardNumber("9112831820410277")
                .expDate("11/25")
                .cvv(566)
                .cardStatus(CardStatus.ACTIVE)
                .build();
    }

    public static CardEntity getIvanIvanovCardPersisted() {
        return CardEntity.builder()
                .id(1L)
                .cardNumber("9112831820410277")
                .expDate("11/25")
                .cvv(566)
                .cardStatus(CardStatus.ACTIVE)
                .build();
    }

    public static CardDto getIvanIvanovCardDto() {
        return CardDto.builder()
                .cardNumber("9112831820410277")
                .expDate("11/25")
                .cvv(566)
                .build();
    }
}
