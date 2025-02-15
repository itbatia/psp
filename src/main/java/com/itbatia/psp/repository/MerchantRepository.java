package com.itbatia.psp.repository;

import com.itbatia.psp.entity.MerchantEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface MerchantRepository extends R2dbcRepository<MerchantEntity, Long> {

    Mono<MerchantEntity> findByApiId(String apiId);
}
