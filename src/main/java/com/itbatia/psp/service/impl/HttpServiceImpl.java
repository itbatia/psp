package com.itbatia.psp.service.impl;

import com.itbatia.psp.model.HttpResponse;
import com.itbatia.psp.service.HttpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * @author Batsian_SV
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HttpServiceImpl implements HttpService {

    private final WebClient webClient;

    @Override
    public Mono<HttpResponse> send(Object requestBody, String url) {
        return webClient
                .post()
                .uri(URI.create(url))
                .bodyValue(requestBody)
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> {
                                    if (response.statusCode().is2xxSuccessful()) {
                                        log.info("IN send - Webhook delivered successfully. HttpStatus = {}", response.statusCode());
                                    } else {
                                        log.warn("IN send - Webhook not delivered. HttpStatus = {}", response.statusCode());
                                    }
                                    return HttpResponse.builder()
                                            .body(body)
                                            .statusCode(response.statusCode())
                                            .build();
                                })
                );
    }
}
