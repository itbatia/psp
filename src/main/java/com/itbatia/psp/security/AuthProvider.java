package com.itbatia.psp.security;

import com.itbatia.psp.entity.MerchantEntity;
import com.itbatia.psp.exception.InvalidAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Objects;

import static com.itbatia.psp.Utils.StringPool.*;

/**
 * @author Batsian_SV
 */
@Slf4j
@Component
class AuthProvider {

    private static final String TEST = "test";
    private static final String ACTIVE_PROFILE = System.getProperty("spring.profiles.active", TEST);

    protected String resolveCredentials(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Basic ")) {
            log.error("IN resolveCredentials - {}", INVALID_AUTH);
            return null;
        }
        return authorization.substring(6);
    }

    protected String decodeCredentials(String credentials) {
        try {
            String decodedCredentials = new String(Base64.getDecoder().decode(credentials));
            if (decodedCredentials.contains(COLON))
                return decodedCredentials;
        } catch (IllegalArgumentException ignored) {
        }
        log.error("IN decodeCredentials - {}", INVALID_CREDENTIALS);
        return null;
    }

    protected void checkRemoteIP(MerchantEntity merchant, ServerWebExchange exchange) {
        if (Objects.equals(ACTIVE_PROFILE, TEST))
            return;

        String remoteHost = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getHostName();
        for (String ipAddress : merchant.getIpAddresses()) {
            if (remoteHost.equals(ipAddress))
                return;
        }
        log.error("IN checkRemoteIP - {}", INVALID_IP);
        throw new InvalidAuthException(MSG_INVALID_IP);
    }

    protected void checkApiKey(MerchantEntity merchant, String apiKey) {
        if (merchant.getApiKey().equals(apiKey))
            return;
        log.error("IN checkApiKey - {}", INVALID_API_KEY);
        throw new InvalidAuthException(MSG_INVALID_API_KEY);
    }

    protected Mono<ServerWebExchange> putHeaders(MerchantEntity merchant, ServerWebExchange exchange) {
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate();

        builder.header(MERCHANT_ID, Objects.requireNonNull(merchant.getId()).toString());
        builder.header(USER_ID, merchant.getUserId().toString());
        ServerHttpRequest mutateRequest = builder.build();

        ServerWebExchange mutateExchange = exchange.mutate().request(mutateRequest).build();
        return Mono.just(mutateExchange);
    }
}
