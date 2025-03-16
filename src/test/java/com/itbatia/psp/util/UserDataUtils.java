package com.itbatia.psp.util;

import com.itbatia.psp.entity.UserEntity;
import com.itbatia.psp.enums.Status;
import com.itbatia.psp.enums.UserType;

/**
 * @author Batsian_SV
 */
public class UserDataUtils {

    public static final long MERCHANT_SMIRNOV_USER_ID = 1;
    public static final long CUSTOMER_IVANOV_USER_ID = 2;

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
