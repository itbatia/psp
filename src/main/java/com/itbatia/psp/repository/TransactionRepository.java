package com.itbatia.psp.repository;

import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.enums.TranType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * @author Batsian_SV
 */
public interface TransactionRepository extends R2dbcRepository<TransactionEntity, String> {

    @Query("INSERT INTO data.transactions (account_id_from, account_id_to, payment_method, amount, type, notification_url, language, request) " +
            "VALUES (:accountIdFrom, :accountIdTo, :paymentMethod, :amount, :tranType, :notificationUrl, :language, :request::jsonb) " +
            "RETURNING *;")
    Mono<TransactionEntity> saveTransaction(Long accountIdFrom, Long accountIdTo, PaymentMethod paymentMethod, BigDecimal amount,
                               TranType tranType, String notificationUrl, String language, String request);

    Mono<Long> countByStatus(TranStatus status);

    @Query("SELECT transaction_id, account_id_from, account_id_to, payment_method, amount, type, " +
            "notification_url, language, status, message, request, created_at, updated_at " +
            "FROM data.transactions AS t " +
            "WHERE t.status = 'IN_PROGRESS' " +
            "LIMIT :limit")
    Flux<TransactionEntity> findAllUnprocessedTransactions(@Param("limit") long limit);

    @Query("UPDATE data.transactions AS t " +
            "SET status = :status, message = :message, updated_at = CURRENT_TIMESTAMP " +
            "WHERE t.transaction_id = :transactionId")
    Mono<Void> updateStatusAndMessage(TranStatus status, String message, String transactionId);

    Flux<TransactionEntity> findAllByAccountIdToAndCreatedAtBetween(Long accountIdTo, OffsetDateTime startDate, OffsetDateTime endDate);

    Flux<TransactionEntity> findAllByAccountIdFromAndCreatedAtBetween(Long accountIdFrom, OffsetDateTime startDate, OffsetDateTime endDate);
}
