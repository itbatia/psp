package com.itbatia.psp.security;

import com.itbatia.psp.exception.InvalidAuthException;
import com.itbatia.psp.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static com.itbatia.psp.Utils.StringPool.*;

/**
 * @author Batsian_SV
 */
@Component
@RequiredArgsConstructor
public class AuthFilter implements WebFilter {

    private final AuthProvider authProvider;
    private final MerchantService merchantService;

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain filterChain) {

        String credentials = authProvider.resolveCredentials(exchange);
        if (credentials == null)
            return enrichResponse(exchange, MSG_INVALID_AUTH);

        String decodeCredentials = authProvider.decodeCredentials(credentials);
        if (decodeCredentials == null)
            return enrichResponse(exchange, MSG_INVALID_CREDENTIALS);

        String apiId = decodeCredentials.split(COLON)[0];
        String apiKey = decodeCredentials.split(COLON)[1];

        return merchantService.findByApiId(apiId)
                .switchIfEmpty(Mono.error(new InvalidAuthException(MSG_INVALID_CREDENTIALS)))
                .doOnNext(merchant -> authProvider.checkRemoteIP(merchant, exchange))
                .doOnNext(merchant -> authProvider.checkApiKey(merchant, apiKey))
                .flatMap(merchant -> authProvider.putHeaders(merchant, exchange))
                .flatMap(filterChain::filter);
    }

    private Mono<Void> enrichResponse(ServerWebExchange exchange, String errorMsg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorMsg.getBytes(StandardCharsets.UTF_8))));
    }
}
