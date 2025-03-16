package com.itbatia.psp.service;

import com.itbatia.psp.entity.AccountEntity;
import com.itbatia.psp.exception.AccountNotFoundException;
import com.itbatia.psp.repository.AccountRepository;
import com.itbatia.psp.service.impl.AccountServiceImpl;
import com.itbatia.psp.util.AccountDataUtils;
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
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Batsian_SV
 */
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountServiceUnderTest;

    @Test
    @DisplayName("Test get accounts by userId functionality")
    public void givenUserId_whenGetAccountsByUserId_thenAccountsIsReturned() {
        //given
        BDDMockito.given(accountRepository.findByUserId(anyLong()))
                .willReturn(Flux.just(AccountDataUtils.getMerchantSmirnovBYNAccountPersisted(), AccountDataUtils.getMerchantSmirnovRUBAccountPersisted()));
        //when
        List<AccountEntity> accountEntities = accountServiceUnderTest.findByUserId(1).collectList().block();
        //then
        assertThat(accountEntities).isNotNull();
        assertThat(accountEntities.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Test get accounts by incorrect userId functionality")
    public void givenIncorrectUserId_whenGetAccountsByUserId_thenExceptionIsThrown() {
        //given
        BDDMockito.given(accountRepository.findByUserId(anyLong()))
                .willThrow(AccountNotFoundException.class);
        //when
        assertThrows(AccountNotFoundException.class, () -> accountServiceUnderTest.findByUserId(1).collectList().block());
        //then
    }

    @Test
    @DisplayName("Test get account by userId and currency functionality")
    public void givenUserIdAndCurrency_whenGetAccountByUserIdAndCurrency_thenAccountIsReturned() {
        //given
        BDDMockito.given(accountRepository.findByUserIdAndCurrency(anyLong(), anyString()))
                .willReturn(Mono.just(AccountDataUtils.getMerchantSmirnovBYNAccountPersisted()));
        //when
        AccountEntity accountEntity = accountServiceUnderTest.findByUserIdAndCurrency(1, "BYN").block();
        //then
        assertThat(accountEntity).isNotNull();
        assertThat(accountEntity.getCurrency()).isEqualTo("BYN");
    }

    @Test
    @DisplayName("Test get account by userId and incorrect currency functionality")
    public void givenUserIdAndIncorrectCurrency_whenGetAccountByUserIdAndCurrency_thenExceptionIsThrown() {
        //given
        BDDMockito.given(accountRepository.findByUserIdAndCurrency(anyLong(), anyString()))
                .willThrow(AccountNotFoundException.class);
        //when
        assertThrows(AccountNotFoundException.class, () -> accountServiceUnderTest.findByUserIdAndCurrency(1, "USD").block());
        //then
    }

    @Test
    @DisplayName("Test append balance of account functionality")
    public void givenAccountIdAndAmount_whenUpBalance_thenRepositoryIsCalled() {
        //given
        Long accountId = 1L;
        BigDecimal transactionAmount = BigDecimal.valueOf(100);
        BDDMockito.given(accountRepository.upBalance(eq(accountId), eq(transactionAmount)))
                .willReturn(Mono.empty());
        //when
        Mono<Void> result = accountServiceUnderTest.upBalance(accountId, transactionAmount);
        //then
        StepVerifier.create(result).verifyComplete();
        verify(accountRepository).upBalance(eq(accountId), eq(transactionAmount));
        verify(accountRepository, times(1)).upBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Test update account functionality")
    public void givenAccount_whenUpdate_thenRepositoryIsCalled() {
        //given
        AccountEntity accountToUpdate = AccountDataUtils.getMerchantSmirnovBYNAccountPersisted();
        BDDMockito.given(accountRepository.save(any(AccountEntity.class)))
                .willReturn(Mono.just(accountToUpdate));
        //when
        Mono<AccountEntity> result = accountServiceUnderTest.update(accountToUpdate);
        //then
        StepVerifier.create(result.doOnNext(System.out::println))
                .expectNext(accountToUpdate)
                .verifyComplete();
        verify(accountRepository).save(accountToUpdate);
        verify(accountRepository, times(1)).save(any(AccountEntity.class));
    }
}
