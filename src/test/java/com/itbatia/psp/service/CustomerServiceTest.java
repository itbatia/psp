package com.itbatia.psp.service;

import com.itbatia.psp.entity.CustomerEntity;
import com.itbatia.psp.exception.CustomerNotFoundException;
import com.itbatia.psp.repository.CustomerRepository;
import com.itbatia.psp.service.impl.CustomerServiceImpl;
import com.itbatia.psp.util.CustomerDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author Batsian_SV
 */
@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerServiceUnderTest;

    @Test
    @DisplayName("Test get customer by firstName and lastName and country functionality")
    public void givenFirstNameAndLastNameAndCountry_whenGetByCondition_thenCustomerIsReturned() {
        //given
        BDDMockito.given(customerRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndCountryIgnoreCase(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(CustomerDataUtils.getCustomerIvanovPersisted()));
        //when
        CustomerEntity obtainedCustomer = customerServiceUnderTest.findByFirstNameAndLastNameAndCountry("Ivan", "Ivanov", "BY").block();
        //then
        assertThat(obtainedCustomer).isNotNull();
        assertThat(obtainedCustomer.getLastName()).isEqualTo("Ivanov");
    }

    @Test
    @DisplayName("Test get customer by incorrect firstName and correct lastName and country functionality")
    public void givenIncorrectFirstNameAndCorrectLastNameAndCountry_whenGetByFirstNameAndLastNameAndCountry_thenExceptionIsThrown() {
        //given
        BDDMockito.given(customerRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndCountryIgnoreCase(anyString(), anyString(), anyString()))
                .willThrow(CustomerNotFoundException.class);
        //when
        assertThrows(CustomerNotFoundException.class, () -> customerServiceUnderTest.findByFirstNameAndLastNameAndCountry("Petr", "Ivanov", "BY").block());
        //then
    }
}
