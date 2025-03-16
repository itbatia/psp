package com.itbatia.psp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.entity.AccountEntity;
import com.itbatia.psp.entity.CardEntity;
import com.itbatia.psp.entity.CustomerEntity;
import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.enums.TranType;
import com.itbatia.psp.exception.AccountNotFoundException;
import com.itbatia.psp.exception.CardNotFoundException;
import com.itbatia.psp.exception.CustomerNotFoundException;
import com.itbatia.psp.exception.TransactionNotFoundException;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

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

    private static final BigDecimal BALANCE_BEFORE_10 = BigDecimal.valueOf(10);
    private static final BigDecimal BALANCE_BEFORE_1000 = BigDecimal.valueOf(1000);
    private static final BigDecimal TRANSACTION_AMOUNT_100 = BigDecimal.valueOf(100);
    private static final BigDecimal BALANCE_AFTER_900 = BALANCE_BEFORE_1000.subtract(TRANSACTION_AMOUNT_100);

    @Test
    @DisplayName("Test create topup transaction functionality")
    void givenTransactionDtoAndMerchantId_whenCreateTopupTransaction_thenSuccessfulResponseReturned() throws JsonProcessingException {
        //given
        CardEntity cardEntity = CardDataUtils.getIvanIvanovCardPersisted();
        CustomerEntity customerEntity = CustomerDataUtils.getCustomerIvanovPersisted();
        AccountEntity customerAccountEntityBeforeTrans = AccountDataUtils.getCustomerIvanovBYNAccountPersisted(BALANCE_BEFORE_1000);
        AccountEntity customerAccountEntityAfterTrans = AccountDataUtils.getCustomerIvanovBYNAccountPersisted(BALANCE_AFTER_900);
        AccountEntity merchantAccountEntity = AccountDataUtils.getMerchantSmirnovBYNAccountPersisted();
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        String jsonTransactionDto = MapperUtils.toJson(transactionDto);
        TransactionEntity transactionEntity = TransactionDataUtils.getIvanovTopupTransactionPersisted(jsonTransactionDto);
        Response expectedResult = ResponseToMerchantDataUtils.getSuccessfulResponseToMerchant();

        BDDMockito.given(cardService.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.just(cardEntity));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(customerEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.CUSTOMER_IVANOV_USER_ID), anyString()))
                .willReturn(Mono.just(customerAccountEntityBeforeTrans));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.MERCHANT_SMIRNOV_USER_ID), anyString()))
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
        StepVerifier.create(actualResult.doOnNext(System.out::println))
                .expectSubscription()
                .expectNext(expectedResult)
                .verifyComplete();
        verify(cardService).findByCardNumberAndExpDateAndCvv(CardDataUtils.IVANOV_CARD_NUMBER, CardDataUtils.IVANOV_CARD_EXP_DATE, CardDataUtils.IVANOV_CARD_CVV);
        verify(cardService, times(1)).findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt());
        verify(customerService).findByFirstNameAndLastNameAndCountry(CustomerDataUtils.IVANOV_FIRST_NAME, CustomerDataUtils.IVANOV_LAST_NAME, CustomerDataUtils.IVANOV_COUNTRY);
        verify(customerService, times(1)).findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString());
        verify(accountService).findByUserIdAndCurrency(UserDataUtils.CUSTOMER_IVANOV_USER_ID, transactionDto.getCurrency());
        verify(accountService).findByUserIdAndCurrency(UserDataUtils.MERCHANT_SMIRNOV_USER_ID, transactionDto.getCurrency());
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
        TransactionDto transactionDto = TransactionDataUtils.getIvanovPayoutTransactionDtoIN();
        String jsonTransactionDto = MapperUtils.toJson(transactionDto);
        TransactionEntity transactionEntity = TransactionDataUtils.getIvanovPayoutTransactionPersisted(jsonTransactionDto);
        Response expectedResult = ResponseToMerchantDataUtils.getSuccessfulResponseToMerchant();

        BDDMockito.given(cardService.findByCardNumber(anyString()))
                .willReturn(Mono.just(cardEntity));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(customerEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.CUSTOMER_IVANOV_USER_ID), anyString()))
                .willReturn(Mono.just(customerAccountEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.MERCHANT_SMIRNOV_USER_ID), anyString()))
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
        StepVerifier.create(actualResult.doOnNext(System.out::println))
                .expectSubscription()
                .expectNext(expectedResult)
                .verifyComplete();
        verify(cardService).findByCardNumber(CardDataUtils.IVANOV_CARD_NUMBER);
        verify(cardService, times(1)).findByCardNumber(anyString());
        verify(customerService).findByFirstNameAndLastNameAndCountry(CustomerDataUtils.IVANOV_FIRST_NAME, CustomerDataUtils.IVANOV_LAST_NAME, CustomerDataUtils.IVANOV_COUNTRY);
        verify(customerService, times(1)).findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString());
        verify(accountService).findByUserIdAndCurrency(UserDataUtils.CUSTOMER_IVANOV_USER_ID, transactionDto.getCurrency());
        verify(accountService).findByUserIdAndCurrency(UserDataUtils.MERCHANT_SMIRNOV_USER_ID, transactionDto.getCurrency());
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
    void givenTransactionDtoWithIncorrectCardNumber_whenCreateTopupTransaction_thenResponseWithStatusFailedReturned() {
        //given
        CustomerEntity customerEntity = CustomerDataUtils.getCustomerIvanovPersisted();
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        Response expectedResult = ResponseToMerchantDataUtils.getNegativeResponse(ResponseToMerchantDataUtils.INVALID_CARD_DATA);

        BDDMockito.given(cardService.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.error(new CardNotFoundException("Card not found")));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(customerEntity));

        //when
        Mono<Response> actualResult = transactionServiceUnderTest.create(TranType.TOPUP, MerchantDataUtils.MERCHANT_SMIRNOV_ID_AS_STRING, transactionDto);

        //then
        StepVerifier.create(actualResult.doOnNext(System.out::println))
                .expectSubscription()
                .expectNext(expectedResult)
                .verifyComplete();
        verify(accountService, never()).update(any(AccountEntity.class));
        verify(transactionRepository, never()).saveTransaction(anyLong(), anyLong(), any(PaymentMethod.class), any(BigDecimal.class), any(TranType.class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Test create topup transaction with blocked card functionality")
    void givenTransactionDtoWithBlockedCard_whenCreateTopupTransaction_thenResponseWithStatusFailedReturned() {
        //given
        CardEntity cardEntity = CardDataUtils.getBlockedIvanIvanovCardPersisted();
        CustomerEntity customerEntity = CustomerDataUtils.getCustomerIvanovPersisted();
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        Response expectedResult = ResponseToMerchantDataUtils.getNegativeResponse(ResponseToMerchantDataUtils.CARD_IS_BLOCKED);

        BDDMockito.given(cardService.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.just(cardEntity));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(customerEntity));

        //when
        Mono<Response> actualResult = transactionServiceUnderTest.create(TranType.TOPUP, MerchantDataUtils.MERCHANT_SMIRNOV_ID_AS_STRING, transactionDto);

        //then
        StepVerifier.create(actualResult.doOnNext(System.out::println))
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
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        Response expectedResult = ResponseToMerchantDataUtils.getNegativeResponse(ResponseToMerchantDataUtils.INVALID_CUSTOMER_DATA);

        BDDMockito.given(cardService.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.just(cardEntity));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.error(new CustomerNotFoundException("Customer not found")));

        //when
        Mono<Response> actualResult = transactionServiceUnderTest.create(TranType.TOPUP, MerchantDataUtils.MERCHANT_SMIRNOV_ID_AS_STRING, transactionDto);

        //then
        StepVerifier.create(actualResult.doOnNext(System.out::println))
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
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        Response expectedResult = ResponseToMerchantDataUtils.getNegativeResponse(ResponseToMerchantDataUtils.INVALID_ACCOUNT_DATA);

        BDDMockito.given(cardService.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.just(cardEntity));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(customerEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.MERCHANT_SMIRNOV_USER_ID), anyString()))
                .willReturn(Mono.just(merchantAccountEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.CUSTOMER_IVANOV_USER_ID), anyString()))
                .willReturn(Mono.error(new AccountNotFoundException("Account not found")));

        //when
        Mono<Response> actualResult = transactionServiceUnderTest.create(TranType.TOPUP, MerchantDataUtils.MERCHANT_SMIRNOV_ID_AS_STRING, transactionDto);

        //then
        StepVerifier.create(actualResult.doOnNext(System.out::println))
                .expectSubscription()
                .expectNext(expectedResult)
                .verifyComplete();
        verify(accountService, never()).update(any(AccountEntity.class));
        verify(transactionRepository, never()).saveTransaction(anyLong(), anyLong(), any(PaymentMethod.class), any(BigDecimal.class), any(TranType.class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Test create topup transaction with incorrect currency functionality")
    void givenTransactionDtoWithCustomerThatIsNotCardOwner_whenCreateTopupTransaction_thenResponseWithStatusFailedReturned() {
        //given
        CardEntity cardEntity = CardDataUtils.getPetrPetrovCardPersisted();
        CustomerEntity customerEntity = CustomerDataUtils.getCustomerIvanovPersisted();
        AccountEntity customerAccountEntity = AccountDataUtils.getCustomerIvanovBYNAccountPersisted();
        AccountEntity merchantAccountEntity = AccountDataUtils.getMerchantSmirnovBYNAccountPersisted();
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        Response expectedResult = ResponseToMerchantDataUtils.getNegativeResponse(ResponseToMerchantDataUtils.CUSTOMER_IS_NOT_OWNER_OF_THIS_CARD);

        BDDMockito.given(cardService.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.just(cardEntity));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(customerEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.CUSTOMER_IVANOV_USER_ID), anyString()))
                .willReturn(Mono.just(customerAccountEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.MERCHANT_SMIRNOV_USER_ID), anyString()))
                .willReturn(Mono.just(merchantAccountEntity));

        //when
        Mono<Response> actualResult = transactionServiceUnderTest.create(TranType.TOPUP, MerchantDataUtils.MERCHANT_SMIRNOV_ID_AS_STRING, transactionDto);

        //then
        StepVerifier.create(actualResult.doOnNext(System.out::println))
                .expectSubscription()
                .expectNext(expectedResult)
                .verifyComplete();
        verify(accountService, never()).update(any(AccountEntity.class));
        verify(transactionRepository, never()).saveTransaction(anyLong(), anyLong(), any(PaymentMethod.class), any(BigDecimal.class), any(TranType.class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Test create topup transaction with customer insufficient funds functionality")
    void givenTransactionDtoWithCustomerInsufficientFunds_whenCreateTopupTransaction_thenResponseWithStatusFailedReturned() {
        //given
        CardEntity cardEntity = CardDataUtils.getIvanIvanovCardPersisted();
        CustomerEntity customerEntity = CustomerDataUtils.getCustomerIvanovPersisted();
        AccountEntity customerAccountEntity = AccountDataUtils.getCustomerIvanovBYNAccountPersisted(BALANCE_BEFORE_10);
        AccountEntity merchantAccountEntity = AccountDataUtils.getMerchantSmirnovBYNAccountPersisted();
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        Response expectedResult = ResponseToMerchantDataUtils.getNegativeResponse(ResponseToMerchantDataUtils.INSUFFICIENT_FUNDS);

        BDDMockito.given(cardService.findByCardNumberAndExpDateAndCvv(anyString(), anyString(), anyInt()))
                .willReturn(Mono.just(cardEntity));
        BDDMockito.given(customerService.findByFirstNameAndLastNameAndCountry(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(customerEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.CUSTOMER_IVANOV_USER_ID), anyString()))
                .willReturn(Mono.just(customerAccountEntity));
        BDDMockito.given(accountService.findByUserIdAndCurrency(eq(UserDataUtils.MERCHANT_SMIRNOV_USER_ID), anyString()))
                .willReturn(Mono.just(merchantAccountEntity));

        //when
        Mono<Response> actualResult = transactionServiceUnderTest.create(TranType.TOPUP, MerchantDataUtils.MERCHANT_SMIRNOV_ID_AS_STRING, transactionDto);

        //then
        StepVerifier.create(actualResult.doOnNext(System.out::println))
                .expectSubscription()
                .expectNext(expectedResult)
                .verifyComplete();
        verify(accountService, never()).update(any(AccountEntity.class));
        verify(transactionRepository, never()).saveTransaction(anyLong(), anyLong(), any(PaymentMethod.class), any(BigDecimal.class), any(TranType.class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Test get topup transaction by uid functionality")
    void givenTransactionUid_whenGetTransactionById_thenTransactionIsReturned() throws JsonProcessingException {
        //given
        TransactionDto transactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        String jsonTransactionDto = MapperUtils.toJson(transactionDto);
        TransactionEntity transactionEntity = TransactionDataUtils.getIvanovTopupTransactionPersisted(jsonTransactionDto);
        String uid = transactionEntity.getTransactionId();

        BDDMockito.given(transactionRepository.findById(anyString()))
                .willReturn(Mono.just(transactionEntity));
        BDDMockito.given(objectMapper.readValue(anyString(), eq(TransactionDto.class)))
                .willReturn(transactionDto);

        //when
        Mono<TransactionDto> actualTransactionDto = transactionServiceUnderTest.getById(uid);

        //then
        TransactionDto expectedTransactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoOUT();
        StepVerifier.create(actualTransactionDto.doOnNext(System.out::println))
                .expectSubscription()
                .expectNext(expectedTransactionDto)
                .verifyComplete();
        verify(objectMapper).readValue(jsonTransactionDto, TransactionDto.class);
        verify(objectMapper, times(1)).readValue(anyString(), eq(TransactionDto.class));
        verify(transactionRepository).findById(uid);
        verify(transactionRepository, times(1)).findById(anyString());
    }

    @Test
    @DisplayName("Test get topup transaction by incorrect uid functionality")
    void givenIncorrectTransactionUid_whenGetTransactionById_thenExceptionIsThrown() {
        //given
        String uid = TransactionDataUtils.TRANSACTION_UID_1;

        BDDMockito.given(transactionRepository.findById(anyString()))
                .willReturn(Mono.empty());

        //when
        Mono<TransactionDto> actualTransactionDto = transactionServiceUnderTest.getById(uid);

        //then
        StepVerifier.create(actualTransactionDto.doOnNext(System.out::println))
                .expectSubscription()
                .expectError(TransactionNotFoundException.class)
                .verify();
        verify(transactionRepository).findById(uid);
        verify(transactionRepository, times(1)).findById(anyString());
    }

    @Test
    @DisplayName("Test get all transactions for days functionality")
    void givenRequestData_whenGetTransactionById_thenExceptionIsThrown() throws JsonProcessingException {
        //given
        TranType tranType = TranType.TOPUP;
        Long userId = UserDataUtils.MERCHANT_SMIRNOV_USER_ID;
        LocalDate startDate = ConstantUtils.START_DATE;
        LocalDate endDate = ConstantUtils.END_DATE;
        AccountEntity merchantBYNAccountEntity = AccountDataUtils.getMerchantSmirnovBYNAccountPersisted();
        AccountEntity merchantRUBAccountEntity = AccountDataUtils.getMerchantSmirnovRUBAccountPersisted();
        TransactionDto ivanovTransactionDto = TransactionDataUtils.getIvanovTopupTransactionDtoIN();
        TransactionDto petrovTransactionDto = TransactionDataUtils.getPetrovTopupTransactionDtoIN();
        String ivanovJsonTransactionDto = MapperUtils.toJson(ivanovTransactionDto);
        String petrovJsonTransactionDto = MapperUtils.toJson(petrovTransactionDto);
        TransactionEntity ivanovTransactionEntity = TransactionDataUtils.getIvanovTopupTransactionPersisted(ivanovJsonTransactionDto);
        TransactionEntity petrovTransactionEntity = TransactionDataUtils.getPetrovTopupTransactionPersisted(petrovJsonTransactionDto);

        TransactionDto ivanovTopupTransactionDtoOUT = TransactionDataUtils.getIvanovTopupTransactionDtoOUT();
        TransactionDto petrovTopupTransactionDtoOUT = TransactionDataUtils.getPetrovTopupTransactionDtoOUT();

        BDDMockito.given(accountService.findByUserId(anyLong()))
                .willReturn(Flux.just(merchantBYNAccountEntity, merchantRUBAccountEntity));
        BDDMockito.given(transactionRepository.findAllByAccountIdToAndCreatedAtBetween(eq(AccountDataUtils.MERCHANT_SMIRNOV_BYN_ACCOUNT_ID), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .willReturn(Flux.just(ivanovTransactionEntity));
        BDDMockito.given(transactionRepository.findAllByAccountIdToAndCreatedAtBetween(eq(AccountDataUtils.MERCHANT_SMIRNOV_RUB_ACCOUNT_ID), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .willReturn(Flux.just(petrovTransactionEntity));
        BDDMockito.given(objectMapper.readValue(eq(ivanovJsonTransactionDto), eq(TransactionDto.class)))
                .willReturn(ivanovTransactionDto);
        BDDMockito.given(objectMapper.readValue(eq(petrovJsonTransactionDto), eq(TransactionDto.class)))
                .willReturn(petrovTransactionDto);

        //when
        Flux<TransactionDto> actualTransactions = transactionServiceUnderTest.getAllTransactionsForDays(tranType, userId, startDate, endDate);

        //then
        StepVerifier.create(actualTransactions.doOnNext(System.out::println))
                .expectSubscription()
                .expectNext(ivanovTopupTransactionDtoOUT, petrovTopupTransactionDtoOUT)
                .verifyComplete();
        verify(accountService).findByUserId(UserDataUtils.MERCHANT_SMIRNOV_USER_ID);
        verify(accountService, times(1)).findByUserId(anyLong());
        verify(transactionRepository).findAllByAccountIdToAndCreatedAtBetween(eq(AccountDataUtils.MERCHANT_SMIRNOV_BYN_ACCOUNT_ID), any(OffsetDateTime.class), any(OffsetDateTime.class));
        verify(transactionRepository).findAllByAccountIdToAndCreatedAtBetween(eq(AccountDataUtils.MERCHANT_SMIRNOV_RUB_ACCOUNT_ID), any(OffsetDateTime.class), any(OffsetDateTime.class));
        verify(transactionRepository, times(2)).findAllByAccountIdToAndCreatedAtBetween(anyLong(), any(OffsetDateTime.class), any(OffsetDateTime.class));
        verify(objectMapper).readValue(eq(ivanovJsonTransactionDto), eq(TransactionDto.class));
        verify(objectMapper).readValue(eq(petrovJsonTransactionDto), eq(TransactionDto.class));
        verify(objectMapper, times(2)).readValue(anyString(), eq(TransactionDto.class));
    }

    @Test
    @DisplayName("Test get total elements by status functionality")
    void givenStatus_whenDetTotalElementsByStatus_thenTotalElementsReturned() {
        //given
        TranStatus tranStatus = TranStatus.IN_PROGRESS;
        long expectedTotalElements = 10;

        BDDMockito.given(transactionRepository.countByStatus(any(TranStatus.class)))
                .willReturn(Mono.just(expectedTotalElements));

        //when
        Mono<Long> actualTotalElements = transactionServiceUnderTest.getTotalElementsByStatus(tranStatus);

        //then
        StepVerifier.create(actualTotalElements.doOnNext(System.out::println))
                .expectSubscription()
                .expectNext(expectedTotalElements)
                .verifyComplete();
        verify(transactionRepository).countByStatus(tranStatus);
        verify(transactionRepository, times(1)).countByStatus(any(TranStatus.class));
    }

    @Test
    @DisplayName("Test find all unprocessed transactions functionality")
    void givenLimit_whenFindAllUnprocessedTransactions_thenUnprocessedTransactionsReturned() throws JsonProcessingException {
        //given
        long limit = 2;
        TransactionEntity ivanovTransactionEntity = TransactionDataUtils.getIvanovTopupTransactionPersisted();
        TransactionEntity petrovTransactionEntity = TransactionDataUtils.getPetrovTopupTransactionPersisted();

        BDDMockito.given(transactionRepository.findAllUnprocessedTransactions(anyLong()))
                .willReturn(Flux.just(ivanovTransactionEntity, petrovTransactionEntity));

        //when
        Flux<TransactionEntity> actualUnprocessedTransactions = transactionServiceUnderTest.findAllUnprocessedTransactions(limit);

        //then
        StepVerifier.create(actualUnprocessedTransactions.doOnNext(System.out::println))
                .expectSubscription()
                .expectNext(ivanovTransactionEntity, petrovTransactionEntity)
                .verifyComplete();
        verify(transactionRepository).findAllUnprocessedTransactions(limit);
        verify(transactionRepository, times(1)).findAllUnprocessedTransactions(anyLong());
    }

    @Test
    @DisplayName("Test update transaction status and message functionality")
    void givenTransactionEntity_whenUpdateStatusAndMessage_thenGivenTransactionEntityReturned() throws JsonProcessingException {
        //given
        TransactionEntity transEntityToUpdate = TransactionDataUtils.getIvanovTopupTransactionPersisted();

        BDDMockito.given(transactionRepository.updateStatusAndMessage(any(TranStatus.class), anyString(), anyString()))
                .willReturn(Mono.empty());

        //when
        Mono<TransactionEntity> actualTransactionEntity = transactionServiceUnderTest.updateStatusAndMessage(transEntityToUpdate);

        //then
        StepVerifier.create(actualTransactionEntity.doOnNext(System.out::println))
                .expectSubscription()
                .expectNext(transEntityToUpdate)
                .verifyComplete();
        verify(transactionRepository).updateStatusAndMessage(transEntityToUpdate.getStatus(), transEntityToUpdate.getMessage(), transEntityToUpdate.getTransactionId());
        verify(transactionRepository, times(1)).updateStatusAndMessage(any(TranStatus.class), anyString(), anyString());
    }
}
