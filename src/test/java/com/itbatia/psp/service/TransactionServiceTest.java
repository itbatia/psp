package com.itbatia.psp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itbatia.psp.dto.CardDto;
import com.itbatia.psp.dto.CustomerDto;
import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.entity.AccountEntity;
import com.itbatia.psp.entity.CardEntity;
import com.itbatia.psp.entity.CustomerEntity;
import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranType;
import com.itbatia.psp.model.Response;
import com.itbatia.psp.repository.TransactionRepository;
import com.itbatia.psp.service.impl.TransactionServiceImpl;
import com.itbatia.psp.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Batsian_SV
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private CardService cardService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private AccountService accountService;
    @Mock
    private CustomerService customerService;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionServiceUnderTest;

    private static final BigDecimal BALANCE_BEFORE_1000 = BigDecimal.valueOf(1000);
    private static final BigDecimal BALANCE_AFTER_900 = BigDecimal.valueOf(900);

//    @BeforeEach
//    void setUp() {
//    }

//    @AfterEach
//    void tearDown() throws Exception {
//    }

    @Test
    @DisplayName("Test create topup transaction functionality")
    void givenTransactionDtoAndMerchantId_whenCreateTransaction_thenSuccessfulResponseReturned() throws JsonProcessingException {
        //given
        CardDto cardDto = CardDataUtils.getIvanIvanovCardDto();
        CardEntity cardEntity = CardDataUtils.getIvanIvanovCardPersisted();
        CustomerDto customerDto = CustomerDataUtils.getCustomerIvanovDto();
        CustomerEntity customerEntity = CustomerDataUtils.getCustomerIvanovPersisted();
        AccountEntity customerAccountEntityBeforeTrans = AccountDataUtils.getCustomerIvanovBYNAccountPersisted(BALANCE_BEFORE_1000);
        AccountEntity customerAccountEntityAfterTrans = AccountDataUtils.getCustomerIvanovBYNAccountPersisted(BALANCE_AFTER_900);
        AccountEntity merchantAccountEntity = AccountDataUtils.getMerchantSmirnovBYNAccountPersisted();
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionTransient();
        String jsonTransactionDto = MapperUtils.toJson(transactionDto);
        TransactionEntity transactionEntity = TransactionDataUtils.getIvanovTopupTransactionPersisted(jsonTransactionDto);
        Response expectedResult = ResponseToMerchantDataUtils.getSuccessfulResponseToMerchant();

        BDDMockito.given(cardService.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.just(cardEntity));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(customerEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.CUSTOMER_USER_ID), anyString()))
                .willReturn(Mono.just(customerAccountEntityBeforeTrans));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.MERCHANT_USER_ID), anyString()))
                .willReturn(Mono.just(merchantAccountEntity));
        BDDMockito.given(accountService.update(any(AccountEntity.class)))
                .willReturn(Mono.just(customerAccountEntityAfterTrans));
        BDDMockito.given(objectMapper.writeValueAsString(any(TransactionDto.class)))
                .willReturn(jsonTransactionDto);
        BDDMockito.given(transactionRepository.saveTransaction(anyLong(), anyLong(), any(PaymentMethod.class), any(BigDecimal.class), any(TranType.class), anyString(), anyString(), anyString()))
                .willReturn(Mono.just(transactionEntity));

        //when
        Mono<Response> actualResult = transactionServiceUnderTest.create(TranType.TOPUP, MerchantDataUtils.MERCHANT_SMIRNOV_ID_AS_STRING, transactionDto);

        //then
        StepVerifier.create(actualResult)
                .expectSubscription()
                .expectNext(expectedResult)
                .verifyComplete();
        verify(cardService).findByCardNumberAndExpDateAndCvv(CardDataUtils.IVANOV_CARD_NUMBER, CardDataUtils.IVANOV_CARD_EXP_DATE, CardDataUtils.IVANOV_CARD_CVV);
        verify(cardService, times(1)).findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt());

    }

//    @Test
//    @DisplayName("Test create payout transaction functionality")
//    void givenTransactionEntity_whenSendAndSaveWebhook_thenCompletedSuccessfully() {
//
//    }
//
//    @Test
//    @DisplayName("Test functionality")
//    void getById() {
//    }
//
//    @Test
//    @DisplayName("Test functionality")
//    void getAllTransactionsForDays() {
//    }
//
//    @Test
//    @DisplayName("Test functionality")
//    void getTotalElementsByStatus() {
//    }
//
//    @Test
//    @DisplayName("Test functionality")
//    void findAllUnprocessedTransactions() {
//    }
//
//    @Test
//    @DisplayName("Test functionality")
//    void updateStatusAndMessage() {
//    }
}