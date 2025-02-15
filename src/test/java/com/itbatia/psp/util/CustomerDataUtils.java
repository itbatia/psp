package com.itbatia.psp.util;

import com.itbatia.psp.entity.CustomerEntity;
import com.itbatia.psp.entity.MerchantEntity;

import java.util.List;

public class CustomerDataUtils {

    public static CustomerEntity getCustomerIvanIvanovTransient() {
        return CustomerEntity.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .country("BY")
                .build();
    }
}
