package com.itbatia.psp.services;

import com.itbatia.psp.entity.MerchantEntity;
import com.itbatia.psp.repository.MerchantRepository;
import com.itbatia.psp.service.impl.MerchantServiceImpl;
import com.itbatia.psp.util.MerchantDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

/**
 * @author Batsian_SV
 */
@ExtendWith(MockitoExtension.class)
public class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private MerchantServiceImpl merchantService;

    @Test
    @DisplayName("Test get merchant by apiId functionality")
    public void givenApiId_whenGetByApiId_thenMerchantIsReturned() {
        //given
        BDDMockito.given(merchantRepository.findByApiId(anyString()))
                .willReturn(Mono.just(MerchantDataUtils.getMerchantSmirnovPersisted()));
        //when
        MerchantEntity obtainedMerchant = merchantService.findByApiId("Smirnov").block();
        //then
        assertThat(obtainedMerchant).isNotNull();
        assertThat(obtainedMerchant.getApiKey()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("Test get merchant by incorrect apiId functionality")
    public void givenIncorrectApiId_whenGetByApiId_thenEmptyIsReturned() {
        //given
        BDDMockito.given(merchantRepository.findByApiId(anyString()))
                .willReturn(Mono.empty());
        //when
        MerchantEntity obtainedMerchant = merchantService.findByApiId("Smirnov").block();
        //then
        assertThat(obtainedMerchant).isNull();
    }
}
