package com.itbatia.psp.util;

import com.itbatia.psp.entity.AccountEntity;

import java.math.BigDecimal;

public class AccountDataUtils {

    public static AccountEntity getSmirnovBYNAccountPersisted() {
        return AccountEntity.builder()
                .id(1L)
                .userId(1L)
                .number("BY34MERCH030140012964136000716")
                .balance(BigDecimal.valueOf(1000))
                .currency("BYN")
                .build();
    }

    public static AccountEntity getSmirnovRUBAccountPersisted() {
        return AccountEntity.builder()
                .id(2L)
                .userId(1L)
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
