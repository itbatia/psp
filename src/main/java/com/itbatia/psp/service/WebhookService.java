package com.itbatia.psp.service;

import com.itbatia.psp.entity.TransactionEntity;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface WebhookService {

    Mono<Void> sendAndSaveWebhook(TransactionEntity transactionEntity);

    Mono<Void> resendUndeliveredWebhooks();
}
