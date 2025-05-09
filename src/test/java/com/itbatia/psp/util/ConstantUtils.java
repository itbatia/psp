package com.itbatia.psp.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * @author Batsian_SV
 */
public class ConstantUtils {

    public static final OffsetDateTime CREATED_AT = OffsetDateTime.now();
    public static final OffsetDateTime UPDATED_AT = OffsetDateTime.now();
    public static final LocalDate END_DATE = LocalDate.now();
    public static final LocalDate START_DATE = END_DATE.minusDays(1);

    public static final String BYN = "BYN";
    public static final String RUB = "RUB";
    public static final String USD = "USD";
    public static final String EN = "en";
    public static final String OK = "OK";
    public static final String PAYMENT_SUCCESS = "Payment completed successfully";
    public static final String PAYMENT_FAILED = "Payment failed";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String FAILED = "FAILED";
    public static final String MOCK_REQUEST = "{}";
    public static final String TOPUP_NOTIFICATION_URL = "http://localhost:8081/api/v1/webhooks/topup";
    public static final String PAYOUT_NOTIFICATION_URL = "http://localhost:8081/api/v1/webhooks/payout";

    public static final BigDecimal AMOUNT_100 = BigDecimal.valueOf(100);
    public static final BigDecimal BALANCE_1000 = BigDecimal.valueOf(1000);
    public static final BigDecimal BALANCE_50 = BigDecimal.valueOf(50);

    public final static String CASE_1 = "1";
    public final static String CASE_2 = "2";
    public final static String CASE_3 = "3";
}
