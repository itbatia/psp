package com.itbatia.psp.model;

import com.itbatia.psp.service.HttpService;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatusCode;

/**
 * @author Batsian_SV
 * @apiNote These are {@code body} and {@code statusCode} received from a merchant after the http-request
 * @see HttpService#send(Object, String)
 */
@Data
@Builder
public class HttpResponse {

    private HttpStatusCode statusCode;
    private String body;
}
