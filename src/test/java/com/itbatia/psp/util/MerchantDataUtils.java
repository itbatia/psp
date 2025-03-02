package com.itbatia.psp.util;

import com.itbatia.psp.entity.MerchantEntity;

import java.util.List;

public class MerchantDataUtils {

    public static MerchantEntity getMerchantSmirnovTransient() {
        return MerchantEntity.builder()
                .apiId("Smirnov")
                .apiKey("123456789")
                .ipAddresses(List.of("127.0.0.1", "0:0:0:0:0:0:0:1"))
                .build();
    }

    public static MerchantEntity getMerchantSmirnovPersisted() {
        return MerchantEntity.builder()
                .id(1L)
                .apiId("Smirnov")
                .apiKey("123456789")
                .ipAddresses(List.of("127.0.0.1", "0:0:0:0:0:0:0:1"))
                .build();
    }
}
