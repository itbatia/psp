package com.itbatia.psp.util;

import com.itbatia.psp.dto.CustomerDto;
import com.itbatia.psp.entity.CustomerEntity;

/**
 * @author Batsian_SV
 */
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
                .userId(UserDataUtils.CUSTOMER_USER_ID)
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
