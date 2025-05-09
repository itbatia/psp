package com.itbatia.psp.repository;

import com.itbatia.psp.config.PostgreSQLTestcontainerConfig;
import com.itbatia.psp.entity.AccountEntity;
import com.itbatia.psp.entity.CardEntity;
import com.itbatia.psp.util.AccountDataUtils;
import com.itbatia.psp.util.CardDataUtils;
import com.itbatia.psp.util.UserDataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Batsian_SV
 */
@DataR2dbcTest
@ActiveProfiles("test")
@Import(PostgreSQLTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CardRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepositoryUnderTest;

    private Long userId;
    private CardEntity savedCardEntity;

    @BeforeEach
    void setUp() {
        StepVerifier.create(
                        userRepository.save(UserDataUtils.getUserCustomerTransient())
                                .flatMap(userEntity -> {
                                    userId = userEntity.getId();
                                    AccountEntity accountTransient = AccountDataUtils.getCustomerAccountTransient();
                                    accountTransient.setUserId(userId);

                                    return accountRepository.save(accountTransient);
                                })
                                .flatMap(accountEntity -> {
                                    CardEntity cardTransient = CardDataUtils.getIvanIvanovCardTransient();
                                    cardTransient.setAccountId(accountEntity.getId());

                                    return cardRepositoryUnderTest.save(cardTransient);
                                })
                )
                .expectSubscription()
                .consumeNextWith(cardEntity -> savedCardEntity = cardEntity)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        StepVerifier.create(
                        cardRepositoryUnderTest.deleteAll()
                                .then(accountRepository.deleteAll())
                                .then(userRepository.deleteAll())
                )
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("Test find by card number functionality")
    void givenCardNumber_whenFindByCardNumber_thenCardReturned() {
        //given
        String cardNumber = savedCardEntity.getCardNumber();

        //when
        Mono<CardEntity> obtainedCardEntity = cardRepositoryUnderTest.findByCardNumber(cardNumber);

        //then
        StepVerifier.create(obtainedCardEntity.doOnNext(cardEntity -> cardEntity.setCreatedAt(null)))
                .expectSubscription()
                .expectNext(savedCardEntity)
                .verifyComplete();
    }

    /**
     * {@code expectComplete} Проверяем, что поток завершился без эмиссии элементов
     */
    @Test
    @DisplayName("Test find by card incorrect number functionality")
    void givenIncorrectCardNumber_whenFindByCardNumber_thenEmptyReturned() {
        //given
        String incorrectCardNumber = CardDataUtils.PETROV_CARD_NUMBER;

        //when
        Mono<CardEntity> obtainedCardEntity = cardRepositoryUnderTest.findByCardNumber(incorrectCardNumber);

        //then
        StepVerifier.create(obtainedCardEntity)
                .expectSubscription()
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Test find by CardNumber and ExpDate and Cvv functionality")
    void givenCardNumberAndExpDateAndCvv_whenFindByCardNumberAndExpDateAndCvv_thenCardReturned() {
        //given
        String cardNumber = savedCardEntity.getCardNumber();
        String expDate = savedCardEntity.getExpDate();
        int cvv = savedCardEntity.getCvv();

        //when
        Mono<CardEntity> obtainedCardEntity = cardRepositoryUnderTest.findByCardNumberAndExpDateAndCvv(cardNumber, expDate, cvv);

        //then
        StepVerifier.create(obtainedCardEntity.doOnNext(cardEntity -> cardEntity.setCreatedAt(null)))
                .expectSubscription()
                .expectNext(savedCardEntity)
                .verifyComplete();
    }

    /**
     * {@code expectComplete} Проверяем, что поток завершился без эмиссии элементов
     */
    @Test
    @DisplayName("Test find by CardNumber and ExpDate and incorrect Cvv functionality")
    void givenCardNumberAndExpDateAndIncorrectCvv_whenFindByCardNumberAndExpDateAndCvv_thenEmptyReturned() {
        //given
        String cardNumber = savedCardEntity.getCardNumber();
        String expDate = savedCardEntity.getExpDate();
        int incorrectCvv = CardDataUtils.PETROV_CARD_CVV;

        //when
        Mono<CardEntity> obtainedCardEntity = cardRepositoryUnderTest.findByCardNumberAndExpDateAndCvv(cardNumber, expDate, incorrectCvv);

        //then
        StepVerifier.create(obtainedCardEntity.doOnNext(cardEntity -> cardEntity.setCreatedAt(null)))
                .expectSubscription()
                .expectComplete()
                .verify();
    }
}
