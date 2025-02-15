package com.itbatia.psp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * @author Batsian_SV
 */
@Data
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionDto {

    // IN + OUT
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String currency;
    private CardDto cardData;
    private String language;
    private String notificationUrl;
    private CustomerDto customer;

    // OUT
    private String transactionId;
    private TranStatus status;
    private String message;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
