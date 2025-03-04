package com.itbatia.psp.util;

import com.itbatia.psp.model.HttpResponse;
import org.springframework.http.HttpStatus;

/**
 * @author Batsian_SV
 */
public class HttpResponseDataUtils {

    public static HttpResponse getSuccessfulMerchantResponse(String transactionId) {
        return HttpResponse.builder()
                .body(MerchantResponseDataUtils.getSuccessfulMerchantResponseBody(transactionId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    public static HttpResponse getFailedMerchantResponse(String transactionId) {
        return HttpResponse.builder()
                .body(MerchantResponseDataUtils.getFailedMerchantResponseBody(transactionId))
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }
}
