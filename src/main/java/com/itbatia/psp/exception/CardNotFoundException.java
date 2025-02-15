package com.itbatia.psp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Batsian_SV
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(String msg) {
        super(msg);
    }
}
