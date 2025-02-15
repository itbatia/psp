package com.itbatia.psp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.enums.TranType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * @author Batsian_SV
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WebhookDto {

    private String transactionId;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String currency;
    private TranType type;
    private CardDto cardData;
    private String language;
    private CustomerDto customer;
    private TranStatus status;
    private String message;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
