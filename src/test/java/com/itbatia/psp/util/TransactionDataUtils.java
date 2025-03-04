package com.itbatia.psp.util;

import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.enums.TranType;

import java.math.BigDecimal;

/**
 * @author Batsian_SV
 */
public class TransactionDataUtils {

    public static TransactionDto getIvanovTopupTransactionTransient() {
        return TransactionDto.builder()
                .paymentMethod(PaymentMethod.CARD)
                .amount(BigDecimal.valueOf(100))
                .currency("BYN")
                .cardData(CardDataUtils.getIvanIvanovCardDto())
                .language("en")
                .notificationUrl("http://localhost:8081/api/v1/webhooks/topup")
                .customer(CustomerDataUtils.getCustomerIvanovDto())
                .build();
    }

    public static TransactionEntity getIvanovTopupTransactionPersisted(String jsonTransactionDto) {
        return TransactionEntity.builder()
                .transactionId("12ff5cdd-c4fa-4023-8b48-d3707917e32e")
                .accountIdFrom(AccountDataUtils.CUSTOMER_IVANOV_BYN_ACCOUNT_ID)
                .accountIdTo(AccountDataUtils.MERCHANT_SMIRNOV_BYN_ACCOUNT_ID)
                .paymentMethod(PaymentMethod.CARD)
                .amount(BigDecimal.valueOf(100))
                .type(TranType.TOPUP)
                .notificationUrl("http://localhost:8081/api/v1/webhooks/topup")
                .language("en")
                .status(TranStatus.SUCCESS)
                .message("OK")
                .request(jsonTransactionDto)
                .build();
    }
}
