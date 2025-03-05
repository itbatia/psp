package com.itbatia.psp.util;

import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.model.Response;

/**
 * @author Batsian_SV
 * @apiNote Response to the merchant for an HTTP-request to create a transaction
 */
public class ResponseToMerchantDataUtils {

    public static final String CARD_IS_BLOCKED = "CARD_IS_BLOCKED";
    public static final String INVALID_CARD_DATA = "INVALID_CARD_DATA";
    public static final String INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS";
    public static final String INVALID_CUSTOMER_DATA = "INVALID_CUSTOMER_DATA";
    public static final String INVALID_ACCOUNT_DATA = "CUSTOMER_DOES_NOT_HAVE_ACCOUNT_IN_SPECIFIED_CURRENCY";

    public static Response getSuccessfulResponseToMerchant() {
        return Response.builder()
                .transactionId("12ff5cdd-c4fa-4023-8b48-d3707917e32e")
                .status(TranStatus.IN_PROGRESS)
                .message("OK")
                .build();
    }

    public static Response getNegativeResponse(String errorMessage) {
        return Response.builder()
                .status(TranStatus.FAILED)
                .message(errorMessage)
                .build();
    }
}
