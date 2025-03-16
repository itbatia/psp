package com.itbatia.psp.repository;

import com.itbatia.psp.config.PostgreSQLTestcontainerConfig;
import com.itbatia.psp.entity.AccountEntity;
import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.entity.UserEntity;
import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.enums.TranType;
import com.itbatia.psp.util.AccountDataUtils;
import com.itbatia.psp.util.ConstantUtils;
import com.itbatia.psp.util.UserDataUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@ActiveProfiles("test")
@Import(PostgreSQLTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TransactionRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepositoryUnderTest;

    private UserEntity savedUserMerchant;
    private UserEntity savedUserCustomer;
    private AccountEntity savedAccountMerchant;
    private AccountEntity savedAccountCustomer;
    private TransactionEntity topupTransactionEntity1;
    private TransactionEntity topupTransactionEntity2;
    private TransactionEntity payoutTransactionEntity;

    @BeforeEach
    void setUp() {
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

                                    return Mono.zip(
                                            transactionRepositoryUnderTest.saveTransaction(
                                                    savedAccountMerchant.getId(),
                                                    savedAccountCustomer.getId(),
                                                    PaymentMethod.CARD,
                                                    ConstantUtils.AMOUNT_100,
                                                    TranType.TOPUP,
                                                    ConstantUtils.TOPUP_NOTIFICATION_URL,
                                                    ConstantUtils.EN,
                                                    ConstantUtils.MOCK_REQUEST
                                            ),
                                            transactionRepositoryUnderTest.saveTransaction(
                                                    savedAccountMerchant.getId(),
                                                    savedAccountCustomer.getId(),
                                                    PaymentMethod.CARD,
                                                    ConstantUtils.AMOUNT_100,
                                                    TranType.TOPUP,
                                                    ConstantUtils.TOPUP_NOTIFICATION_URL,
                                                    ConstantUtils.EN,
                                                    ConstantUtils.MOCK_REQUEST
                                            ),
                                            transactionRepositoryUnderTest.saveTransaction(
                                                    savedAccountCustomer.getId(),
                                                    savedAccountMerchant.getId(),
                                                    PaymentMethod.CARD,
                                                    ConstantUtils.AMOUNT_100,
                                                    TranType.PAYOUT,
                                                    ConstantUtils.PAYOUT_NOTIFICATION_URL,
                                                    ConstantUtils.EN,
                                                    ConstantUtils.MOCK_REQUEST
                                            )
                                    );
                                })
                )
                .expectSubscription()
                .consumeNextWith(tuples -> {
                    topupTransactionEntity1 = tuples.getT1();
                    topupTransactionEntity2 = tuples.getT2();
                    payoutTransactionEntity = tuples.getT3();
                })
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        StepVerifier.create(
                        transactionRepositoryUnderTest.deleteAll()
                                .then(accountRepository.deleteAll())
                                .then(userRepository.deleteAll())
                )
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("Test save transaction functionality")
    void givenTransactionData_whenSaveTransaction_thenTransactionIsCreated() {
        //given
        long accountIdFrom = topupTransactionEntity1.getAccountIdFrom();
        long accountIdTo = topupTransactionEntity1.getAccountIdTo();
        PaymentMethod paymentMethod = topupTransactionEntity1.getPaymentMethod();
        BigDecimal amount = topupTransactionEntity1.getAmount();
        TranType type = topupTransactionEntity1.getType();
        String notificationUrl = topupTransactionEntity1.getNotificationUrl();
        String language = topupTransactionEntity1.getLanguage();
        String request = topupTransactionEntity1.getRequest();

        //when
        Mono<TransactionEntity> transactionEntity = transactionRepositoryUnderTest.saveTransaction(
                accountIdFrom, accountIdTo, paymentMethod, amount, type, notificationUrl, language, request
        );

        //then
        StepVerifier.create(transactionEntity
                        .flatMap(savedTransactionEntity -> {
                            assertNotNull(savedTransactionEntity.getId(), "Transaction ID should not be null");
                            return transactionRepositoryUnderTest.findById(savedTransactionEntity.getId());
                        })
                )
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({"IN_PROGRESS", "SUCCESS", "FAILED"})
    @DisplayName("Test get count transactions by status functionality")
    void givenThreeSavedTransactionsAndStatus_whenCountByStatus_thenActualTotalTransactionsAreReturned(String tranStatus) {
        //given
        TranStatus status = TranStatus.valueOf(tranStatus);

        //when
        Mono<Long> countTrans = transactionRepositoryUnderTest.countByStatus(status);

        //then
        StepVerifier.create(countTrans)
                .expectSubscription()
                .expectNextMatches(count -> switch (status) {
                    case IN_PROGRESS -> Objects.equals(count, 3L);
                    case FAILED, SUCCESS -> Objects.equals(count, 0L);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({"2", "1000"})
    @DisplayName("Test find all unprocessed transactions functionality")
    void givenThreeUnprocessedTransactionsAndLimit_whenFindAllUnprocessedTransactions_thenNoMoreThenLimitTransactionAreReturned(String limitTrans) {
        //given
        int limit = Integer.parseInt(limitTrans);

        //when
        Flux<TransactionEntity> unprocessedTransactions = transactionRepositoryUnderTest.findAllUnprocessedTransactions(limit);

        //then
        StepVerifier.create(unprocessedTransactions.count())
                .expectSubscription()
                .expectNextMatches(count -> switch (limit) {
                    case 2 -> Objects.equals(count, 2L);
                    case 1000 -> Objects.equals(count, 3L);
                    default -> throw new IllegalStateException("Unexpected value: " + limit);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test update Status and Message of a transaction functionality")
    void givenStatusAndMessageAnd_whenUpdateStatusAndMessage_thenTransactionStatusAndMessageAreUpdated() {
        //given
        String transactionId = payoutTransactionEntity.getTransactionId();
        TranStatus currentTranStatus = payoutTransactionEntity.getStatus();
        String currentMessage = payoutTransactionEntity.getMessage();
        TranStatus newTranStatus = TranStatus.SUCCESS;
        String newMessage = ConstantUtils.OK;
        assertNotEquals(currentTranStatus, newTranStatus, "Invalid status to test");
        assertNotEquals(currentMessage, newMessage, "Invalid message to test");

        //when
        Mono<Void> empty = transactionRepositoryUnderTest.updateStatusAndMessage(newTranStatus, newMessage, transactionId);

        //then
        StepVerifier.create(empty.then(transactionRepositoryUnderTest.findById(transactionId)))
                .expectSubscription()
                .expectNextMatches(updatedTransaction ->
                        Objects.equals(updatedTransaction.getStatus(), newTranStatus) &&
                                Objects.equals(updatedTransaction.getMessage(), newMessage))
                .verifyComplete();
    }

    @Test
    @DisplayName("Test find all by AccountIdTo and CreatedAt between DateFrom and DateTo functionality")
    void givenAccountIdToAndDateFromAndDateTo_whenFindAllByAccountIdToAndCreatedAtBetween_thenTransactionAreReturned() {
        //given
        LocalDate dateFrom = LocalDate.now().minusDays(1);
        LocalDate dateTo = LocalDate.now();

        OffsetDateTime start = OffsetDateTime.of(dateFrom, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime end = OffsetDateTime.of(dateTo, LocalTime.MAX, ZoneOffset.UTC);

        long accountIdTo1 = topupTransactionEntity1.getAccountIdTo(); // customer's accountId
        long accountIdTo2 = topupTransactionEntity2.getAccountIdTo(); // customer's accountId
        long accountIdTo3 = payoutTransactionEntity.getAccountIdTo(); // merchant's accountId
        assertEquals(accountIdTo1, accountIdTo2, "Invalid AccountIdTo to test");
        assertNotEquals(accountIdTo1, accountIdTo3, "Invalid AccountIdTo to test");

        //when
        Flux<TransactionEntity> transactionsForCustomer = transactionRepositoryUnderTest.findAllByAccountIdToAndCreatedAtBetween(accountIdTo1, start, end);
        Flux<TransactionEntity> transactionsForMerchant = transactionRepositoryUnderTest.findAllByAccountIdToAndCreatedAtBetween(accountIdTo3, start, end);
        Flux<TransactionEntity> transactionsForEmptyRange = transactionRepositoryUnderTest.findAllByAccountIdToAndCreatedAtBetween(accountIdTo2, start.minusDays(1), end.minusDays(1));

        //then
        StepVerifier.create(transactionsForCustomer)
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
        StepVerifier.create(transactionsForMerchant)
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(transactionsForEmptyRange)
                .expectSubscription()
                .expectComplete()
                .verify();
    }
}
