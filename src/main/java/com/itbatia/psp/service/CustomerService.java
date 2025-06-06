package com.itbatia.psp.service;

import com.itbatia.psp.entity.CustomerEntity;
import com.itbatia.psp.exception.CustomerNotFoundException;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface CustomerService {

    Mono<CustomerEntity> findByFirstNameAndLastNameAndCountry(String firstName, String lastName, String country) throws CustomerNotFoundException;
}
