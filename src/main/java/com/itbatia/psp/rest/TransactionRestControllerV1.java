package com.itbatia.psp.rest;

import com.itbatia.psp.dto.TransactionDto;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.enums.TranType;
import com.itbatia.psp.model.Response;
import com.itbatia.psp.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static com.itbatia.psp.Utils.StringPool.*;

/**
 * @author Batsian_SV
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class TransactionRestControllerV1 {

    //TODO Exceptions переписать в handler

    private final TransactionService transactionService;

    @PostMapping("/topup")
    public Mono<ResponseEntity<Response>> createTopup(@RequestHeader(USER_ID) Long merchantUserId,
                                                      @RequestBody TransactionDto dto) {
        return transactionService.create(TranType.TOPUP, merchantUserId, dto).flatMap(this::buildResponse);
    }

    @PostMapping("/payout")
    public Mono<ResponseEntity<Response>> createPayout(@RequestHeader(USER_ID) Long merchantUserId,
                                                       @RequestBody TransactionDto dto) {
        return transactionService.create(TranType.PAYOUT, merchantUserId, dto).flatMap(this::buildResponse);
    }

    @GetMapping("/topup/{transaction_id}/details")
    public Mono<ResponseEntity<TransactionDto>> getByIdTopupTransaction(@PathVariable(TRANSACTION_ID) String transactionId) {
        return transactionService.getById(transactionId)
                .flatMap(dto -> Mono.just(ResponseEntity.status(HttpStatus.OK).body(dto)));
    }

    @GetMapping("/payout/{transaction_id}/details")
    public Mono<ResponseEntity<TransactionDto>> getByIdPayoutTransaction(@PathVariable(TRANSACTION_ID) String transactionId) {
        return transactionService.getById(transactionId)
                .flatMap(dto -> Mono.just(ResponseEntity.status(HttpStatus.OK).body(dto)));
    }

    @GetMapping("/topup/list")
    public Flux<TransactionDto> getAllTopupForDays(@RequestParam(value = START_DATE, required = false) LocalDate startDate,
                                                   @RequestParam(value = END_DATE, required = false) LocalDate endDate,
                                                   @RequestHeader(USER_ID) Long userId) {
        return transactionService.getAllTransactionsForDays(TranType.TOPUP, userId, startDate, endDate);
    }

    @GetMapping("/payout/list")
    public Flux<TransactionDto> getAllPayoutForDays(@RequestParam(value = START_DATE, required = false) LocalDate startDate,
                                                    @RequestParam(value = END_DATE, required = false) LocalDate endDate,
                                                    @RequestHeader(USER_ID) Long userId) {
        return transactionService.getAllTransactionsForDays(TranType.PAYOUT, userId, startDate, endDate);
    }

    private Mono<ResponseEntity<Response>> buildResponse(Response response) {
        if (response.getStatus().equals(TranStatus.IN_PROGRESS))
            return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(response));
        else
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }
}
