package com.itbatia.psp.it;

import com.itbatia.psp.config.PostgreSQLTestcontainerConfig;
import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.entity.*;
import com.itbatia.psp.enums.TranType;
import com.itbatia.psp.repository.*;
import com.itbatia.psp.service.TransactionService;
import com.itbatia.psp.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.itbatia.psp.util.AuthDataUtils.getMerchantBasicAuth;
import static com.itbatia.psp.util.ConstantUtils.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Batsian_SV
 */
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(PostgreSQLTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    @Autowired
    private TransactionService transactionService;

    @BeforeEach
    public void setUp() {
        StepVerifier.create(transactionRepository.deleteAll()
                        .then(cardRepository.deleteAll())
                        .then(accountRepository.deleteAll())
                        .then(customerRepository.deleteAll())
                        .then(merchantRepository.deleteAll())
                        .then(userRepository.deleteAll())
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("IT: Test create topup transaction with status 201 functionality")
    public void givenTransactionDto_whenCreateTopupTransaction_thenSuccessfulResponseReturned() {
        //given

        //Incoming transaction data
        TransactionDto dto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();

        //Other necessary objects
        String merchantBasicAuth = buildAndSaveAllNecessaryObjects(
                AccountDataUtils.getMerchantSmirnovBYNAccountTransient(),
                AccountDataUtils.getCustomerAccountTransient());

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/api/v1/payments/topup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), TransactionDto.class)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();
        //then
        result.expectStatus().isCreated()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.transactionId").isNotEmpty()
                .jsonPath("$.status").isNotEmpty()
                .jsonPath("$.status").isEqualTo("IN_PROGRESS")
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.message").isEqualTo("OK");
    }

    @Test
    @DisplayName("IT: Test create topup transaction with status 400 functionality")
    public void givenTransactionDtoWithAmountMoreThanBalanceOfCustomer_whenCreateTopupTransaction_thenFailedResponseReturned() {
        //given

        //Incoming transaction data with amount more than balance of customer
        TransactionDto dto = TransactionDataUtils.getIvanovTopupTransactionDtoIN(ConstantUtils.AMOUNT_100);

        //Other necessary objects
        String merchantBasicAuth = buildAndSaveAllNecessaryObjects(
                AccountDataUtils.getMerchantSmirnovBYNAccountTransient(ConstantUtils.BALANCE_1000),
                AccountDataUtils.getCustomerAccountTransient(ConstantUtils.BALANCE_50));

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/api/v1/payments/topup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), TransactionDto.class)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.transactionId").doesNotExist()
                .jsonPath("$.status").isEqualTo("FAILED")
                .jsonPath("$.message").isEqualTo(ResponseToMerchantDataUtils.INSUFFICIENT_FUNDS);
    }

    @Test
    @DisplayName("IT: Test create payout transaction with status 201 functionality")
    public void givenTransactionDto_whenCreatePayoutTransaction_thenSuccessfulResponseReturned() {
        //given

        //Incoming transaction data
        TransactionDto dto = TransactionDataUtils.getIvanovPayoutTransactionDtoIN();

        //Other necessary objects
        String merchantBasicAuth = buildAndSaveAllNecessaryObjects(
                AccountDataUtils.getMerchantSmirnovBYNAccountTransient(),
                AccountDataUtils.getCustomerAccountTransient());

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/api/v1/payments/payout")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), TransactionDto.class)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();
        //then
        result.expectStatus().isCreated()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.transactionId").isNotEmpty()
                .jsonPath("$.status").isNotEmpty()
                .jsonPath("$.status").isEqualTo("IN_PROGRESS")
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.message").isEqualTo("OK");
    }

    @Test
    @DisplayName("IT: Test create payout transaction with status 400 functionality")
    public void givenTransactionDtoWithUnknownCustomerAndCardData_whenCreatePayoutTransaction_thenFailedResponseReturned() {
        //given

        //Incoming transaction data with unknown customer and card data
        TransactionDto dto = TransactionDataUtils.getPetrovPayoutTransactionDtoIN();

        //Other necessary objects
        String merchantBasicAuth = buildAndSaveAllNecessaryObjects(
                AccountDataUtils.getMerchantSmirnovBYNAccountTransient(),
                AccountDataUtils.getCustomerAccountTransient());

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/api/v1/payments/payout")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), TransactionDto.class)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.transactionId").doesNotExist()
                .jsonPath("$.status").isEqualTo("FAILED")
                .jsonPath("$.message").isEqualTo(ResponseToMerchantDataUtils.INVALID_CARD_DATA);
    }

    @Test
    @DisplayName("IT: Test get by id topup transaction functionality")
    public void givenTransactionId_whenGetByIdTopupTransaction_thenTransactionReturned() {
        //given

        //Necessary objects
        MerchantEntity merchantEntity = buildAndSaveNecessaryObjects();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);

        //Transaction data
        TransactionDto dto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        String transactionId = saveTransaction(TranType.TOPUP, merchantEntity.getUserId(), dto);

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/payments/topup/{transaction_id}/details", transactionId)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.transaction_id").isEqualTo(transactionId)
                .jsonPath("$.payment_method").isEqualTo(dto.getPaymentMethod())
                .jsonPath("$.amount").isEqualTo(dto.getAmount())
                .jsonPath("$.currency").isEqualTo(dto.getCurrency())
                .jsonPath("$.card_data").isEqualTo(dto.getCardData())
                .jsonPath("$.language").isEqualTo(dto.getLanguage())
                .jsonPath("$.notification_url").isEqualTo(dto.getNotificationUrl())
                .jsonPath("$.customer").isEqualTo(dto.getCustomer())
                .jsonPath("$.status").isEqualTo(ConstantUtils.IN_PROGRESS)
                .jsonPath("$.message").isEqualTo(ConstantUtils.OK);
    }

    @Test
    @DisplayName("IT: Test get by non-existent id topup transaction functionality")
    public void givenNonExistedTransactionId_whenGetByIdTopupTransaction_thenExceptionIsThrown() {
        //given

        //Necessary objects
        MerchantEntity merchantEntity = buildAndSaveNecessaryObjects();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);

        //Transaction data
        TransactionDto dto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        String existingTransactionId = saveTransaction(TranType.TOPUP, merchantEntity.getUserId(), dto);
        String nonExistentTransactionId = TransactionDataUtils.TRANSACTION_UID_1;
        assertNotEquals(existingTransactionId, nonExistentTransactionId, "Transaction IDs must not match");

        String errorMsg = "Transaction by id=" + nonExistentTransactionId + " not found";

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/payments/topup/{transaction_id}/details", nonExistentTransactionId)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo(errorMsg);
    }

    @Test
    @DisplayName("IT: Test get by id payout transaction functionality")
    public void givenTransactionId_whenGetByIdPayoutTransaction_thenTransactionReturned() {
        //given

        //Necessary objects
        MerchantEntity merchantEntity = buildAndSaveNecessaryObjects();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);

        //Transaction data
        TransactionDto dto = TransactionDataUtils.getIvanovPayoutTransactionDtoIN();
        String transactionId = saveTransaction(TranType.PAYOUT, merchantEntity.getUserId(), dto);

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/payments/payout/{transaction_id}/details", transactionId)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.transaction_id").isEqualTo(transactionId)
                .jsonPath("$.payment_method").isEqualTo(dto.getPaymentMethod())
                .jsonPath("$.amount").isEqualTo(dto.getAmount())
                .jsonPath("$.currency").isEqualTo(dto.getCurrency())
                .jsonPath("$.card_data").isEqualTo(dto.getCardData())
                .jsonPath("$.language").isEqualTo(dto.getLanguage())
                .jsonPath("$.notification_url").isEqualTo(dto.getNotificationUrl())
                .jsonPath("$.customer").isEqualTo(dto.getCustomer())
                .jsonPath("$.status").isEqualTo(ConstantUtils.IN_PROGRESS)
                .jsonPath("$.message").isEqualTo(ConstantUtils.OK);
    }

    @Test
    @DisplayName("IT: Test get by non-existent id payout transaction functionality")
    public void givenNonExistedTransactionId_whenGetByIdPayoutTransaction_thenExceptionIsThrown() {
        //given

        //Necessary objects
        MerchantEntity merchantEntity = buildAndSaveNecessaryObjects();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);

        //Transaction data
        TransactionDto dto = TransactionDataUtils.getIvanovPayoutTransactionDtoIN();
        String existingTransactionId = saveTransaction(TranType.PAYOUT, merchantEntity.getUserId(), dto);
        String nonExistentTransactionId = TransactionDataUtils.TRANSACTION_UID_1;
        assertNotEquals(existingTransactionId, nonExistentTransactionId, "Transaction IDs must not match");

        String errorMsg = "Transaction by id=" + nonExistentTransactionId + " not found";

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/payments/topup/{transaction_id}/details", nonExistentTransactionId)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo(errorMsg);
    }

    /**
     * <ol>
     *     Cases:
     *     <ul><tt color='008000'>CASE_1</tt> - with query params: {@code start_date}=today, {@code end_date}=tomorrow - transactions exist</ul>
     *     <ul><tt color='008000'>CASE_2</tt> - without query params: transactions exist</ul>
     *     <ul><tt color='008000'>CASE_3</tt> - with query params: invalid dates - no transactions</ul>
     * </ol>
     */
    @ParameterizedTest
    @CsvSource({CASE_1, CASE_2, CASE_3})
    @DisplayName("IT: Test get all topup transaction for days functionality")
    public void givenListTransactions_whenGetAllTopupTransactionsForDays_thenCurrentTransactionListWillReturned(String testcase) {
        //given
        String queryParams = getQueryParams(testcase);

        int size = switch (testcase) {
            case CASE_1, CASE_2 -> 3;
            case CASE_3 -> 0;
            default -> throw new IllegalStateException("Unexpected value: " + testcase);
        };

        //Necessary objects
        MerchantEntity merchantEntity = buildAndSaveNecessaryObjects();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);
        buildAndSaveFiveTransactions(merchantEntity.getUserId());

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/payments/topup/list" + queryParams)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.size()").isEqualTo(size);
    }

    /**
     * <ol>
     *     Cases:
     *     <ul><tt color='008000'>CASE_1</tt> - with query params: {@code start_date}=today, {@code end_date}=tomorrow - transactions exist</ul>
     *     <ul><tt color='008000'>CASE_2</tt> - without query params: transactions exist</ul>
     *     <ul><tt color='008000'>CASE_3</tt> - with query params: invalid dates - no transactions</ul>
     * </ol>
     */
    @ParameterizedTest
    @CsvSource({CASE_1, CASE_2, CASE_3})
    @DisplayName("IT: Test get all payout transaction for days functionality")
    public void givenListTransactions_whenGetAllPayoutTransactionsForDays_thenCurrentTransactionListWillReturned(String testcase) {
        //given
        String queryParams = getQueryParams(testcase);

        int size = switch (testcase) {
            case CASE_1, CASE_2 -> 2;
            case CASE_3 -> 0;
            default -> throw new IllegalStateException("Unexpected value: " + testcase);
        };

        //Necessary objects
        MerchantEntity merchantEntity = buildAndSaveNecessaryObjects();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);
        buildAndSaveFiveTransactions(merchantEntity.getUserId());

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/payments/payout/list" + queryParams)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.size()").isEqualTo(size);
    }

    private static String getQueryParams(String testcase) {
        boolean noQueryParams = testcase.equals(CASE_2);

        LocalDate actualStartDate = LocalDate.now();
        LocalDate actualEndDate = actualStartDate.plusDays(1);
        LocalDate nonActualStartDate = actualStartDate.minusDays(5);
        LocalDate nonActualEndDate = actualStartDate.minusDays(4);

        return switch (testcase) {
            case CASE_1, CASE_2 -> noQueryParams ? "" : "?start_date=" + actualStartDate + "&end_date=" + actualEndDate;
            case CASE_3 -> "?start_date=" + nonActualStartDate + "&end_date=" + nonActualEndDate;
            default -> throw new IllegalStateException("Unexpected value: " + testcase);
        };
    }

    private void buildAndSaveFiveTransactions(long merchantUserId) {
        TransactionDto topupDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        TransactionDto payOutDto = TransactionDataUtils.getIvanovPayoutTransactionDtoIN();

        saveTransaction(TranType.TOPUP, merchantUserId, topupDto);
        saveTransaction(TranType.TOPUP, merchantUserId, topupDto);
        saveTransaction(TranType.TOPUP, merchantUserId, topupDto);
        saveTransaction(TranType.PAYOUT, merchantUserId, payOutDto);
        saveTransaction(TranType.PAYOUT, merchantUserId, payOutDto);
    }

    private String saveTransaction(TranType tranType, long merchantUserId, TransactionDto dto) {
        return Objects.requireNonNull(transactionService.create(tranType, merchantUserId, dto).block()).getTransactionId();
    }

    private String buildAndSaveAllNecessaryObjects(AccountEntity merchantAccount, AccountEntity customerAccount) {
        MerchantEntity merchantEntity = buildAndSaveNecessaryObjects(merchantAccount, customerAccount);
        return getBasicAuth(merchantEntity);
    }

    private MerchantEntity buildAndSaveNecessaryObjects() {
        return buildAndSaveNecessaryObjects(
                AccountDataUtils.getMerchantSmirnovBYNAccountTransient(),
                AccountDataUtils.getCustomerAccountTransient());
    }

    private MerchantEntity buildAndSaveNecessaryObjects(AccountEntity merchantAccount, AccountEntity customerAccount) {
        //Users
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
        merchantAccount.setUserId(savedUserMerchant.getId());
        customerAccount.setUserId(savedUserCustomer.getId());
        accountRepository.save(merchantAccount).block();
        accountRepository.save(customerAccount).block();

        //Customer card
        CardEntity customerCard = CardDataUtils.getIvanIvanovCardTransient();
        customerCard.setAccountId(customerAccount.getId());
        cardRepository.save(customerCard).block();

        return merchantEntity;
    }

    private String getBasicAuth(MerchantEntity merchantEntity) {
        return getMerchantBasicAuth(merchantEntity);
    }
}
