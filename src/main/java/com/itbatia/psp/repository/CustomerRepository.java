package com.itbatia.psp.repository;

import com.itbatia.psp.entity.CustomerEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface CustomerRepository extends R2dbcRepository<CustomerEntity, Long> {

    Mono<CustomerEntity> findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndCountryIgnoreCase(String firstName, String lastName, String country);
}
