package com.itbatia.psp.util;

import com.itbatia.psp.entity.MerchantEntity;

import java.util.List;

/**
 * @author Batsian_SV
 */
public class MerchantDataUtils {

    public static final long MERCHANT_SMIRNOV_ID = 1;
    public static final String MERCHANT_SMIRNOV_ID_AS_STRING = "1";

    public static MerchantEntity getMerchantSmirnovTransient() {
        return MerchantEntity.builder()
                .apiId("Smirnov")
                .apiKey("123456789")
                .ipAddresses(List.of("127.0.0.1", "0:0:0:0:0:0:0:1"))
                .build();
    }

    public static MerchantEntity getMerchantSmirnovPersisted() {
        return MerchantEntity.builder()
                .id(MERCHANT_SMIRNOV_ID)
                .apiId("Smirnov")
                .apiKey("123456789")
                .ipAddresses(List.of("127.0.0.1", "0:0:0:0:0:0:0:1"))
                .build();
    }
}
