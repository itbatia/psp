package com.itbatia.psp.service.impl;

import com.itbatia.psp.entity.MerchantEntity;
import com.itbatia.psp.repository.MerchantRepository;
import com.itbatia.psp.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author Batsian_SV
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;

    @Override
    public Mono<MerchantEntity> findByApiId(String apiId) {
        return merchantRepository.findByApiId(apiId)
                .doOnSuccess(merchant -> {
                    if (Objects.isNull(merchant))
                        log.warn("IN findByApiId - Merchant not found by apiId={}", apiId);
                    else
                        log.info("IN findByApiId - Merchant found by apiId={}", apiId);
                });
    }
}
