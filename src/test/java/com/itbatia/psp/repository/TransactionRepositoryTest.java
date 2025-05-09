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

/**
 * @author Batsian_SV
 */
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
    private TransactionEntity payoutTransactionEntity1;
    private TransactionEntity payoutTransactionEntity2;
    private TransactionEntity topupTransactionEntity;

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
                    payoutTransactionEntity1 = tuples.getT1();
                    payoutTransactionEntity2 = tuples.getT2();
                    topupTransactionEntity = tuples.getT3();
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
        long accountIdFrom = payoutTransactionEntity1.getAccountIdFrom();
        long accountIdTo = payoutTransactionEntity1.getAccountIdTo();
        PaymentMethod paymentMethod = payoutTransactionEntity1.getPaymentMethod();
        BigDecimal amount = payoutTransactionEntity1.getAmount();
        TranType type = payoutTransactionEntity1.getType();
        String notificationUrl = payoutTransactionEntity1.getNotificationUrl();
        String language = payoutTransactionEntity1.getLanguage();
        String request = payoutTransactionEntity1.getRequest();

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
        String transactionId = topupTransactionEntity.getTransactionId();
        TranStatus currentTranStatus = topupTransactionEntity.getStatus();
        String currentMessage = topupTransactionEntity.getMessage();
        TranStatus newTranStatus = TranStatus.SUCCESS;
        String newMessage = ConstantUtils.PAYMENT_SUCCESS;
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
    @DisplayName("Test find all transactions by AccountIdTo and CreatedAt between DateFrom and DateTo functionality")
    void givenAccountIdToAndDateFromAndDateTo_whenFindAllByAccountIdToAndCreatedAtBetween_thenTransactionsAreReturned() {
        //given
        LocalDate dateFrom = LocalDate.now().minusDays(1);
        LocalDate dateTo = LocalDate.now();

        OffsetDateTime start = OffsetDateTime.of(dateFrom, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime end = OffsetDateTime.of(dateTo, LocalTime.MAX, ZoneOffset.UTC);

        long accountIdTo1 = payoutTransactionEntity1.getAccountIdTo(); // customer's accountId
        long accountIdTo2 = payoutTransactionEntity2.getAccountIdTo(); // customer's accountId
        long accountIdTo3 = topupTransactionEntity.getAccountIdTo();   // merchant's accountId
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

    @Test
    @DisplayName("Test find all transactions by AccountIdFrom and CreatedAt between DateFrom and DateTo functionality")
    void givenAccountIdFromAndDateFromAndDateTo_whenFindAllByAccountIdFromAndCreatedAtBetween_thenTransactionsAreReturned() {
        //given
        LocalDate dateFrom = LocalDate.now().minusDays(1);
        LocalDate dateTo = LocalDate.now();

        OffsetDateTime start = OffsetDateTime.of(dateFrom, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime end = OffsetDateTime.of(dateTo, LocalTime.MAX, ZoneOffset.UTC);

        long accountIdFrom1 = payoutTransactionEntity1.getAccountIdFrom(); // merchant's accountId
        long accountIdFrom2 = payoutTransactionEntity2.getAccountIdFrom(); // merchant's accountId
        long accountIdFrom3 = topupTransactionEntity.getAccountIdFrom();   // customer's accountId
        assertEquals(accountIdFrom1, accountIdFrom2, "Invalid AccountIdFrom to test");
        assertNotEquals(accountIdFrom1, accountIdFrom3, "Invalid AccountIdFrom to test");

        //when
        Flux<TransactionEntity> payoutTransactions = transactionRepositoryUnderTest.findAllByAccountIdFromAndCreatedAtBetween(accountIdFrom1, start, end);
        Flux<TransactionEntity> topupTransactions = transactionRepositoryUnderTest.findAllByAccountIdFromAndCreatedAtBetween(accountIdFrom3, start, end);
        Flux<TransactionEntity> transactionsForEmptyRange = transactionRepositoryUnderTest.findAllByAccountIdFromAndCreatedAtBetween(accountIdFrom2, start.minusDays(1), end.minusDays(1));

        //then
        StepVerifier.create(payoutTransactions)
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
        StepVerifier.create(topupTransactions)
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(transactionsForEmptyRange)
                .expectSubscription()
                .expectComplete()
                .verify();
    }
}
