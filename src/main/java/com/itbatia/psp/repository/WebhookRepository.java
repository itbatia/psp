package com.itbatia.psp.repository;

import com.itbatia.psp.entity.WebhookEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface WebhookRepository extends R2dbcRepository<WebhookEntity, Long> {

    @Query("SELECT id, transaction_id, notification_url, attempt, request " +
            "FROM data.webhooks AS w " +
            "WHERE w.to_resend = TRUE AND w.attempt < :maxAttempt " +
            "ORDER BY w.id " +
            "LIMIT :limit")
    Flux<WebhookEntity> findAllUndeliveredWebhooks(@Param("maxAttempt") int maxAttempt, @Param("limit") long limit);

    @Query("SELECT COUNT(*) " +
            "FROM data.webhooks AS w " +
            "WHERE w.to_resend = TRUE AND w.attempt < :maxAttempt")
    Mono<Long> getCountUndeliveredWebhooks(@Param("maxAttempt") int maxAttempt);

    @Query("INSERT INTO data.webhooks (transaction_id, notification_url, attempt, request, response, response_status, to_resend) " +
            "VALUES (:transactionId, :notificationUrl, :attempt, :request::jsonb, :response::jsonb, :responseStatus, :toResend)")
    Mono<Void> saveWebhook(String transactionId,
                           String notificationUrl,
                           int attempt,
                           String request,
                           String response,
                           int responseStatus,
                           boolean toResend);

    @Query("UPDATE data.webhooks AS w SET to_resend = FALSE, updated_at = CURRENT_TIMESTAMP WHERE w.id = :id")
    Mono<Void> markOldWebhookAsNotForSending(Long id);
}
