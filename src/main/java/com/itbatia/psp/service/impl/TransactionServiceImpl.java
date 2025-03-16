package com.itbatia.psp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itbatia.psp.dto.*;
import com.itbatia.psp.entity.*;
import com.itbatia.psp.enums.CardStatus;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.enums.TranType;
import com.itbatia.psp.exception.*;
import com.itbatia.psp.model.Response;
import com.itbatia.psp.repository.TransactionRepository;
import com.itbatia.psp.service.AccountService;
import com.itbatia.psp.service.CardService;
import com.itbatia.psp.service.CustomerService;
import com.itbatia.psp.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.*;
import java.util.Objects;

/**
 * @author Batsian_SV
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final CardService cardService;
    private final ObjectMapper objectMapper;
    private final AccountService accountService;
    private final CustomerService customerService;
    private final TransactionRepository transactionRepository;

    //////-----------------------------------------------  CREATE  -----------------------------------------------//////

    @Override
    @Transactional
    public Mono<Response> create(TranType tranType, String merchantId, TransactionDto transactionDto) {

        CardDto card = transactionDto.getCardData();
        CustomerDto customer = transactionDto.getCustomer();
        String currency = transactionDto.getCurrency();

        return Mono.zip(
                        findCard(card, tranType),
                        findCustomer(customer)
                )
                .flatMap(tuples -> {
                    CardEntity cardEntity = tuples.getT1();
                    if (cardEntity.isNew())
                        return Mono.just(Response.build(TranStatus.FAILED, "INVALID_CARD_DATA"));

                    if (!cardEntity.getCardStatus().equals(CardStatus.ACTIVE))
                        return Mono.just(Response.build(TranStatus.FAILED, "CARD_IS_" + cardEntity.getCardStatus()));

                    CustomerEntity customerEntity = tuples.getT2();
                    if (customerEntity.isNew())
                        return Mono.just(Response.build(TranStatus.FAILED, "INVALID_CUSTOMER_DATA"));

                    return Mono.zip(
                                    findCustomerAccount(customerEntity.getUserId(), currency),
                                    findMerchantAccount(merchantId, currency)
                            )
                            .flatMap(accounts -> {
                                AccountEntity customerAccount = accounts.getT1();
                                if (customerAccount.isNew())
                                    return Mono.just(Response.build(TranStatus.FAILED, "CUSTOMER_DOES_NOT_HAVE_ACCOUNT_IN_SPECIFIED_CURRENCY"));

                                AccountEntity merchantAccount = accounts.getT2();
                                if (merchantAccount.isNew())
                                    return Mono.just(Response.build(TranStatus.FAILED, "MERCHANT_DOES_NOT_HAVE_ACCOUNT_IN_SPECIFIED_CURRENCY"));

                                if (!cardEntity.getAccountId().equals(customerAccount.getId()))
                                    return Mono.just(Response.build(TranStatus.FAILED, "CUSTOMER_IS_NOT_OWNER_OF_THIS_CARD"));

                                return processTransaction(tranType, transactionDto, customerAccount, merchantAccount);
                            });
                });
    }

    private Mono<Response> processTransaction(TranType tranType, TransactionDto transactionDto, AccountEntity customerAccount, AccountEntity merchantAccount) {
        BigDecimal amount = transactionDto.getAmount();
        AccountEntity accountFrom = tranType.equals(TranType.TOPUP) ? customerAccount : merchantAccount; // Транзакция пополнения: переводим от кастомера к мерчу
        AccountEntity accountTo = tranType.equals(TranType.TOPUP) ? merchantAccount : customerAccount;   // Транзакция снятия: переводим от мерча к кастомеру

        if (accountFrom.getBalance().compareTo(amount) < 0)
            return Mono.just(Response.build(TranStatus.FAILED, "INSUFFICIENT_FUNDS"));

        accountFrom.setBalance(accountFrom.getBalance().subtract(amount));

        return accountService
                .update(accountFrom)
                .flatMap(updatedAccounts -> saveTransaction(tranType, transactionDto, accountFrom, accountTo)
                        .map(transactionEntity -> Response.build(transactionEntity.getTransactionId(), TranStatus.IN_PROGRESS, "OK"))
                );
    }

    private Mono<TransactionEntity> saveTransaction(TranType tranType, TransactionDto transactionDto, AccountEntity accountFrom, AccountEntity accountTo) {
        return transactionRepository.saveTransaction(
                accountFrom.getId(),
                accountTo.getId(),
                transactionDto.getPaymentMethod(),
                transactionDto.getAmount(),
                tranType,
                transactionDto.getNotificationUrl(),
                transactionDto.getLanguage(),
                toJson(transactionDto)
        );
    }

    private Mono<CardEntity> findCard(CardDto card, TranType tranType) {
        Mono<CardEntity> cardEntity = switch (tranType) {
            case PAYOUT -> cardService.findByCardNumber(card.getCardNumber());
            case TOPUP -> cardService.findByCardNumberAndExpDateAndCvv(card.getCardNumber(), card.getExpDate(), card.getCvv());
        };

        return cardEntity.onErrorResume(CardNotFoundException.class, e -> {
            printLog(e.getMessage());
            return Mono.just(new CardEntity());
        });
    }

    private Mono<CustomerEntity> findCustomer(CustomerDto customer) {
        return customerService.findByFirstNameAndLastNameAndCountry(customer.getFirstName(), customer.getLastName(), customer.getCountry())
                .onErrorResume(CustomerNotFoundException.class, e -> {
                    printLog(e.getMessage());
                    return Mono.just(new CustomerEntity());
                });
    }

    private Mono<AccountEntity> findCustomerAccount(long userId, String currency) {
        return accountService.findByUserIdAndCurrency(userId, currency)
                .onErrorResume(AccountNotFoundException.class, e -> {
                    printLog(e.getMessage());
                    return Mono.just(new AccountEntity());
                });
    }

    private Mono<AccountEntity> findMerchantAccount(String merchantId, String currency) {
        return accountService.findByUserIdAndCurrency(Long.parseLong(merchantId), currency)
                .onErrorResume(AccountNotFoundException.class, e -> {
                    printLog(e.getMessage());
                    return Mono.just(new AccountEntity());
                });
    }

    private void printLog(String errorMsg) {
        log.error("IN create - {}", errorMsg);
    }

    //////---------------------------------------------  GET_BY_ID  ----------------------------------------------//////

    @Override
    public Mono<TransactionDto> getById(String transactionId) {
        return transactionRepository.findById(transactionId)
                .switchIfEmpty(Mono.error(new TransactionNotFoundException("Transaction by id=" + transactionId + " not found")))
                .map(this::toDto);
    }

    //////----------------------------------------------  GET_ALL  -----------------------------------------------//////

    @Override
    public Flux<TransactionDto> getAllTransactionsForDays(TranType tranType, Long userId, LocalDate startDate, LocalDate endDate) {
        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            startDate = LocalDate.now();
            endDate = LocalDate.now();
        }
        OffsetDateTime start = toOffsetDateTime(startDate, LocalTime.MIN);
        OffsetDateTime end = toOffsetDateTime(endDate, LocalTime.MAX);

        return accountService
                .findByUserId(userId)
                .flatMap(accountEntity -> getAllTransByCondition(tranType, accountEntity.getId(), start, end)
                        .map(this::toDto));
    }

    private OffsetDateTime toOffsetDateTime(LocalDate localDate, LocalTime localTime) {
        return OffsetDateTime.of(localDate, localTime, ZoneOffset.UTC);
    }

    private Flux<TransactionEntity> getAllTransByCondition(TranType tranType, Long accountId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return switch (tranType) {
            case TOPUP -> transactionRepository.findAllByAccountIdToAndCreatedAtBetween(accountId, startDate, endDate);
            case PAYOUT -> transactionRepository.findAllByAccountIdFromAndCreatedAtBetween(accountId, startDate, endDate);
        };
    }

    //////---------------------------------------------  PROCESSING  ---------------------------------------------//////

    @Override
    public Mono<Long> getTotalElementsByStatus(TranStatus tranStatus) {
        return transactionRepository.countByStatus(tranStatus);
    }

    @Override
    public Flux<TransactionEntity> findAllUnprocessedTransactions(long limit) {
        return transactionRepository.findAllUnprocessedTransactions(limit);
    }

    //////-----------------------------------------------  UPDATE  -----------------------------------------------//////

    @Override
    @Transactional
    public Mono<TransactionEntity> updateStatusAndMessage(TransactionEntity transaction) {
        return transactionRepository
                .updateStatusAndMessage(transaction.getStatus(), transaction.getMessage(), transaction.getTransactionId())
                .then(Mono.just(transaction));
    }

    //////----------------------------------------------  MAPPERS  -----------------------------------------------//////

    private TransactionDto toDto(TransactionEntity transactionEntity) throws JsonConversionException {
        TransactionDto dto = fromJson(transactionEntity.getRequest());

        dto.setTransactionId(transactionEntity.getTransactionId());
        dto.setStatus(transactionEntity.getStatus());
        dto.setMessage(transactionEntity.getMessage());
        dto.setCreatedAt(transactionEntity.getCreatedAt());
        dto.setUpdatedAt(transactionEntity.getUpdatedAt());

        return dto;
    }

    private String toJson(TransactionDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.error("IN toJson - Error while converting TransactionDto to json. Reason: {}", e.getMessage());
            return "{}";
        }
    }

    private TransactionDto fromJson(String json) throws JsonConversionException {
        try {
            return objectMapper.readValue(json, TransactionDto.class);
        } catch (JsonProcessingException e) {
            log.error("IN fromJson - Error while converting json to TransactionDto. Reason: {}", e.getMessage());
            throw new JsonConversionException("Internal server error");
        }
    }
}
