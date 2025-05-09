package com.itbatia.psp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.service.impl.ProcessingServiceImpl;
import com.itbatia.psp.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.itbatia.psp.util.ConstantUtils.AMOUNT_100;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * @author Batsian_SV
 */
@ExtendWith(MockitoExtension.class)
public class ProcessingServiceTest {

    @Mock
    private AccountService accountService;
    @Mock
    private WebhookService webhookService;
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ProcessingServiceImpl processingServiceUnderTest;

    private static final int LIMIT = 2;

    @BeforeEach
    void setUp() {
        setField(processingServiceUnderTest, "limit", LIMIT);
    }

    /**
     * {@code StepVerifier.create(empty)} - создает верификатор для потока<br>
     * {@code expectSubscription()} - проверяет, что подписка на поток была успешно создана<br>
     * {@code verifyComplete()} - проверяет, что поток завершился корректно (получен сигнал onComplete).
     *
     * @param successRate Range 0-100 in percent. Example SUCCESS/FAILED: if '5' then 5/95, if '70' then 70/30 etc
     * @throws JsonProcessingException if serialization via {@link org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper ObjectMapper} fails
     */
    @ParameterizedTest
    @CsvSource({"100", "0"})
    @DisplayName("Test process transactions with unsuccessful completed functionality")
    void givenLimitAndSuccessRateIs0_whenProcessTransactionsIsInvoked_thenRepositoryIsCalledWithUpdatedTransStatusOfUnprocessedTransactions(int successRate) throws JsonProcessingException {
        //given
        long totalElements = 2;
        setField(processingServiceUnderTest, "successRate", successRate);

        TransactionEntity inProgressIvanovTransactionEntity = TransactionDataUtils.getIvanovTopupTransactionPersisted(TranStatus.IN_PROGRESS);
        TransactionEntity inProgressPetrovTransactionEntity = TransactionDataUtils.getPetrovTopupTransactionPersisted(TranStatus.IN_PROGRESS);
        TransactionEntity successfulIvanovTransactionEntity = TransactionDataUtils.getIvanovTopupTransactionPersisted(TranStatus.SUCCESS, ConstantUtils.PAYMENT_SUCCESS);
        TransactionEntity successfulPetrovTransactionEntity = TransactionDataUtils.getPetrovTopupTransactionPersisted(TranStatus.SUCCESS, ConstantUtils.PAYMENT_SUCCESS);
        TransactionEntity failedIvanovTransactionEntity = TransactionDataUtils.getIvanovTopupTransactionPersisted(TranStatus.FAILED, ConstantUtils.PAYMENT_FAILED);
        TransactionEntity failedPetrovTransactionEntity = TransactionDataUtils.getPetrovTopupTransactionPersisted(TranStatus.FAILED, ConstantUtils.PAYMENT_FAILED);

        BDDMockito.given(transactionService.getTotalElementsByStatus(any(TranStatus.class)))
                .willReturn(Mono.just(totalElements));
        BDDMockito.given(transactionService.findAllUnprocessedTransactions(anyLong()))
                .willReturn(Flux.just(inProgressIvanovTransactionEntity, inProgressPetrovTransactionEntity));

        BDDMockito.given(accountService.upBalance(anyLong(), any(BigDecimal.class)))
                .willReturn(Mono.empty());

        if (successRate == 100) {
            BDDMockito.given(transactionService.updateStatusAndMessage(eq(successfulIvanovTransactionEntity)))
                    .willReturn(Mono.just(successfulIvanovTransactionEntity));
            BDDMockito.given(transactionService.updateStatusAndMessage(eq(successfulPetrovTransactionEntity)))
                    .willReturn(Mono.just(successfulPetrovTransactionEntity));
        }
        if (successRate == 0) {
            BDDMockito.given(transactionService.updateStatusAndMessage(eq(failedIvanovTransactionEntity)))
                    .willReturn(Mono.just(failedIvanovTransactionEntity));
            BDDMockito.given(transactionService.updateStatusAndMessage(eq(failedPetrovTransactionEntity)))
                    .willReturn(Mono.just(failedPetrovTransactionEntity));
        }

        BDDMockito.given(webhookService.sendAndSaveWebhook(any(TransactionEntity.class)))
                .willReturn(Mono.empty());

        //when
        Mono<Void> empty = processingServiceUnderTest.processTransactions();

        //then
        StepVerifier.create(empty)
                .expectSubscription()
                .verifyComplete();
        verify(transactionService).getTotalElementsByStatus(TranStatus.IN_PROGRESS);
        verify(transactionService, times(1)).getTotalElementsByStatus(any(TranStatus.class));
        verify(transactionService).findAllUnprocessedTransactions(LIMIT);
        verify(transactionService, times(1)).findAllUnprocessedTransactions(anyLong());

        if (successRate == 100) {
            verify(accountService, times(2)).upBalance(eq(AccountDataUtils.MERCHANT_SMIRNOV_BYN_ACCOUNT_ID), eq(AMOUNT_100));
        }
        if (successRate == 0) {
            verify(accountService, times(1)).upBalance(eq(AccountDataUtils.CUSTOMER_IVANOV_BYN_ACCOUNT_ID), eq(AMOUNT_100));
            verify(accountService, times(1)).upBalance(eq(AccountDataUtils.CUSTOMER_PETROV_BYN_ACCOUNT_ID), eq(AMOUNT_100));
        }

        if (successRate == 100) {
            verify(transactionService).updateStatusAndMessage(successfulIvanovTransactionEntity);
            verify(transactionService).updateStatusAndMessage(successfulPetrovTransactionEntity);
        }
        if (successRate == 0) {
            verify(transactionService).updateStatusAndMessage(failedIvanovTransactionEntity);
            verify(transactionService).updateStatusAndMessage(failedPetrovTransactionEntity);
        }
        verify(transactionService, times(2)).updateStatusAndMessage(any(TransactionEntity.class));

        if (successRate == 100) {
            verify(webhookService).sendAndSaveWebhook(successfulIvanovTransactionEntity);
            verify(webhookService).sendAndSaveWebhook(successfulPetrovTransactionEntity);
        }
        if (successRate == 0) {
            verify(webhookService).sendAndSaveWebhook(failedIvanovTransactionEntity);
            verify(webhookService).sendAndSaveWebhook(failedPetrovTransactionEntity);
        }
        verify(webhookService, times(2)).sendAndSaveWebhook(any(TransactionEntity.class));
    }
}
