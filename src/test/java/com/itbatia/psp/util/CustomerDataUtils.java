package com.itbatia.psp.util;

import com.itbatia.psp.dto.CustomerDto;
import com.itbatia.psp.entity.CustomerEntity;

public class CustomerDataUtils {

    public static CustomerEntity getCustomerIvanovTransient() {
        return CustomerEntity.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .country("BY")
                .build();
    }

    public static CustomerEntity getCustomerIvanovPersisted() {
        return CustomerEntity.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .country("BY")
                .build();
    }

    public static CustomerDto getCustomerIvanovDto() {
        return CustomerDto.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .country("BY")
                .build();
    }
}
