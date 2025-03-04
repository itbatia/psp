package com.itbatia.psp.util;

import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.model.Response;

/**
 * @author Batsian_SV
 * @apiNote Response to the merchant for an HTTP-request to create a transaction
 */
public class ResponseToMerchantDataUtils {

    public static Response getSuccessfulResponseToMerchant() {
        return Response.builder()
                .transactionId("12ff5cdd-c4fa-4023-8b48-d3707917e32e")
                .status(TranStatus.IN_PROGRESS)
                .message("OK")
                .build();
    }
}
