package com.itbatia.psp.config;

import com.itbatia.psp.service.ProcessingService;
import com.itbatia.psp.service.WebhookService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * @author Batsian_SV
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {

    private final WebhookService webhookService;
    private final ProcessingService processingService;

    @Value("${config.processing.enabled}")
    private boolean processingEnabled;

    @Value("${config.processing.interval}")
    private int processingInterval;

    @Value("${config.webhook.enabled}")
    private boolean webhookSendingEnabled;

    @Value("${config.webhook.interval}")
    private int webhookSendingInterval;

    @PostConstruct
    public void startScheduler() {
        transactionsProcessing().subscribe();
//        webhooksResending().subscribe();
    }

    private Flux<Void> transactionsProcessing() {
        return Flux.interval(Duration.ofSeconds(processingInterval), Schedulers.boundedElastic())
                .filter(tick -> processingEnabled)
                .doOnNext(empty -> log.info("IN transactionsProcessing - Transaction processing. Start ..."))
                .flatMap(tick -> processingService.processTransactions());
    }

    private Flux<Void> webhooksResending() {
        return Flux.interval(Duration.ofSeconds(webhookSendingInterval), Schedulers.boundedElastic())
                .filter(tick -> webhookSendingEnabled)
                .doOnNext(empty -> log.info("IN webhooksResending - Resend undelivered webhooks. Start ..."))
                .flatMap(tick -> webhookService.resendUndeliveredWebhooks());
    }
}
