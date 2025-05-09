package com.itbatia.psp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itbatia.psp.dto.WebhookDto;
import com.itbatia.psp.entity.WebhookEntity;
import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.enums.TranType;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

/**
 * @author Batsian_SV
 */
public class WebhookDataUtils {

    public static WebhookEntity getWebhookPersisted1(String transactionId, String jsonWebhookDto) {
        return WebhookEntity.builder()
                .id(1L)
                .transactionId(transactionId)
                .notificationUrl(ConstantUtils.TOPUP_NOTIFICATION_URL)
                .attempt(1)
                .request(jsonWebhookDto)
                .response(MerchantResponseDataUtils.getFailedMerchantResponseBody(transactionId))
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .toResend(true)
                .build();
    }

    public static WebhookEntity getWebhookPersisted2(String transactionId, String jsonWebhookDto) {
        return WebhookEntity.builder()
                .id(2L)
                .transactionId(transactionId)
                .notificationUrl(ConstantUtils.TOPUP_NOTIFICATION_URL)
                .attempt(1)
                .request(jsonWebhookDto)
                .response(MerchantResponseDataUtils.getFailedMerchantResponseBody(transactionId))
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .toResend(true)
                .build();
    }

    public static WebhookDto getWebhookDto(String transactionId) {
        return WebhookDto.builder()
                .transactionId(transactionId)
                .paymentMethod(PaymentMethod.CARD)
                .amount(BigDecimal.valueOf(100))
                .currency("BYN")
                .type(TranType.TOPUP)
                .cardData(CardDataUtils.getIvanovCardDto(TranType.TOPUP))
                .language("en")
                .customer(CustomerDataUtils.getCustomerIvanovDto())
                .status(TranStatus.SUCCESS)
                .message("OK")
                .createdAt(ConstantUtils.CREATED_AT)
                .updatedAt(ConstantUtils.UPDATED_AT)
                .build();
    }

    public static WebhookEntity getWebhookTransient(String transactionId) throws JsonProcessingException {
        return WebhookEntity.builder()
                .transactionId(transactionId)
                .notificationUrl(ConstantUtils.TOPUP_NOTIFICATION_URL)
                .attempt(1)
                .request(MapperUtils.toJson(getWebhookDto(transactionId)))
                .response(MerchantResponseDataUtils.getFailedMerchantResponseBody(transactionId))
                .responseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .toResend(true)
                .build();
    }
}
