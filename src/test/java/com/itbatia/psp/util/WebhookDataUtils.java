package com.itbatia.psp.util;

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
                .notificationUrl("http://localhost:8081/api/v1/webhooks/topup")
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
                .notificationUrl("http://localhost:8081/api/v1/webhooks/topup")
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
                .cardData(CardDataUtils.getIvanIvanovCardDto(TranType.TOPUP))
                .language("en")
                .customer(CustomerDataUtils.getCustomerIvanovDto())
                .status(TranStatus.SUCCESS)
                .message("OK")
                .build();
    }
}
