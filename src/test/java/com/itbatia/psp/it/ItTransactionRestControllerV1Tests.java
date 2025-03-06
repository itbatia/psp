package com.itbatia.psp.it;

import com.itbatia.psp.config.PostgreSQLTestcontainerConfig;
import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.entity.*;
import com.itbatia.psp.repository.*;
import com.itbatia.psp.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static com.itbatia.psp.util.AuthDataUtils.getMerchantBasicAuth;

/**
 * @author Batsian_SV
 */

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(PostgreSQLTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ComponentScan({"com.itbatia.psp"})
public class ItTransactionRestControllerV1Tests {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    public void setUp() {
        StepVerifier.create(transactionRepository.deleteAll())
                .verifyComplete();
    }

    @Test
    @DisplayName("Test create topup transaction functionality")
    public void givenTransactionDto_whenCreateTransaction_thenSuccessResponse() {
        //given

        //Users
        TransactionDto dto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        UserEntity userMerchant = UserDataUtils.getUserMerchantTransient();
        UserEntity userCustomer = UserDataUtils.getUserCustomerTransient();
        UserEntity savedUserMerchant = userRepository.save(userMerchant).block();
        UserEntity savedUserCustomer = userRepository.save(userCustomer).block();

        //Merchant and Customer
        MerchantEntity merchantEntity = MerchantDataUtils.getMerchantSmirnovTransient();
        CustomerEntity customerEntity = CustomerDataUtils.getCustomerIvanovTransient();
        merchantEntity.setUserId(Objects.requireNonNull(savedUserMerchant).getId());
        customerEntity.setUserId(Objects.requireNonNull(savedUserCustomer).getId());
        merchantRepository.save(merchantEntity).block();
        customerRepository.save(customerEntity).block();

        //Accounts
        AccountEntity merchantAccount = AccountDataUtils.getMerchantAccountTransient();
        AccountEntity customerAccount = AccountDataUtils.getCustomerAccountTransient();
        merchantAccount.setUserId(savedUserMerchant.getId());
        customerAccount.setUserId(savedUserCustomer.getId());
        accountRepository.save(merchantAccount).block();
        accountRepository.save(customerAccount).block();

        //Customer card
        CardEntity customerCard = CardDataUtils.getIvanIvanovCardTransient();
        customerCard.setAccountId(customerAccount.getId());
        cardRepository.save(customerCard).block();

        //Merchant auth
        String auth = getMerchantBasicAuth(merchantEntity);

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/api/v1/payments/topup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), TransactionDto.class)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .exchange();
        //then
        result.expectStatus().isCreated()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.status").isNotEmpty()
                .jsonPath("$.status").isEqualTo("IN_PROGRESS")
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.message").isEqualTo("OK");
    }

}
