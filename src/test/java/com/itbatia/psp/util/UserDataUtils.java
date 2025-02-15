package com.itbatia.psp.util;

import com.itbatia.psp.entity.UserEntity;
import com.itbatia.psp.enums.Status;
import com.itbatia.psp.enums.UserType;

public class UserDataUtils {

    public static UserEntity getUserMerchantTransient() {
        return UserEntity.builder()
                .type(UserType.MERCHANT)
                .status(Status.ACTIVE)
                .build();
    }

    public static UserEntity getUserCustomerTransient() {
        return UserEntity.builder()
                .type(UserType.CUSTOMER)
                .status(Status.ACTIVE)
                .build();
    }
}
