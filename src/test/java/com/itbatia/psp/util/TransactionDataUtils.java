package com.itbatia.psp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.enums.TranType;

import java.math.BigDecimal;

import static com.itbatia.psp.util.ConstantUtils.*;

/**
 * @author Batsian_SV
 */
public class TransactionDataUtils {

    public static final String TRANSACTION_UID_1 = "12ff5cdd-c4fa-4023-8b48-d3707917e32e";
    public static final String TRANSACTION_UID_2 = "23c3050f-9339-4b3f-a394-2edce4eda242";
    public static final String TRANSACTION_UID_3 = "34656365-4a75-4123-ac06-2ac1c0a5c16f";

    public static TransactionDto getIvanovTopupTransactionDtoIN() {
        return getIvanovTransactionDto(TranType.TOPUP);
    }

    public static TransactionDto getPetrovTopupTransactionDtoIN() {
        return getPetrovTransactionDto(TranType.TOPUP);
    }

    public static TransactionDto getIvanovPayoutTransactionDtoIN() {
        return getIvanovTransactionDto(TranType.PAYOUT);
    }

    public static TransactionEntity getIvanovTopupTransactionPersisted() throws JsonProcessingException {
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        String jsonTransactionDto = MapperUtils.toJson(transactionDto);
        return getIvanovTopupTransactionPersisted(jsonTransactionDto);
    }

    public static TransactionEntity getIvanovTopupTransactionPersisted(TranStatus tranStatus) throws JsonProcessingException {
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        String jsonTransactionDto = MapperUtils.toJson(transactionDto);
        return getIvanovTopupTransactionPersisted(jsonTransactionDto, tranStatus);
    }

    public static TransactionEntity getIvanovTopupTransactionPersisted(String jsonTransactionDto) {
        return getIvanovTopupTransactionPersisted(jsonTransactionDto, TranStatus.SUCCESS);
    }

    public static TransactionEntity getIvanovTopupTransactionPersisted(String jsonTransactionDto, TranStatus tranStatus) {
        return TransactionEntity.builder()
                .transactionId(TRANSACTION_UID_1)
                .accountIdFrom(AccountDataUtils.CUSTOMER_IVANOV_BYN_ACCOUNT_ID)
                .accountIdTo(AccountDataUtils.MERCHANT_SMIRNOV_BYN_ACCOUNT_ID)
                .paymentMethod(PaymentMethod.CARD)
                .amount(AMOUNT_100)
                .type(TranType.TOPUP)
                .notificationUrl(TOPUP_NOTIFICATION_URL)
                .language(EN)
                .status(tranStatus)
                .message(OK)
                .request(jsonTransactionDto)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }

    public static TransactionEntity getPetrovTopupTransactionPersisted() throws JsonProcessingException {
        TransactionDto transactionDto = TransactionDataUtils.getPetrovTopupTransactionDtoIN();
        String jsonTransactionDto = MapperUtils.toJson(transactionDto);
        return getPetrovTopupTransactionPersisted(jsonTransactionDto);
    }

    public static TransactionEntity getPetrovTopupTransactionPersisted(TranStatus tranStatus) throws JsonProcessingException {
        TransactionDto transactionDto = TransactionDataUtils.getPetrovTopupTransactionDtoIN();
        String jsonTransactionDto = MapperUtils.toJson(transactionDto);
        return getPetrovTopupTransactionPersisted(jsonTransactionDto, tranStatus);
    }

    public static TransactionEntity getPetrovTopupTransactionPersisted(String jsonTransactionDto) {
        return getPetrovTopupTransactionPersisted(jsonTransactionDto, TranStatus.SUCCESS);
    }

    public static TransactionEntity getPetrovTopupTransactionPersisted(String jsonTransactionDto, TranStatus tranStatus) {
        return TransactionEntity.builder()
                .transactionId(TRANSACTION_UID_2)
                .accountIdFrom(AccountDataUtils.CUSTOMER_PETROV_BYN_ACCOUNT_ID)
                .accountIdTo(AccountDataUtils.MERCHANT_SMIRNOV_BYN_ACCOUNT_ID)
                .paymentMethod(PaymentMethod.CARD)
                .amount(AMOUNT_100)
                .type(TranType.TOPUP)
                .notificationUrl(TOPUP_NOTIFICATION_URL)
                .language(EN)
                .status(tranStatus)
                .message(OK)
                .request(jsonTransactionDto)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }

    public static TransactionEntity getIvanovPayoutTransactionPersisted(String jsonTransactionDto) {
        return TransactionEntity.builder()
                .transactionId(TRANSACTION_UID_1)
                .accountIdFrom(AccountDataUtils.MERCHANT_SMIRNOV_BYN_ACCOUNT_ID)
                .accountIdTo(AccountDataUtils.CUSTOMER_IVANOV_BYN_ACCOUNT_ID)
                .paymentMethod(PaymentMethod.CARD)
                .amount(BigDecimal.valueOf(100))
                .type(TranType.PAYOUT)
                .notificationUrl(PAYOUT_NOTIFICATION_URL)
                .language(EN)
                .status(TranStatus.SUCCESS)
                .message(OK)
                .request(jsonTransactionDto)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }

    private static TransactionDto getIvanovTransactionDto(TranType tranType) {
        return TransactionDto.builder()
                .paymentMethod(PaymentMethod.CARD)
                .amount(AMOUNT_100)
                .currency(BYN)
                .cardData(CardDataUtils.getIvanovCardDto(tranType))
                .language(EN)
                .notificationUrl(TOPUP_NOTIFICATION_URL)
                .customer(CustomerDataUtils.getCustomerIvanovDto())
                .build();
    }

    private static TransactionDto getPetrovTransactionDto(TranType tranType) {
        return TransactionDto.builder()
                .paymentMethod(PaymentMethod.CARD)
                .amount(AMOUNT_100)
                .currency(RUB)
                .cardData(CardDataUtils.getPetrovCardDto(tranType))
                .language(EN)
                .notificationUrl(TOPUP_NOTIFICATION_URL)
                .customer(CustomerDataUtils.getCustomerPetrovDto())
                .build();
    }

    public static TransactionDto getIvanovTopupTransactionDtoOUT() {
        return TransactionDto.builder()
                .transactionId(TRANSACTION_UID_1)
                .paymentMethod(PaymentMethod.CARD)
                .amount(AMOUNT_100)
                .currency(BYN)
                .cardData(CardDataUtils.getIvanovCardDto(TranType.TOPUP))
                .language(EN)
                .notificationUrl(TOPUP_NOTIFICATION_URL)
                .customer(CustomerDataUtils.getCustomerIvanovDto())
                .status(TranStatus.SUCCESS)
                .message(OK)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }

    public static TransactionDto getPetrovTopupTransactionDtoOUT() {
        return TransactionDto.builder()
                .transactionId(TRANSACTION_UID_2)
                .paymentMethod(PaymentMethod.CARD)
                .amount(AMOUNT_100)
                .currency(RUB)
                .cardData(CardDataUtils.getPetrovCardDto(TranType.TOPUP))
                .language(EN)
                .notificationUrl(TOPUP_NOTIFICATION_URL)
                .customer(CustomerDataUtils.getCustomerPetrovDto())
                .status(TranStatus.SUCCESS)
                .message(OK)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }

    public static TransactionEntity getPetrovTopupTransactionTransient(long accountIdFrom, long accountIdTo, TranType tranType) {
        return TransactionEntity.builder()
                .accountIdFrom(accountIdFrom)
                .accountIdTo(accountIdTo)
                .paymentMethod(PaymentMethod.CARD)
                .amount(AMOUNT_100)
                .type(TranType.TOPUP)
                .notificationUrl(TOPUP_NOTIFICATION_URL)
                .language(EN)
                .status(TranStatus.IN_PROGRESS)
                .message(OK)
                .request(MOCK_REQUEST)
                .build();
    }
}
