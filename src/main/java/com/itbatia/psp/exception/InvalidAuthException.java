package com.itbatia.psp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Batsian_SV
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidAuthException extends RuntimeException {

    public InvalidAuthException(String msg) {
        super(msg);
    }
}
