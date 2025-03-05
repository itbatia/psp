package com.itbatia.psp.util;

import com.itbatia.psp.entity.AccountEntity;

import java.math.BigDecimal;

/**
 * @author Batsian_SV
 */
public class AccountDataUtils {

    public static final long MERCHANT_SMIRNOV_BYN_ACCOUNT_ID = 1;
    public static final long MERCHANT_SMIRNOV_RUB_ACCOUNT_ID = 2;
    public static final long CUSTOMER_IVANOV_BYN_ACCOUNT_ID = 3;

    public static AccountEntity getMerchantSmirnovBYNAccountPersisted() {
        return buildMerchantSmirnovBYNAccountPersisted(BigDecimal.valueOf(1000));
    }

    public static AccountEntity getMerchantSmirnovBYNAccountPersisted(BigDecimal balance) {
        return buildMerchantSmirnovBYNAccountPersisted(balance);
    }

    private static AccountEntity buildMerchantSmirnovBYNAccountPersisted(BigDecimal balance) {
        return AccountEntity.builder()
                .id(MERCHANT_SMIRNOV_BYN_ACCOUNT_ID)
                .userId(UserDataUtils.MERCHANT_USER_ID)
                .number("BY34MERCH030140012964136000716")
                .balance(balance)
                .currency("BYN")
                .build();
    }

    public static AccountEntity getCustomerIvanovBYNAccountPersisted() {
        return buildCustomerIvanovBYNAccountPersisted(BigDecimal.valueOf(1000));
    }

    public static AccountEntity getCustomerIvanovBYNAccountPersisted(BigDecimal balance) {
        return buildCustomerIvanovBYNAccountPersisted(balance);
    }

    private static AccountEntity buildCustomerIvanovBYNAccountPersisted(BigDecimal balance) {
        return AccountEntity.builder()
                .id(CUSTOMER_IVANOV_BYN_ACCOUNT_ID)
                .userId(UserDataUtils.CUSTOMER_USER_ID)
                .number("BY03CUSTOM30140900044303134462")
                .balance(balance)
                .currency("BYN")
                .build();
    }

    public static AccountEntity getMerchantSmirnovRUBAccountPersisted() {
        return AccountEntity.builder()
                .id(MERCHANT_SMIRNOV_RUB_ACCOUNT_ID)
                .userId(UserDataUtils.MERCHANT_USER_ID)
                .number("RU83MERCH361407000843083139443")
                .balance(BigDecimal.valueOf(1000))
                .currency("RUB")
                .build();
    }

    public static AccountEntity getMerchantAccountTransient() {
        return AccountEntity.builder()
                .number("BY34MERCH030140012964136000716")
                .balance(BigDecimal.valueOf(1000))
                .currency("BYN")
                .build();
    }

    public static AccountEntity getCustomerAccountTransient() {
        return AccountEntity.builder()
                .number("BY03CUSTOM30140900044303134462")
                .balance(BigDecimal.valueOf(1000))
                .currency("BYN")
                .build();
    }
}
