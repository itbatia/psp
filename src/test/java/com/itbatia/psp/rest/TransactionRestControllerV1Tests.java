package com.itbatia.psp.rest;

import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.entity.*;
import com.itbatia.psp.enums.TranType;
import com.itbatia.psp.exception.TransactionNotFoundException;
import com.itbatia.psp.model.Response;
import com.itbatia.psp.service.MerchantService;
import com.itbatia.psp.service.TransactionService;
import com.itbatia.psp.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.itbatia.psp.util.AuthDataUtils.getMerchantBasicAuth;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Batsian_SV
 */
@ExtendWith(SpringExtension.class)
@ComponentScan({"com.itbatia.psp.security"})
@WebFluxTest(controllers = {TransactionRestControllerV1.class})
public class TransactionRestControllerV1Tests {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private MerchantService merchantService;
    @MockitoBean
    private TransactionService transactionService;

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    @DisplayName("Test create topup transaction with status 201 functionality")
    public void givenTransactionDto_whenCreateTopupTransaction_thenSuccessfulResponseReturned() {
        //given
        MerchantEntity merchantEntity = MerchantDataUtils.getMerchantSmirnovPersisted();
        TransactionDto dto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);
        Response responseToMerchant = ResponseToMerchantDataUtils.getSuccessfulResponseToMerchant();

        BDDMockito.given(merchantService.findByApiId(anyString()))
                .willReturn(Mono.just(merchantEntity));
        BDDMockito.given(transactionService.create(eq(TranType.TOPUP), anyLong(), any(TransactionDto.class)))
                .willReturn(Mono.just(responseToMerchant));

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
                .jsonPath("$.transactionId").isEqualTo(TransactionDataUtils.TRANSACTION_UID_1)
                .jsonPath("$.status").isEqualTo(ConstantUtils.IN_PROGRESS)
                .jsonPath("$.message").isEqualTo(ConstantUtils.OK);
    }

    @Test
    @DisplayName("Test create topup transaction with status 400 functionality")
    public void givenTransactionDto_whenCreateTopupTransaction_thenFailedResponseReturned() {
        //given
        MerchantEntity merchantEntity = MerchantDataUtils.getMerchantSmirnovPersisted();
        TransactionDto dto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);
        Response responseToMerchant = ResponseToMerchantDataUtils.getNegativeResponse(ResponseToMerchantDataUtils.INSUFFICIENT_FUNDS);

        BDDMockito.given(merchantService.findByApiId(anyString()))
                .willReturn(Mono.just(merchantEntity));
        BDDMockito.given(transactionService.create(eq(TranType.TOPUP), anyLong(), any(TransactionDto.class)))
                .willReturn(Mono.just(responseToMerchant));

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
                .jsonPath("$.transactionId").isEmpty()
                .jsonPath("$.status").isEqualTo(ConstantUtils.FAILED)
                .jsonPath("$.message").isEqualTo(ResponseToMerchantDataUtils.INSUFFICIENT_FUNDS);
    }

    @Test
    @DisplayName("Test create payout transaction with status 201 functionality")
    public void givenTransactionDto_whenCreatePayoutTransaction_thenSuccessfulResponseReturned() {
        //given
        MerchantEntity merchantEntity = MerchantDataUtils.getMerchantSmirnovPersisted();
        TransactionDto dto = TransactionDataUtils.getIvanovPayoutTransactionDtoIN();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);
        Response responseToMerchant = ResponseToMerchantDataUtils.getSuccessfulResponseToMerchant();

        BDDMockito.given(merchantService.findByApiId(anyString()))
                .willReturn(Mono.just(merchantEntity));
        BDDMockito.given(transactionService.create(eq(TranType.PAYOUT), anyLong(), any(TransactionDto.class)))
                .willReturn(Mono.just(responseToMerchant));

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
                .jsonPath("$.transactionId").isEqualTo(TransactionDataUtils.TRANSACTION_UID_1)
                .jsonPath("$.status").isEqualTo(ConstantUtils.IN_PROGRESS)
                .jsonPath("$.message").isEqualTo(ConstantUtils.OK);
    }

    @Test
    @DisplayName("Test create payout transaction with status 400 functionality")
    public void givenTransactionDtoWithUnknownCustomer_whenCreatePayoutTransaction_thenFailedResponseReturned() {
        //given
        MerchantEntity merchantEntity = MerchantDataUtils.getMerchantSmirnovPersisted();
        TransactionDto dto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);
        Response responseToMerchant = ResponseToMerchantDataUtils.getNegativeResponse(ResponseToMerchantDataUtils.INVALID_CUSTOMER_DATA);

        BDDMockito.given(merchantService.findByApiId(anyString()))
                .willReturn(Mono.just(merchantEntity));
        BDDMockito.given(transactionService.create(eq(TranType.PAYOUT), anyLong(), any(TransactionDto.class)))
                .willReturn(Mono.just(responseToMerchant));

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
                .jsonPath("$.transactionId").isEmpty()
                .jsonPath("$.status").isEqualTo(ConstantUtils.FAILED)
                .jsonPath("$.message").isEqualTo(ResponseToMerchantDataUtils.INVALID_CUSTOMER_DATA);
    }

    @Test
    @DisplayName("Test get by uid topup transaction functionality")
    public void givenTransactionUid_whenGetByIdTopupTransaction_thenTransactionReturned() {
        //given
        MerchantEntity merchantEntity = MerchantDataUtils.getMerchantSmirnovPersisted();
        TransactionDto dto = TransactionDataUtils.getIvanovTopupTransactionDtoOUT();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);

        BDDMockito.given(merchantService.findByApiId(anyString()))
                .willReturn(Mono.just(merchantEntity));
        BDDMockito.given(transactionService.getById(anyString()))
                .willReturn(Mono.just(dto));

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/payments/topup/{transaction_id}/details", TransactionDataUtils.TRANSACTION_UID_1)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.transaction_id").isEqualTo(dto.getTransactionId())
                .jsonPath("$.payment_method").isEqualTo(dto.getPaymentMethod())
                .jsonPath("$.amount").isEqualTo(dto.getAmount())
                .jsonPath("$.currency").isEqualTo(dto.getCurrency())
                .jsonPath("$.card_data").isEqualTo(dto.getCardData())
                .jsonPath("$.language").isEqualTo(dto.getLanguage())
                .jsonPath("$.notification_url").isEqualTo(dto.getNotificationUrl())
                .jsonPath("$.customer").isEqualTo(dto.getCustomer())
                .jsonPath("$.status").isEqualTo(dto.getStatus())
                .jsonPath("$.message").isEqualTo(dto.getMessage());
        verify(transactionService).getById(TransactionDataUtils.TRANSACTION_UID_1);
        verify(transactionService, times(1)).getById(anyString());
    }

    @Test
    @DisplayName("Test get by non-existent uid topup transaction functionality")
    public void givenNonExistedTransactionUid_whenGetByIdTopupTransaction_thenExceptionIsThrown() {
        //given
        MerchantEntity merchantEntity = MerchantDataUtils.getMerchantSmirnovPersisted();
        String errorMsg = "Transaction by id=" + TransactionDataUtils.TRANSACTION_UID_1 + " not found";
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);

        BDDMockito.given(merchantService.findByApiId(anyString()))
                .willReturn(Mono.just(merchantEntity));
        BDDMockito.given(transactionService.getById(anyString()))
                .willThrow(new TransactionNotFoundException(errorMsg));

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/payments/topup/{transaction_id}/details", TransactionDataUtils.TRANSACTION_UID_1)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo(errorMsg);
        verify(transactionService).getById(TransactionDataUtils.TRANSACTION_UID_1);
        verify(transactionService, times(1)).getById(anyString());
    }

    @Test
    @DisplayName("Test get by uid payout transaction functionality")
    public void givenTransactionUid_whenGetByIdPayoutTransaction_thenTransactionReturned() {
        //given
        MerchantEntity merchantEntity = MerchantDataUtils.getMerchantSmirnovPersisted();
        TransactionDto dto = TransactionDataUtils.getIvanovPayoutTransactionDtoOUT();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);

        BDDMockito.given(merchantService.findByApiId(anyString()))
                .willReturn(Mono.just(merchantEntity));
        BDDMockito.given(transactionService.getById(anyString()))
                .willReturn(Mono.just(dto));

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/payments/payout/{transaction_id}/details", TransactionDataUtils.TRANSACTION_UID_1)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.transaction_id").isEqualTo(dto.getTransactionId())
                .jsonPath("$.payment_method").isEqualTo(dto.getPaymentMethod())
                .jsonPath("$.amount").isEqualTo(dto.getAmount())
                .jsonPath("$.currency").isEqualTo(dto.getCurrency())
                .jsonPath("$.card_data").isEqualTo(dto.getCardData())
                .jsonPath("$.language").isEqualTo(dto.getLanguage())
                .jsonPath("$.notification_url").isEqualTo(dto.getNotificationUrl())
                .jsonPath("$.customer").isEqualTo(dto.getCustomer())
                .jsonPath("$.status").isEqualTo(dto.getStatus())
                .jsonPath("$.message").isEqualTo(dto.getMessage());
        verify(transactionService).getById(TransactionDataUtils.TRANSACTION_UID_1);
        verify(transactionService, times(1)).getById(anyString());
    }

    @Test
    @DisplayName("Test get by non-existent uid payout transaction functionality")
    public void givenNonExistedTransactionUid_whenGetByIdPayoutTransaction_thenExceptionIsThrown() {
        //given
        MerchantEntity merchantEntity = MerchantDataUtils.getMerchantSmirnovPersisted();
        String errorMsg = "Transaction by id=" + TransactionDataUtils.TRANSACTION_UID_1 + " not found";
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);

        BDDMockito.given(merchantService.findByApiId(anyString()))
                .willReturn(Mono.just(merchantEntity));
        BDDMockito.given(transactionService.getById(anyString()))
                .willThrow(new TransactionNotFoundException(errorMsg));

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/payments/payout/{transaction_id}/details", TransactionDataUtils.TRANSACTION_UID_1)
                .header(HttpHeaders.AUTHORIZATION, merchantBasicAuth)
                .exchange();

        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo(errorMsg);
        verify(transactionService).getById(TransactionDataUtils.TRANSACTION_UID_1);
        verify(transactionService, times(1)).getById(anyString());
    }

    /**
     * case 1: with query params<br>
     * case 2: without query params
     */
    @ParameterizedTest
    @CsvSource({"2025-04-14", "null"})
    @DisplayName("Test get all topup transaction for days functionality")
    public void givenListTransactions_whenGetAllTopupForDays_thenTransactionsReturned(String date) {
        //given
        boolean noQueryParams = date.equals("null");
        LocalDate startDate = noQueryParams ? null : LocalDate.parse(date, FORMATTER);
        LocalDate endDate = noQueryParams ? null : startDate.plusDays(1);
        String queryParams = noQueryParams ? "" : "?start_date=" + startDate + "&end_date=" + endDate;
        int size = noQueryParams ? 1 : 3;

        MerchantEntity merchantEntity = MerchantDataUtils.getMerchantSmirnovPersisted();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);

        TransactionDto dto1 = TransactionDataUtils.getIvanovTopupTransactionDtoOUT(TransactionDataUtils.TRANSACTION_UID_1);
        TransactionDto dto2 = TransactionDataUtils.getIvanovTopupTransactionDtoOUT(TransactionDataUtils.TRANSACTION_UID_2);
        TransactionDto dto3 = TransactionDataUtils.getIvanovTopupTransactionDtoOUT(TransactionDataUtils.TRANSACTION_UID_3);

        BDDMockito.given(merchantService.findByApiId(anyString()))
                .willReturn(Mono.just(merchantEntity));
        BDDMockito.given(transactionService.getAllTransactionsForDays(eq(TranType.TOPUP), anyLong(), eq(startDate), eq(endDate)))
                .willReturn(Flux.just(dto1, dto2, dto3));
        BDDMockito.given(transactionService.getAllTransactionsForDays(eq(TranType.TOPUP), anyLong(), eq(null), eq(null)))
                .willReturn(Flux.just(dto1));

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
        verify(transactionService).getAllTransactionsForDays(TranType.TOPUP, merchantEntity.getUserId(), startDate, endDate);
        verify(transactionService, times(1)).getAllTransactionsForDays(eq(TranType.TOPUP), anyLong(), any(), any());
    }

    /**
     * case 1: with query params<br>
     * case 2: without query params
     */
    @ParameterizedTest
    @CsvSource({"2025-04-14", "null"})
    @DisplayName("Test get all payout transaction for days functionality")
    public void givenListTransactions_whenGetAllPayoutForDays_thenTransactionsReturned(String date) {
        //given
        boolean noQueryParams = date.equals("null");
        LocalDate startDate = noQueryParams ? null : LocalDate.parse(date, FORMATTER);
        LocalDate endDate = noQueryParams ? null : startDate.plusDays(1);
        String queryParams = noQueryParams ? "" : "?start_date=" + startDate + "&end_date=" + endDate;
        int size = noQueryParams ? 1 : 3;

        MerchantEntity merchantEntity = MerchantDataUtils.getMerchantSmirnovPersisted();
        String merchantBasicAuth = getMerchantBasicAuth(merchantEntity);

        TransactionDto dto1 = TransactionDataUtils.getIvanovPayoutTransactionDtoOUT(TransactionDataUtils.TRANSACTION_UID_1);
        TransactionDto dto2 = TransactionDataUtils.getIvanovPayoutTransactionDtoOUT(TransactionDataUtils.TRANSACTION_UID_2);
        TransactionDto dto3 = TransactionDataUtils.getIvanovPayoutTransactionDtoOUT(TransactionDataUtils.TRANSACTION_UID_3);

        BDDMockito.given(merchantService.findByApiId(anyString()))
                .willReturn(Mono.just(merchantEntity));
        BDDMockito.given(transactionService.getAllTransactionsForDays(eq(TranType.PAYOUT), anyLong(), eq(startDate), eq(endDate)))
                .willReturn(Flux.just(dto1, dto2, dto3));
        BDDMockito.given(transactionService.getAllTransactionsForDays(eq(TranType.PAYOUT), anyLong(), eq(null), eq(null)))
                .willReturn(Flux.just(dto1));

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
        verify(transactionService).getAllTransactionsForDays(TranType.PAYOUT, merchantEntity.getUserId(), startDate, endDate);
        verify(transactionService, times(1)).getAllTransactionsForDays(eq(TranType.PAYOUT), anyLong(), any(), any());
    }
}
