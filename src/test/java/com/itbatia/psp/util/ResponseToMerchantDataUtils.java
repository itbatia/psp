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
    public static final String CUSTOMER_IS_NOT_OWNER_OF_THIS_CARD = "CUSTOMER_IS_NOT_OWNER_OF_THIS_CARD";
    public static final String INVALID_ACCOUNT_DATA = "CUSTOMER_DOES_NOT_HAVE_ACCOUNT_IN_SPECIFIED_CURRENCY";

    public static Response getSuccessfulResponseToMerchant() {
        return Response.builder()
                .transactionId(TransactionDataUtils.TRANSACTION_UID_1)
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
