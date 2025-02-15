package com.itbatia.psp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.dto.WebhookDto;
import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.entity.WebhookEntity;
import com.itbatia.psp.model.HttpResponse;
import com.itbatia.psp.repository.WebhookRepository;
import com.itbatia.psp.service.HttpService;
import com.itbatia.psp.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.itbatia.psp.Utils.StringPool.ERROR_MSG;

/**
 * @author Batsian_SV
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebhookServiceImpl implements WebhookService {

    @Value("${config.webhook.max-attempt}")
    private Integer maxAttempt;

    @Value("${config.webhook.limit}")
    private Integer webhooksLimit;

    private final HttpService httpService;
    private final ObjectMapper objectMapper;
    private final WebhookRepository webhookRepository;

    private final static int FIRST_ATTEMPT = 1;

    //////-----------------------------------------  FIRST SEND WEBHOOK  -----------------------------------------//////

    @Override
    @Transactional
    public Mono<Void> sendAndSaveWebhook(TransactionEntity transactionEntity) {
        WebhookDto webhookDto = mapToWebhookDto(transactionEntity);
        return httpService
                .send(webhookDto, transactionEntity.getNotificationUrl())
                .onErrorResume(this::defaultHttpResponse)
                .flatMap(response -> mapToWebhookEntityAndSave(transactionEntity, webhookDto, response))
                .doOnSuccess(webhookEntity -> log.info("IN sendAndSaveWebhook - Webhook sent and saved"))
                .then();
    }

    private WebhookDto mapToWebhookDto(TransactionEntity transactionEntity) {
        try {
            TransactionDto transactionDto = objectMapper.readValue(transactionEntity.getRequest(), TransactionDto.class);

            return WebhookDto.builder()
                    .transactionId(transactionEntity.getTransactionId())
                    .paymentMethod(transactionEntity.getPaymentMethod())
                    .amount(transactionEntity.getAmount())
                    .currency(transactionDto.getCurrency())
                    .type(transactionEntity.getType())
                    .cardData(transactionDto.getCardData())
                    .language(transactionDto.getLanguage())
                    .customer(transactionDto.getCustomer())
                    .status(transactionEntity.getStatus())
                    .message(transactionEntity.getMessage())
                    .createdAt(transactionEntity.getCreatedAt())
                    .updatedAt(transactionEntity.getUpdatedAt())
                    .build();

        } catch (JsonProcessingException e) {
            log.error("IN mapToWebhookDto - Error while converting TransactionEntity to TransactionDto. Reason: {}", e.getMessage());
            return null;
        }
    }

    private Mono<Void> mapToWebhookEntityAndSave(TransactionEntity transactionEntity, WebhookDto webhookDto, HttpResponse httpResponse) {
        return Mono.just(httpResponse)
                .map(response -> toWebhookEntity(transactionEntity, webhookDto, response))
                .flatMap(this::saveWebhook)
                .then();
    }

    private WebhookEntity toWebhookEntity(TransactionEntity transactionEntity, WebhookDto webhookDto, HttpResponse httpResponse) {
        return WebhookEntity.builder()
                .transactionId(transactionEntity.getTransactionId())
                .notificationUrl(transactionEntity.getNotificationUrl())
                .attempt(FIRST_ATTEMPT)
                .request(map2String(webhookDto))
                .response(httpResponse.getBody())
                .responseStatus(httpResponse.getStatusCode().value())
                .toResend(!httpResponse.getStatusCode().is2xxSuccessful() && FIRST_ATTEMPT < maxAttempt)
                .build();
    }

    private String map2String(WebhookDto webhookDto) {
        try {
            return objectMapper.writeValueAsString(webhookDto);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    //////-------------------------------------------  RESEND WEBHOOK  -------------------------------------------//////

    @Override
    @Transactional
    public Mono<Void> resendUndeliveredWebhooks() {
        return findAllUndeliveredWebhooks(webhooksLimit)
                .flatMap(this::resendAndSaveWebhook)
                .doOnComplete(() -> log.info("IN resendUndeliveredWebhooks - Resend undelivered webhooks completed"))
                .then();
    }

    private Flux<WebhookEntity> findAllUndeliveredWebhooks(int limit) {
        return fetchPage(0, limit)
//                .doOnNext(list -> System.out.println("1 Webhook - list.size() = " + list.size())) //TODO del
                .expand(list -> list.isEmpty() ? Mono.empty() : fetchPage(list.size(), limit))
//                .doOnNext(list -> System.out.println("2 Webhook - list.size() = " + list.size())) //TODO del
                .flatMap(Flux::fromIterable);
    }

    private Mono<List<WebhookEntity>> fetchPage(int offset, int limit) {
        return webhookRepository.findAllUndeliveredWebhooks(maxAttempt, limit, offset).collectList();
    }

    private Mono<Void> resendAndSaveWebhook(WebhookEntity webhookEntity) {
        return httpService
                .send(webhookEntity.getRequest(), webhookEntity.getNotificationUrl())
                .onErrorResume(this::defaultHttpResponse)
                .flatMap(response -> mapToWebhookEntityAndSave(webhookEntity, response))
                .flatMap(this::updateOldWebhook)
                .doOnSuccess(empty -> log.info("IN resendAndSaveWebhook - Webhook resent and saved. Attempt={}", webhookEntity.getAttempt()))
                .then();
    }

    private Mono<WebhookEntity> mapToWebhookEntityAndSave(WebhookEntity webhookEntity, HttpResponse httpResponse) {
        return Mono.just(httpResponse)
                .map(response -> enrichWebhookEntity(webhookEntity, response))
                .flatMap(this::saveWebhook);
    }

    private WebhookEntity enrichWebhookEntity(WebhookEntity webhookEntity, HttpResponse httpResponse) {
        int attempt = webhookEntity.getAttempt() + 1;

        webhookEntity.setId(webhookEntity.getId());
        webhookEntity.setResponse(httpResponse.getBody());
        webhookEntity.setResponseStatus(httpResponse.getStatusCode().value());
        webhookEntity.setToResend(!httpResponse.getStatusCode().is2xxSuccessful() && attempt < maxAttempt);
        webhookEntity.setAttempt(attempt);

        return webhookEntity;
    }

    private Mono<Void> updateOldWebhook(WebhookEntity webhookEntity) {
        return webhookRepository.updateWebhook(webhookEntity.getId());
    }

    //////-------------------------------------------  COMMON METHODS  -------------------------------------------//////

    /**
     * @return Mock with an error about what happened
     * @apiNote This is the case when the merchant's server doesn't respond
     */
    private Mono<HttpResponse> defaultHttpResponse(Throwable e) {
        log.error("IN sendAndSaveWebhook - Error during HTTP request to merchant: {}", e.getMessage());
        return Mono.just(HttpResponse.builder()
                .body(String.format(ERROR_MSG, e.getMessage())) //TODO Если сервак мерча лежит, то такой вариант сойдёт?
                .statusCode(HttpStatusCode.valueOf(500))
                .build());
    }

    private Mono<WebhookEntity> saveWebhook(WebhookEntity webhookEntity) {
        return webhookRepository
                .saveWebhook(
                        webhookEntity.getTransactionId(),
                        webhookEntity.getNotificationUrl(),
                        webhookEntity.getAttempt(),
                        webhookEntity.getRequest(),
                        webhookEntity.getResponse(),
                        webhookEntity.getResponseStatus(),
                        webhookEntity.getToResend())
                .then(Mono.just(webhookEntity));
    }
}
