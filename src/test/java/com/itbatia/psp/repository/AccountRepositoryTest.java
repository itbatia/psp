package com.itbatia.psp.repository;

import com.itbatia.psp.config.PostgreSQLTestcontainerConfig;
import com.itbatia.psp.entity.AccountEntity;
import com.itbatia.psp.entity.UserEntity;
import com.itbatia.psp.util.AccountDataUtils;
import com.itbatia.psp.util.ConstantUtils;
import com.itbatia.psp.util.UserDataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataR2dbcTest
@ActiveProfiles("test")
@Import(PostgreSQLTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AccountRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepositoryUnderTest;

    private UserEntity savedUserEntity;
    private AccountEntity savedBYNAccount;
    private AccountEntity savedRUBAccount;

    @BeforeEach
    void setUp() {
        StepVerifier.create(userRepository.save(UserDataUtils.getUserMerchantTransient())
                        .flatMap(userEntity -> {
                            savedUserEntity = userEntity;
                            AccountEntity bynAccountTransient = AccountDataUtils.getMerchantSmirnovBYNAccountTransient();
                            bynAccountTransient.setUserId(savedUserEntity.getId());
                            return accountRepositoryUnderTest.save(bynAccountTransient);
                        })
                        .flatMap(accountEntity -> {
                            savedBYNAccount = accountEntity;
                            AccountEntity rubAccountTransient = AccountDataUtils.getMerchantSmirnovRUBAccountTransient();
                            rubAccountTransient.setUserId(savedUserEntity.getId());
                            return accountRepositoryUnderTest.save(rubAccountTransient);
                        })
                )
                .expectSubscription()
                .consumeNextWith(accountEntity -> savedRUBAccount = accountEntity)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        StepVerifier.create(
                        accountRepositoryUnderTest.deleteAll()
                                .then(userRepository.deleteAll())
                )
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("Test increase in the account balance functionality")
    void givenAmountToAddToBalance_whenUpBalance_thenRepositoryIsCalledAndBalanceWillBeUpdated() {
        //given
        Long accountId = savedBYNAccount.getId();
        assertNotNull(accountId, "Account ID should not be null");
        BigDecimal delta = ConstantUtils.AMOUNT_100;
        BigDecimal expectedBalance = savedBYNAccount.getBalance().add(delta);

        //when
        Mono<Void> empty = accountRepositoryUnderTest.upBalance(accountId, delta);

        //then
        StepVerifier.create(empty.then(accountRepositoryUnderTest.findById(accountId)))
                .expectSubscription()
                .assertNext(updatedAccount -> assertEquals(expectedBalance, updatedAccount.getBalance(), "Balance should be updated correctly"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Test find account by user id functionality")
    void givenUserId_whenFindByUserId_thenAllAccountsReturned() {
        //given
        assertNotNull(savedUserEntity.getId(), "User ID should not be null");
        long userId = savedUserEntity.getId();

        //when
        Flux<AccountEntity> obtainedAccounts = accountRepositoryUnderTest.findByUserId(userId);

        //then
        StepVerifier.create(obtainedAccounts)
                .expectSubscription()
                .expectNext(savedBYNAccount)
                .expectNext(savedRUBAccount)
                .verifyComplete();
    }

    /**
     * {@code expectComplete} Проверяем, что поток завершился без эмиссии элементов
     */
    @Test
    @DisplayName("Test find account by incorrect user id functionality")
    void givenIncorrectUserId_whenFindByUserId_thenEmptyReturned() {
        //given
        long incorrectUserId = UserDataUtils.CUSTOMER_IVANOV_USER_ID;

        //when
        Flux<AccountEntity> obtainedAccounts = accountRepositoryUnderTest.findByUserId(incorrectUserId);

        //then
        StepVerifier.create(obtainedAccounts)
                .expectSubscription()
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Test find account by user and currency id functionality")
    void givenUserIdAndCurrency_whenFindByUserIdAndCurrency_thenTargetAccountReturned() {
        //given
        assertNotNull(savedUserEntity.getId(), "User ID should not be null");
        long userId = savedUserEntity.getId();
        String currency = savedBYNAccount.getCurrency();

        //when
        Mono<AccountEntity> obtainedAccount = accountRepositoryUnderTest.findByUserIdAndCurrency(userId, currency);

        //then
        StepVerifier.create(obtainedAccount)
                .expectSubscription()
                .expectNext(savedBYNAccount)
                .verifyComplete();
    }

    /**
     * {@code expectComplete} Проверяем, что поток завершился без эмиссии элементов
     */
    @Test
    @DisplayName("Test find account by user and incorrect currency id functionality")
    void givenUserIdAndIncorrectCurrency_whenFindByUserIdAndCurrency_thenEmptyReturned() {
        //given
        String incorrectCurrency = ConstantUtils.USD;

        //when
        Mono<AccountEntity> obtainedAccount = accountRepositoryUnderTest.findByUserIdAndCurrency(savedBYNAccount.getUserId(), incorrectCurrency);

        //then
        StepVerifier.create(obtainedAccount)
                .expectSubscription()
                .expectComplete()
                .verify();
    }
}
