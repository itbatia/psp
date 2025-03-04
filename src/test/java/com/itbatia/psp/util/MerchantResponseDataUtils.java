package com.itbatia.psp.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;

/**
 * @author Batsian_SV
 */
public class MerchantResponseDataUtils {

    public static String getSuccessfulMerchantResponseBody(String transactionId) {
        ObjectNode objectNode = MapperUtils.initJsonObject();
        objectNode.put("success", true);
        objectNode.put("httpStatus", HttpStatus.OK.value());
        objectNode.put("transactionId", transactionId);

        return objectNode.toPrettyString();
    }

    public static String getFailedMerchantResponseBody(String transactionId) {
        ObjectNode objectNode = MapperUtils.initJsonObject();
        objectNode.put("success", false);
        objectNode.put("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        objectNode.put("transactionId", transactionId);

        return objectNode.toPrettyString();
    }
}
