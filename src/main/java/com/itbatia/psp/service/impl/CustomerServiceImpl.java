package com.itbatia.psp.service.impl;

import com.itbatia.psp.entity.CustomerEntity;
import com.itbatia.psp.exception.CustomerNotFoundException;
import com.itbatia.psp.repository.CustomerRepository;
import com.itbatia.psp.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Mono<CustomerEntity> findByFirstNameAndLastNameAndCountry(String firstName, String lastName, String country) throws CustomerNotFoundException {
        return customerRepository
                .findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndCountryIgnoreCase(firstName, lastName, country)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer not found")))
                .doOnError(CustomerNotFoundException.class, e -> log.error("IN findByFirstNameAndLastNameAndCountry - {}", e.getMessage()));
    }
}
