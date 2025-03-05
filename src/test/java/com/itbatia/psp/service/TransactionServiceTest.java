package com.itbatia.psp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.entity.AccountEntity;
import com.itbatia.psp.entity.CardEntity;
import com.itbatia.psp.entity.CustomerEntity;
import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranType;
import com.itbatia.psp.exception.AccountNotFoundException;
import com.itbatia.psp.exception.CardNotFoundException;
import com.itbatia.psp.exception.CustomerNotFoundException;
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
import static org.mockito.Mockito.*;

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
    private static final BigDecimal TRANSACTION_AMOUNT_100 = BigDecimal.valueOf(100);
    private static final BigDecimal BALANCE_AFTER_900 = BALANCE_BEFORE_1000.subtract(TRANSACTION_AMOUNT_100);

//    @BeforeEach
//    void setUp() {
//    }

//    @AfterEach
//    void tearDown() throws Exception {
//    }

    @Test
    @DisplayName("Test create topup transaction functionality")
    void givenTransactionDtoAndMerchantId_whenCreateTopupTransaction_thenSuccessfulResponseReturned() throws JsonProcessingException {
        //given
        CardEntity cardEntity = CardDataUtils.getIvanIvanovCardPersisted();
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
        verify(customerService).findByFirstNameAndLastNameAndCountry(CustomerDataUtils.IVANOV_FIRST_NAME, CustomerDataUtils.IVANOV_LAST_NAME, CustomerDataUtils.IVANOV_COUNTRY);
        verify(customerService, times(1)).findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString());
        verify(accountService).findByUserIdAndCurrency(UserDataUtils.CUSTOMER_USER_ID, transactionDto.getCurrency());
        verify(accountService).findByUserIdAndCurrency(UserDataUtils.MERCHANT_USER_ID, transactionDto.getCurrency());
        verify(accountService, times(2)).findByUserIdAndCurrency(anyLong(), anyString());
        verify(accountService).update(customerAccountEntityAfterTrans);
        verify(accountService, times(1)).update(any(AccountEntity.class));
        verify(objectMapper).writeValueAsString(transactionDto);
        verify(objectMapper, times(1)).writeValueAsString(any(TransactionDto.class));
        verify(transactionRepository).saveTransaction(
                customerAccountEntityBeforeTrans.getId(),
                merchantAccountEntity.getId(),
                transactionDto.getPaymentMethod(),
                TRANSACTION_AMOUNT_100,
                TranType.TOPUP,
                transactionDto.getNotificationUrl(),
                transactionDto.getLanguage(),
                jsonTransactionDto);
        verify(transactionRepository, times(1)).saveTransaction(anyLong(), anyLong(), any(PaymentMethod.class), any(BigDecimal.class), any(TranType.class), anyString(), anyString(), anyString());
        //TODO Женя: пройти по then
        //TODO Женя: в verify все проверки сводятся, что метод был вызван с определёнными данными и только указанное количество раз. Может что-то ещё надо?
        //TODO Женя: как тесты юзаются перед продом?
    }

    @Test
    @DisplayName("Test create payout transaction functionality")
    void givenTransactionDtoAndMerchantId_whenCreatePayoutTransaction_thenSuccessfulResponseReturned() throws JsonProcessingException {
        //given
        CardEntity cardEntity = CardDataUtils.getIvanIvanovCardPersisted();
        CustomerEntity customerEntity = CustomerDataUtils.getCustomerIvanovPersisted();
        AccountEntity customerAccountEntity = AccountDataUtils.getCustomerIvanovBYNAccountPersisted();
        AccountEntity merchantAccountEntityBeforeTrans = AccountDataUtils.getMerchantSmirnovBYNAccountPersisted(BALANCE_BEFORE_1000);
        AccountEntity merchantAccountEntityAfterTrans = AccountDataUtils.getMerchantSmirnovBYNAccountPersisted(BALANCE_AFTER_900);
        TransactionDto transactionDto = TransactionDataUtils.getIvanovPayoutTransactionTransient();
        String jsonTransactionDto = MapperUtils.toJson(transactionDto);
        TransactionEntity transactionEntity = TransactionDataUtils.getIvanovPayoutTransactionPersisted(jsonTransactionDto);
        Response expectedResult = ResponseToMerchantDataUtils.getSuccessfulResponseToMerchant();

        BDDMockito.given(cardService.findByCardNumber(anyString()))
                .willReturn(Mono.just(cardEntity));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(customerEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.CUSTOMER_USER_ID), anyString()))
                .willReturn(Mono.just(customerAccountEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.MERCHANT_USER_ID), anyString()))
                .willReturn(Mono.just(merchantAccountEntityBeforeTrans));
        BDDMockito.given(accountService.update(any(AccountEntity.class)))
                .willReturn(Mono.just(merchantAccountEntityAfterTrans));
        BDDMockito.given(objectMapper.writeValueAsString(any(TransactionDto.class)))
                .willReturn(jsonTransactionDto);
        BDDMockito.given(transactionRepository.saveTransaction(anyLong(), anyLong(), any(PaymentMethod.class), any(BigDecimal.class), any(TranType.class), anyString(), anyString(), anyString()))
                .willReturn(Mono.just(transactionEntity));

        //when
        Mono<Response> actualResult = transactionServiceUnderTest.create(TranType.PAYOUT, MerchantDataUtils.MERCHANT_SMIRNOV_ID_AS_STRING, transactionDto);

        //then
        StepVerifier.create(actualResult)
                .expectSubscription()
                .expectNext(expectedResult)
                .verifyComplete();
        verify(cardService).findByCardNumber(CardDataUtils.IVANOV_CARD_NUMBER);
        verify(cardService, times(1)).findByCardNumber(anyString());
        verify(customerService).findByFirstNameAndLastNameAndCountry(CustomerDataUtils.IVANOV_FIRST_NAME, CustomerDataUtils.IVANOV_LAST_NAME, CustomerDataUtils.IVANOV_COUNTRY);
        verify(customerService, times(1)).findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString());
        verify(accountService).findByUserIdAndCurrency(UserDataUtils.CUSTOMER_USER_ID, transactionDto.getCurrency());
        verify(accountService).findByUserIdAndCurrency(UserDataUtils.MERCHANT_USER_ID, transactionDto.getCurrency());
        verify(accountService, times(2)).findByUserIdAndCurrency(anyLong(), anyString());
        verify(accountService).update(merchantAccountEntityAfterTrans);
        verify(accountService, times(1)).update(any(AccountEntity.class));
        verify(objectMapper).writeValueAsString(transactionDto);
        verify(objectMapper, times(1)).writeValueAsString(any(TransactionDto.class));
        verify(transactionRepository).saveTransaction(
                merchantAccountEntityBeforeTrans.getId(),
                customerAccountEntity.getId(),
                transactionDto.getPaymentMethod(),
                TRANSACTION_AMOUNT_100,
                TranType.PAYOUT,
                transactionDto.getNotificationUrl(),
                transactionDto.getLanguage(),
                jsonTransactionDto);
        verify(transactionRepository, times(1)).saveTransaction(anyLong(), anyLong(), any(PaymentMethod.class), any(BigDecimal.class), any(TranType.class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Test create topup transaction with incorrect curd number functionality")
    void givenTransactionDtoWithIncorrectCurdNumberAndMerchantId_whenCreateTopupTransaction_thenResponseWithStatusFailedReturned() {
        //given
        CustomerEntity customerEntity = CustomerDataUtils.getCustomerIvanovPersisted();
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionTransient();
        Response expectedResult = ResponseToMerchantDataUtils.getNegativeResponse(ResponseToMerchantDataUtils.INVALID_CARD_DATA);

        BDDMockito.given(cardService.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.error(new CardNotFoundException("Card not found")));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(customerEntity));

        //when
        Mono<Response> actualResult = transactionServiceUnderTest.create(TranType.TOPUP, MerchantDataUtils.MERCHANT_SMIRNOV_ID_AS_STRING, transactionDto);

        //then
        StepVerifier.create(actualResult)
                .expectSubscription()
                .expectNext(expectedResult)
                .verifyComplete();
        verify(accountService, never()).update(any(AccountEntity.class));
        verify(transactionRepository, never()).saveTransaction(anyLong(), anyLong(), any(PaymentMethod.class), any(BigDecimal.class), any(TranType.class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Test create topup transaction with incorrect customer first name functionality")
    void givenTransactionDtoWithIncorrectCustomerFirstName_whenCreateTopupTransaction_thenResponseWithStatusFailedReturned() {
        //given
        CardEntity cardEntity = CardDataUtils.getIvanIvanovCardPersisted();
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionTransient();
        Response expectedResult = ResponseToMerchantDataUtils.getNegativeResponse(ResponseToMerchantDataUtils.INVALID_CUSTOMER_DATA);

        BDDMockito.given(cardService.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.just(cardEntity));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.error(new CustomerNotFoundException("Customer not found")));

        //when
        Mono<Response> actualResult = transactionServiceUnderTest.create(TranType.TOPUP, MerchantDataUtils.MERCHANT_SMIRNOV_ID_AS_STRING, transactionDto);

        //then
        StepVerifier.create(actualResult)
                .expectSubscription()
                .expectNext(expectedResult)
                .verifyComplete();
        verify(accountService, never()).update(any(AccountEntity.class));
        verify(transactionRepository, never()).saveTransaction(anyLong(), anyLong(), any(PaymentMethod.class), any(BigDecimal.class), any(TranType.class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Test create topup transaction with incorrect currency functionality")
    void givenTransactionDtoWithIncorrectCurrency_whenCreateTopupTransaction_thenResponseWithStatusFailedReturned() {
        //given
        CardEntity cardEntity = CardDataUtils.getIvanIvanovCardPersisted();
        CustomerEntity customerEntity = CustomerDataUtils.getCustomerIvanovPersisted();
        AccountEntity merchantAccountEntity = AccountDataUtils.getMerchantSmirnovBYNAccountPersisted();
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionTransient();
        Response expectedResult = ResponseToMerchantDataUtils.getNegativeResponse(ResponseToMerchantDataUtils.INVALID_ACCOUNT_DATA);

        BDDMockito.given(cardService.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.just(cardEntity));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(customerEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.MERCHANT_USER_ID), anyString()))
                .willReturn(Mono.just(merchantAccountEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.CUSTOMER_USER_ID), anyString()))
                .willReturn(Mono.error(new AccountNotFoundException("Account not found")));

        //when
        Mono<Response> actualResult = transactionServiceUnderTest.create(TranType.TOPUP, MerchantDataUtils.MERCHANT_SMIRNOV_ID_AS_STRING, transactionDto);

        //then
        StepVerifier.create(actualResult)
                .expectSubscription()
                .expectNext(expectedResult)
                .verifyComplete();
        verify(accountService, never()).update(any(AccountEntity.class));
        verify(transactionRepository, never()).saveTransaction(anyLong(), anyLong(), any(PaymentMethod.class), any(BigDecimal.class), any(TranType.class), anyString(), anyString(), anyString());
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
