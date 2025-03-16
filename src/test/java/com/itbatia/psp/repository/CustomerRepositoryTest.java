package com.itbatia.psp.repository;

import com.itbatia.psp.config.PostgreSQLTestcontainerConfig;
import com.itbatia.psp.entity.CustomerEntity;
import com.itbatia.psp.util.CustomerDataUtils;
import com.itbatia.psp.util.UserDataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@ActiveProfiles("test")
@Import(PostgreSQLTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CustomerRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepositoryUnderTest;

    private Long userId;
    private CustomerEntity savedCustomerEntity;

    @BeforeEach
    void setUp() {
        StepVerifier.create(
                        userRepository.save(UserDataUtils.getUserCustomerTransient())
                                .flatMap(userEntity -> {
                                    userId = userEntity.getId();
                                    CustomerEntity customerTransient = CustomerDataUtils.getCustomerIvanovTransient();
                                    customerTransient.setUserId(userId);

                                    return customerRepositoryUnderTest.save(customerTransient);
                                })
                )
                .expectSubscription()
                .consumeNextWith(customerEntity -> savedCustomerEntity = customerEntity)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        StepVerifier.create(
                        customerRepositoryUnderTest.deleteAll()
                                .then(userRepository.deleteAll())
                )
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("Test find by FirstName and LastName and Country functionality")
    void givenFirstNameAndLastNameAndCountry_whenFindByFirstNameAndLastNameAndCountry_thenCustomerReturned() {
        //given
        String firstName = savedCustomerEntity.getFirstName();
        String lastName = savedCustomerEntity.getLastName();
        String country = savedCustomerEntity.getCountry();

        //when
        Mono<CustomerEntity> obtainedCustomerEntity = customerRepositoryUnderTest.
                findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndCountryIgnoreCase(firstName, lastName, country);

        //then
        StepVerifier.create(obtainedCustomerEntity)
                .expectSubscription()
                .expectNext(savedCustomerEntity)
                .verifyComplete();
    }

    /**
     * {@code expectComplete} Проверяем, что поток завершился без эмиссии элементов
     */
    @Test
    @DisplayName("Test find by FirstName and incorrect LastName and Country functionality")
    void givenFirstNameAndIncorrectLastNameAndCountry_whenFindByFirstNameAndLastNameAndCountry_thenCustomerReturned() {
        //given
        String firstName = savedCustomerEntity.getFirstName();
        String incorrectLastName = CustomerDataUtils.PETROV_LAST_NAME;
        String country = savedCustomerEntity.getCountry();

        //when
        Mono<CustomerEntity> obtainedCustomerEntity = customerRepositoryUnderTest.
                findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndCountryIgnoreCase(firstName, incorrectLastName, country);

        //then
        StepVerifier.create(obtainedCustomerEntity)
                .expectSubscription()
                .expectComplete()
                .verify();
    }
}
