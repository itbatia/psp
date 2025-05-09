package com.itbatia.psp.model;

import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.rest.TransactionRestControllerV1;
import com.itbatia.psp.enums.TranStatus;
import lombok.Builder;
import lombok.Data;

/**
 * @author Batsian_SV
 * @apiNote Response to the merchant for an HTTP-request to create a transaction
 * @see TransactionRestControllerV1#createTopup(Long, TransactionDto) Create topup transaction
 * @see TransactionRestControllerV1#createPayout(Long, TransactionDto) Create payout transaction
 */
@Data
@Builder
public class Response {

    private String transactionId;
    private TranStatus status;
    private String message;

    public static Response build(String transactionId, TranStatus status, String message) {
        return Response.builder()
                .transactionId(transactionId)
                .status(status)
                .message(message)
                .build();
    }

    public static Response build(TranStatus status, String message) {
        return Response.builder()
                .status(status)
                .message(message)
                .build();
    }
}
