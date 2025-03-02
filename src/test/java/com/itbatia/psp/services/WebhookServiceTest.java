package com.itbatia.psp.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.dto.WebhookDto;
import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.entity.WebhookEntity;
import com.itbatia.psp.model.HttpResponse;
import com.itbatia.psp.repository.WebhookRepository;
import com.itbatia.psp.service.HttpService;
import com.itbatia.psp.service.impl.WebhookServiceImpl;
import com.itbatia.psp.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.itbatia.psp.util.WebhookDataUtils.getWebhookDto;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * @author Batsian_SV
 */
@ExtendWith(MockitoExtension.class)
public class WebhookServiceTest {

    @Mock
    private HttpService httpService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private WebhookRepository webhookRepository;

    @InjectMocks
    private WebhookServiceImpl webhookServiceUnderTest;

    private static final int MAX_ATTEMPTS = 3;
    private static final int WEBHOOK_LIMIT = 10;
    private static final String NOTIFICATION_URL = "http://localhost:8081/api/v1/webhooks/topup";

    @BeforeEach
    void setUp() {
        setField(webhookServiceUnderTest, "maxAttempt", MAX_ATTEMPTS);
        setField(webhookServiceUnderTest, "webhooksLimit", WEBHOOK_LIMIT);
    }

    @Test
    @DisplayName("Test send and save webhook functionality")
    public void givenTransactionEntity_whenSendAndSaveWebhook_thenCompletedSuccessfully() throws JsonProcessingException {
        //given
        String transactionId = "12ff5cdd-c4fa-4023-8b48-d3707917e32e";
        WebhookDto webhookDto = getWebhookDto(transactionId);
        String jsonWebhookDto = MapperUtils.toJson(webhookDto);
        HttpResponse merchantResponse = HttpResponseDataUtils.getSuccessfulMerchantResponse(transactionId);
        TransactionDto transactionDto = TransactionDataUtils.getTopupTransactionDtoTransient();
        String jsonTransactionDto = MapperUtils.toJson(transactionDto);
        TransactionEntity transactionEntity = TransactionDataUtils.getTopupTransactionEntityPersisted(jsonTransactionDto);

        BDDMockito.given(httpService.send(any(WebhookDto.class), anyString()))
                .willReturn(Mono.just(merchantResponse));
        BDDMockito.given(objectMapper.readValue(anyString(), eq(TransactionDto.class)))
                .willReturn(transactionDto);
        BDDMockito.given(objectMapper.writeValueAsString(any(WebhookDto.class)))
                .willReturn(jsonWebhookDto);
        BDDMockito.given(webhookRepository.saveWebhook(anyString(), anyString(), anyInt(), anyString(), anyString(), anyInt(), anyBoolean()))
                .willReturn(Mono.empty());

        //when
        Mono<Void> result = webhookServiceUnderTest.sendAndSaveWebhook(transactionEntity);

        //then
        StepVerifier.create(result).verifyComplete();
        verify(httpService).send(webhookDto, NOTIFICATION_URL);
        verify(httpService, times(1)).send(any(WebhookDto.class), anyString());
        verify(objectMapper).readValue(jsonTransactionDto, TransactionDto.class);
        verify(objectMapper, times(1)).readValue(anyString(), eq(TransactionDto.class));
        verify(objectMapper).writeValueAsString(webhookDto);
        verify(objectMapper, times(1)).writeValueAsString(any(WebhookDto.class));
        verify(webhookRepository).saveWebhook(
                eq(transactionId),
                eq(NOTIFICATION_URL),
                eq(1),
                eq(jsonWebhookDto),
                eq(MerchantResponseDataUtils.getSuccessfulMerchantResponseBody(transactionId)),
                eq(HttpStatus.OK.value()),
                eq(false));
        verify(webhookRepository, times(1)).saveWebhook(anyString(), anyString(), anyInt(), anyString(), anyString(), anyInt(), anyBoolean());
    }

    @Test
    @DisplayName("Test resend undelivered webhooks functionality")
    public void givenUndeliveredWebhooks_whenResendUndeliveredWebhooks_thenCompletedSuccessfully() throws JsonProcessingException {
        //given
        String transactionId1 = "23c3050f-9339-4b3f-a394-2edce4eda242";
        String transactionId2 = "34656365-4a75-4123-ac06-2ac1c0a5c16f";
        String jsonWebhookDto1 = MapperUtils.toJson(getWebhookDto(transactionId1));
        String jsonWebhookDto2 = MapperUtils.toJson(getWebhookDto(transactionId2));
        WebhookEntity webhookPersisted1 = WebhookDataUtils.getWebhookPersisted1(transactionId1, jsonWebhookDto1);
        WebhookEntity webhookPersisted2 = WebhookDataUtils.getWebhookPersisted2(transactionId2, jsonWebhookDto2);
        HttpResponse successMerchantResponse = HttpResponseDataUtils.getSuccessfulMerchantResponse(transactionId1);
        HttpResponse failedMerchantResponse = HttpResponseDataUtils.getFailedMerchantResponse(transactionId2);

        BDDMockito.given(webhookRepository.getCountUndeliveredWebhooks(anyInt()))
                .willReturn(Mono.just(2L));
        BDDMockito.given(webhookRepository.findAllUndeliveredWebhooks(anyInt(), anyLong()))
                .willReturn(Flux.just(webhookPersisted1, webhookPersisted2));
        BDDMockito.given(httpService.send(eq(jsonWebhookDto1), anyString()))
                .willReturn(Mono.just(successMerchantResponse));
        BDDMockito.given(httpService.send(eq(jsonWebhookDto2), anyString()))
                .willReturn(Mono.just(failedMerchantResponse));
        BDDMockito.given(webhookRepository.saveWebhook(anyString(), anyString(), anyInt(), anyString(), anyString(), anyInt(), anyBoolean()))
                .willReturn(Mono.empty());
        BDDMockito.given(webhookRepository.markOldWebhookAsNotForSending(anyLong()))
                .willReturn(Mono.empty());
        //when
        Mono<Void> result = webhookServiceUnderTest.resendUndeliveredWebhooks();

        //then
        StepVerifier.create(result).verifyComplete();
        verify(webhookRepository).findAllUndeliveredWebhooks(MAX_ATTEMPTS, WEBHOOK_LIMIT);
        verify(webhookRepository, times(1)).findAllUndeliveredWebhooks(anyInt(), anyLong());
        verify(httpService).send(jsonWebhookDto1, NOTIFICATION_URL);
        verify(httpService).send(jsonWebhookDto2, NOTIFICATION_URL);
        verify(httpService, times(2)).send(anyString(), anyString());
        verify(webhookRepository).saveWebhook(
                eq(transactionId1),
                eq(NOTIFICATION_URL),
                eq(2),
                eq(jsonWebhookDto1),
                eq(MerchantResponseDataUtils.getSuccessfulMerchantResponseBody(transactionId1)),
                eq(HttpStatus.OK.value()),
                eq(false));
        verify(webhookRepository).saveWebhook(
                eq(transactionId2),
                eq(NOTIFICATION_URL),
                eq(2),
                eq(jsonWebhookDto2),
                eq(MerchantResponseDataUtils.getFailedMerchantResponseBody(transactionId2)),
                eq(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                eq(true));
        verify(webhookRepository, times(2)).saveWebhook(anyString(), anyString(), anyInt(), anyString(), anyString(), anyInt(), anyBoolean());
        verify(webhookRepository).markOldWebhookAsNotForSending(webhookPersisted1.getId());
        verify(webhookRepository).markOldWebhookAsNotForSending(webhookPersisted2.getId());
        verify(webhookRepository, times(2)).markOldWebhookAsNotForSending(anyLong());
    }
}
