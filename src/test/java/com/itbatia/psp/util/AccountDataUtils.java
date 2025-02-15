package com.itbatia.psp.util;

import com.itbatia.psp.entity.AccountEntity;

import java.math.BigDecimal;

public class AccountDataUtils {

    public static AccountEntity getBatsianSergeyAccountTransient() {
        return AccountEntity.builder()
                .number("BY34MERCH030140012964136000716")
                .balance(BigDecimal.valueOf(1000))
                .currency("BYN")
                .build();
    }

    public static AccountEntity getIvanIvanovAccountTransient() {
        return AccountEntity.builder()
                .number("BY03CUSTOM30140900044303134462")
                .balance(BigDecimal.valueOf(1000))
                .currency("BYN")
                .build();
    }
}
