package com.itbatia.psp.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Batsian_SV
 */
@Data
@AllArgsConstructor
public class ErrorResponse {

    @JsonProperty("error_message")
    private String errorMessage;
}
