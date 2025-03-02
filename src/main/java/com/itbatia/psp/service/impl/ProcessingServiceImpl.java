package com.itbatia.psp.service.impl;

import com.itbatia.psp.entity.TransactionEntity;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.model.Pagination;
import com.itbatia.psp.service.AccountService;
import com.itbatia.psp.service.ProcessingService;
import com.itbatia.psp.service.TransactionService;
import com.itbatia.psp.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Random;

/**
 * @author Batsian_SV
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ProcessingServiceImpl implements ProcessingService {

    @Value("${config.processing.limit}")
    private Integer limit;

    @Value("${config.processing.success-rate}")
    private Integer successRate;

    private final Random random = new Random();
    private final WebhookService webhookService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Override
    public Mono<Void> processTransactions() {
        return transactionService.getTotalElementsByStatus(TranStatus.IN_PROGRESS)
                .map(totalElements -> Pagination.init(limit, totalElements))
                .flatMap(this::processPagesRecursively)
                .doOnSuccess(e -> log.info("IN processTransactions - Transaction processing completed"));
    }

    /**
     * Recursive page-based transaction processing
     */
    private Mono<Void> processPagesRecursively(Pagination pagination) {
        return transactionService.findAllUnprocessedTransactions(pagination.getLimit())
                .doOnNext(this::processTransaction)
                .flatMap(this::transferFunds)
                .flatMap(transactionService::updateStatusAndMessage)
                .flatMap(webhookService::sendAndSaveWebhook)
                .then(Mono.defer(() -> {
                    if (pagination.hasNextPage()) {
                        pagination.moveToNextPage();
                        return processPagesRecursively(pagination);
                    }
                    return Mono.empty();
                }));
    }

    private Mono<TransactionEntity> transferFunds(TransactionEntity transactionEntity) {
        return accountService
                .upBalance(getTargetAccountId(transactionEntity), transactionEntity.getAmount())
                .then(Mono.just(transactionEntity));
    }

    private long getTargetAccountId(TransactionEntity trans) {
        return trans.getStatus().equals(TranStatus.SUCCESS) ? trans.getAccountIdTo() : trans.getAccountIdFrom();
    }

    /**
     * @apiNote Simulating the processing is a status update
     */
    private void processTransaction(TransactionEntity transactionEntity) {
        if (random.nextInt(100) < successRate)
            transactionEntity.setStatus(TranStatus.SUCCESS);
        else
            transactionEntity.setStatus(TranStatus.FAILED);

        transactionEntity.setUpdatedAt(OffsetDateTime.now());
    }
}
