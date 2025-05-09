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
    public static final long CUSTOMER_PETROV_BYN_ACCOUNT_ID = 4;

    public static AccountEntity getMerchantSmirnovBYNAccountPersisted() {
        return buildMerchantSmirnovBYNAccountPersisted(ConstantUtils.BALANCE_1000);
    }

    public static AccountEntity getMerchantSmirnovBYNAccountPersisted(BigDecimal balance) {
        return buildMerchantSmirnovBYNAccountPersisted(balance);
    }

    private static AccountEntity buildMerchantSmirnovBYNAccountPersisted(BigDecimal balance) {
        return AccountEntity.builder()
                .id(MERCHANT_SMIRNOV_BYN_ACCOUNT_ID)
                .userId(UserDataUtils.MERCHANT_SMIRNOV_USER_ID)
                .number("BY34MERCH030140012964136000716")
                .balance(balance)
                .currency("BYN")
                .build();
    }

    public static AccountEntity getCustomerIvanovBYNAccountPersisted() {
        return buildCustomerIvanovBYNAccountPersisted(ConstantUtils.BALANCE_1000);
    }

    public static AccountEntity getCustomerIvanovBYNAccountPersisted(BigDecimal balance) {
        return buildCustomerIvanovBYNAccountPersisted(balance);
    }

    private static AccountEntity buildCustomerIvanovBYNAccountPersisted(BigDecimal balance) {
        return AccountEntity.builder()
                .id(CUSTOMER_IVANOV_BYN_ACCOUNT_ID)
                .userId(UserDataUtils.CUSTOMER_IVANOV_USER_ID)
                .number("BY03CUSTOM30140900044303134462")
                .balance(balance)
                .currency("BYN")
                .build();
    }

    public static AccountEntity getMerchantSmirnovRUBAccountPersisted() {
        return AccountEntity.builder()
                .id(MERCHANT_SMIRNOV_RUB_ACCOUNT_ID)
                .userId(UserDataUtils.MERCHANT_SMIRNOV_USER_ID)
                .number("RU83MERCH361407000843083139443")
                .balance(ConstantUtils.BALANCE_1000)
                .currency("RUB")
                .build();
    }

    public static AccountEntity getCustomerAccountTransient() {
        return buildCustomerAccountTransient(ConstantUtils.BALANCE_1000);
    }

    public static AccountEntity getCustomerAccountTransient(BigDecimal balance) {
        return buildCustomerAccountTransient(balance);
    }

    private static AccountEntity buildCustomerAccountTransient(BigDecimal balance) {
        return AccountEntity.builder()
                .number("BY03CUSTOM30140900044303134462")
                .balance(balance)
                .currency("BYN")
                .build();
    }

    public static AccountEntity getMerchantSmirnovBYNAccountTransient() {
        return buildMerchantSmirnovBYNAccountTransient(ConstantUtils.BALANCE_1000);
    }

    public static AccountEntity getMerchantSmirnovBYNAccountTransient(BigDecimal balance) {
        return buildMerchantSmirnovBYNAccountTransient(balance);
    }

    private static AccountEntity buildMerchantSmirnovBYNAccountTransient(BigDecimal balance) {
        return AccountEntity.builder()
                .number("BY34MERCH030140012964136000716")
                .balance(balance)
                .currency("BYN")
                .build();
    }

    public static AccountEntity getMerchantSmirnovRUBAccountTransient() {
        return AccountEntity.builder()
                .number("RU83MERCH361407000843083139443")
                .balance(ConstantUtils.BALANCE_1000)
                .currency("RUB")
                .build();
    }
}
