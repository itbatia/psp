package com.itbatia.psp.util;

import com.itbatia.psp.dto.CustomerDto;
import com.itbatia.psp.entity.CustomerEntity;

/**
 * @author Batsian_SV
 */
public class CustomerDataUtils {

    public static final String IVANOV_FIRST_NAME = "Ivan";
    public static final String IVANOV_LAST_NAME = "Ivanov";
    public static final String IVANOV_COUNTRY = "BY";
    public static final String PETROV_FIRST_NAME = "Petr";
    public static final String PETROV_LAST_NAME = "Petrov";
    public static final String PETROV_COUNTRY = "RU";

    public static CustomerEntity getCustomerIvanovTransient() {
        return CustomerEntity.builder()
                .firstName(IVANOV_FIRST_NAME)
                .lastName(IVANOV_LAST_NAME)
                .country(IVANOV_COUNTRY)
                .build();
    }

    public static CustomerEntity getCustomerIvanovPersisted() {
        return CustomerEntity.builder()
                .id(1L)
                .userId(UserDataUtils.CUSTOMER_IVANOV_USER_ID)
                .firstName(IVANOV_FIRST_NAME)
                .lastName(IVANOV_LAST_NAME)
                .country(IVANOV_COUNTRY)
                .build();
    }

    public static CustomerDto getCustomerIvanovDto() {
        return CustomerDto.builder()
                .firstName(IVANOV_FIRST_NAME)
                .lastName(IVANOV_LAST_NAME)
                .country(IVANOV_COUNTRY)
                .build();
    }

    public static CustomerDto getCustomerPetrovDto() {
        return CustomerDto.builder()
                .firstName(PETROV_FIRST_NAME)
                .lastName(PETROV_LAST_NAME)
                .country(PETROV_COUNTRY)
                .build();
    }
}
