package com.itbatia.psp.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author Batsian_SV
 */
@Configuration
public class WebClientConfig {

    @Value("${config.rest.connection-timeout}")
    private Integer connectionTimeout;

    @Bean
    public WebClient webClient() {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)                           //Время ожидания соединения
                .responseTimeout(Duration.ofMillis(connectionTimeout))                                     //Время ожидания ответа - основная
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(connectionTimeout, TimeUnit.MILLISECONDS))  //Время ожидания чтения
                        .addHandlerLast(new WriteTimeoutHandler(connectionTimeout, TimeUnit.MILLISECONDS)) //Время ожидания записи
                );

        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
