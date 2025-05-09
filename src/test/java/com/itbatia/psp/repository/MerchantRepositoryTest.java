package com.itbatia.psp.repository;

import com.itbatia.psp.config.PostgreSQLTestcontainerConfig;
import com.itbatia.psp.entity.MerchantEntity;
import com.itbatia.psp.util.MerchantDataUtils;
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
class MerchantRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepositoryUnderTest;

    private MerchantEntity savedMerchantEntity;

    @BeforeEach
    void setUp() {
        StepVerifier.create(
                        userRepository.save(UserDataUtils.getUserMerchantTransient())
                                .flatMap(userEntity -> {
                                    MerchantEntity merchantTransient = MerchantDataUtils.getMerchantSmirnovTransient();
                                    merchantTransient.setUserId(userEntity.getId());

                                    return merchantRepositoryUnderTest.save(merchantTransient);
                                })
                )
                .expectSubscription()
                .consumeNextWith(merchantEntity -> savedMerchantEntity = merchantEntity)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        StepVerifier.create(
                        merchantRepositoryUnderTest.deleteAll()
                                .then(userRepository.deleteAll())
                )
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("Test find by ApiId functionality")
    void givenApiId_whenFindByApiId_thenMerchantReturned() {
        //given
        String apiId = savedMerchantEntity.getApiId();

        //when
        Mono<MerchantEntity> obtainedMerchantEntity = merchantRepositoryUnderTest.findByApiId(apiId);

        //then
        StepVerifier.create(obtainedMerchantEntity)
                .expectSubscription()
                .expectNext(savedMerchantEntity)
                .verifyComplete();
    }

    /**
     * {@code expectComplete} Проверяем, что поток завершился без эмиссии элементов
     */
    @Test
    @DisplayName("Test find by incorrect ApiId functionality")
    void givenIncorrectApiId_whenFindByApiId_thenMerchantReturned() {
        //given
        String incorrectApiId = MerchantDataUtils.OTHER_MERCHANT_API_ID;

        //when
        Mono<MerchantEntity> obtainedMerchantEntity = merchantRepositoryUnderTest.findByApiId(incorrectApiId);

        //then
        StepVerifier.create(obtainedMerchantEntity)
                .expectSubscription()
                .expectComplete()
                .verify();
    }
}
