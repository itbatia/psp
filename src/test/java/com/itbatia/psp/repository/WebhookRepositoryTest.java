package com.itbatia.psp.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itbatia.psp.config.PostgreSQLTestcontainerConfig;
import com.itbatia.psp.entity.AccountEntity;
import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.entity.UserEntity;
import com.itbatia.psp.entity.WebhookEntity;
import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranType;
import com.itbatia.psp.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Batsian_SV
 */
@DataR2dbcTest
@ActiveProfiles("test")
@Import(PostgreSQLTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class WebhookRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WebhookRepository webhookRepositoryUnderTest;

    private String transactionUid;
    private final static int LIMIT = 100;
    private final static String TRANSACTION_UID = TransactionDataUtils.TRANSACTION_UID_1;
    private final static String NOTIFICATION_URL = ConstantUtils.TOPUP_NOTIFICATION_URL;

    private UserEntity savedUserMerchant;
    private UserEntity savedUserCustomer;
    private AccountEntity savedAccountMerchant;
    private AccountEntity savedAccountCustomer;
    private TransactionEntity savedTransactionEntity;

    @BeforeEach
    void setUp() throws JsonProcessingException {

        String jsonRequest = MapperUtils.toJson(WebhookDataUtils.getWebhookDto(TRANSACTION_UID));
        String jsomResponse = MerchantResponseDataUtils.getSuccessfulMerchantResponseBody(TRANSACTION_UID);
        int responseStatusOK = HttpStatus.OK.value();
        int responseStatusERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();

        StepVerifier.create(Mono.zip(
                                userRepository.save(UserDataUtils.getUserMerchantTransient()),
                                userRepository.save(UserDataUtils.getUserMerchantTransient())
                        )
                        .flatMap(tuples -> {
                            savedUserMerchant = tuples.getT1();
                            savedUserCustomer = tuples.getT2();

                            AccountEntity merchantAccountTransient = AccountDataUtils.getMerchantSmirnovBYNAccountTransient();
                            merchantAccountTransient.setUserId(savedUserMerchant.getId());
                            AccountEntity customerAccountTransient = AccountDataUtils.getCustomerAccountTransient();
                            customerAccountTransient.setUserId(savedUserCustomer.getId());

                            return Mono.zip(
                                    accountRepository.save(merchantAccountTransient),
                                    accountRepository.save(customerAccountTransient)
                            );
                        })
                        .flatMap(tuples -> {
                            savedAccountMerchant = tuples.getT1();
                            savedAccountCustomer = tuples.getT2();

                            return transactionRepository.saveTransaction(
                                    savedAccountMerchant.getId(),
                                    savedAccountCustomer.getId(),
                                    PaymentMethod.CARD,
                                    ConstantUtils.AMOUNT_100,
                                    TranType.TOPUP,
                                    ConstantUtils.TOPUP_NOTIFICATION_URL,
                                    ConstantUtils.EN,
                                    ConstantUtils.MOCK_REQUEST
                            );
                        })
                        .doOnSuccess(transactionEntity -> {
                            this.savedTransactionEntity = transactionEntity;
                            transactionUid = transactionEntity.getTransactionId();
                        })
                        .flatMap(transactionEntity -> webhookRepositoryUnderTest.saveWebhook(savedTransactionEntity.getTransactionId(), NOTIFICATION_URL, 1, jsonRequest, jsomResponse, responseStatusERROR, true)
                                .then(webhookRepositoryUnderTest.saveWebhook(savedTransactionEntity.getTransactionId(), NOTIFICATION_URL, 2, jsonRequest, jsomResponse, responseStatusERROR, true))
                                .then(webhookRepositoryUnderTest.saveWebhook(savedTransactionEntity.getTransactionId(), NOTIFICATION_URL, 3, jsonRequest, jsomResponse, responseStatusERROR, true))
                                .then(webhookRepositoryUnderTest.saveWebhook(savedTransactionEntity.getTransactionId(), NOTIFICATION_URL, 1, jsonRequest, jsomResponse, responseStatusOK, false))
                        ))
                .expectSubscription()
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        StepVerifier.create(
                        webhookRepositoryUnderTest.deleteAll()
                                .then(transactionRepository.deleteAll())
                                .then(accountRepository.deleteAll())
                                .then(userRepository.deleteAll())
                )
                .expectSubscription()
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({"2", "3", "4"})
    @DisplayName("Test find all undelivered webhooks functionality")
    void givenMaxAttemptAndLimit_whenFindAllUndeliveredWebhooks_thenWebhooksAreReturned(String maxAttemptForTest) {
        //given
        int maxAttempt = Integer.parseInt(maxAttemptForTest);

        //when
        Flux<WebhookEntity> undeliveredWebhooks = webhookRepositoryUnderTest.findAllUndeliveredWebhooks(maxAttempt, LIMIT);

        //then
        StepVerifier.create(undeliveredWebhooks.count())
                .expectSubscription()
                .expectNextMatches(count -> switch (maxAttempt) {
                    case 2 -> Objects.equals(count, 1L);
                    case 3 -> Objects.equals(count, 2L);
                    case 4 -> Objects.equals(count, 3L);
                    default -> throw new IllegalStateException("Unexpected value: " + maxAttempt);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({"2", "3", "4"})
    @DisplayName("Test get count undelivered webhooks functionality")
    void givenMaxAttempt_whenGetCountUndeliveredWebhooks_thenCountUndeliveredWebhooksIsReturned(String maxAttemptForTest) {
        //given
        int maxAttempt = Integer.parseInt(maxAttemptForTest);

        //when
        Mono<Long> countUndeliveredWebhooks = webhookRepositoryUnderTest.getCountUndeliveredWebhooks(maxAttempt);

        //then
        StepVerifier.create(countUndeliveredWebhooks)
                .expectSubscription()
                .expectNextMatches(count -> switch (maxAttempt) {
                    case 2 -> Objects.equals(count, 1L);
                    case 3 -> Objects.equals(count, 2L);
                    case 4 -> Objects.equals(count, 3L);
                    default -> throw new IllegalStateException("Unexpected value: " + maxAttempt);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test save webhook functionality")
    void givenWebhookData_whenSaveWebhook_thenWebhookIsSaved() throws JsonProcessingException {
        //given
        WebhookEntity webhookTransient = WebhookDataUtils.getWebhookTransient(transactionUid);

        String notificationUrl = webhookTransient.getNotificationUrl();
        int attempt = webhookTransient.getAttempt();
        String request = webhookTransient.getRequest();
        String response = webhookTransient.getResponse();
        int responseStatus = webhookTransient.getResponseStatus();
        boolean toResend = webhookTransient.getToResend();
        int maxAttempt = 3;

        //when
        Mono<Void> savedWebhook = webhookRepositoryUnderTest.saveWebhook(transactionUid, notificationUrl, attempt, request, response, responseStatus, toResend);

        //then
        StepVerifier.create(savedWebhook
                        .then(Mono.defer(() -> webhookRepositoryUnderTest.findAllUndeliveredWebhooks(maxAttempt, LIMIT).count()))
                )
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test mark old webhook as not for sending functionality")
    void givenThreeSavedUndeliveredWebhooks_whenMarkOldWebhookAsNotForSending_thenSetFalseInFieldToResendAndRepositoryWillReturnTwoUndeliveredWebhooks() {
        //given
        List<WebhookEntity> savedUndeliveredWebhooks = webhookRepositoryUnderTest.findAllUndeliveredWebhooks(4, LIMIT).collectList().block();
        assert savedUndeliveredWebhooks != null;
        assertEquals(savedUndeliveredWebhooks.size(), 3);
        WebhookEntity anySavedWebhook = savedUndeliveredWebhooks.getFirst();

        //when
        Mono<Void> empty = webhookRepositoryUnderTest.markOldWebhookAsNotForSending(anySavedWebhook.getId());

        //then
        StepVerifier.create(empty
                        .then(Mono.defer(() -> webhookRepositoryUnderTest.findAllUndeliveredWebhooks(4, LIMIT).count())))
                .expectSubscription()
                .expectNextMatches(count -> Objects.equals(count, 2L))
                .verifyComplete();
    }
}
