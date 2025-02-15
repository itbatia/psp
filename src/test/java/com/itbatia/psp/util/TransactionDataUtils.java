package com.itbatia.psp.util;


import com.itbatia.psp.dto.CardDto;
import com.itbatia.psp.dto.CustomerDto;
import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.enums.PaymentMethod;

import java.math.BigDecimal;

public class TransactionDataUtils {

    public static TransactionDto getTopupTransactionDtoTransient() {
        return TransactionDto.builder()
                .paymentMethod(PaymentMethod.CARD)
                .amount(BigDecimal.valueOf(100))
                .currency("BYN")
                .cardData(getCardDtoTransient())
                .language("en")
                .notificationUrl("http://localhost:8081/api/v1/webhooks/topup")
                .customer(getCustomerDtoTransient())
                .build();
    }

    private static CardDto getCardDtoTransient() {
        return CardDto.builder()
                .cardNumber("9112831820410277")
                .expDate("11/25")
                .cvv(566)
                .build();
    }

    private static CustomerDto getCustomerDtoTransient() {
        return CustomerDto.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .country("BY")
                .build();
    }
}
