package com.itbatia.psp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Batsian_SV
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class JsonConversionException extends RuntimeException {

    public JsonConversionException(String msg) {
        super(msg);
    }
}
