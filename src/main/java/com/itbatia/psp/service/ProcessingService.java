package com.itbatia.psp.service;

import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface ProcessingService {

    Mono<Void> processTransactions();
}
