package com.itbatia.psp.service;

import com.itbatia.psp.model.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface HttpService {

    Mono<HttpResponse> send(Object requestBody, String url);
}
