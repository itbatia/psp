package com.itbatia.psp.service;

import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.enums.TranType;
import com.itbatia.psp.model.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * @author Batsian_SV
 */
public interface TransactionService {

    Mono<Response> create(TranType tranType, String merchantId, String userId, TransactionDto dto);

    Mono<TransactionDto> getById(String transactionId);

    Flux<TransactionDto> getAllTransactionsForDays(TranType tranType, Long userId, LocalDate startDate, LocalDate endDate);

    Mono<TransactionEntity> updateStatusAndMessage(TransactionEntity transactionEntity);

    Mono<Long> getTotalElementsByStatus(TranStatus tranStatus);

    Flux<TransactionEntity> findAllUnprocessedTransactions(long limit);
}
