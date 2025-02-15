package com.itbatia.psp.service;

import com.itbatia.psp.entity.MerchantEntity;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface MerchantService {

    Mono<MerchantEntity> findByApiId(String apiId);
}
