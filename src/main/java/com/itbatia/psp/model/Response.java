package com.itbatia.psp.model;

import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.rest.TransactionRestControllerV1;
import com.itbatia.psp.enums.TranStatus;
import lombok.Builder;
import lombok.Data;

/**
 * @author Batsian_SV
 * @apiNote Response to the merchant for an HTTP-request to create a transaction
 * @see TransactionRestControllerV1#createTopup(String, String, TransactionDto)
 * @see TransactionRestControllerV1#createPayout(String, String, TransactionDto)
 */
@Data
@Builder
public class Response {

    private String transactionId;
    private TranStatus status;
    private String message;
}
