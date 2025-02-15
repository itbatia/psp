package com.itbatia.psp.util;

import com.itbatia.psp.entity.MerchantEntity;

import java.util.List;

public class MerchantDataUtils {

    public static MerchantEntity getMerchantBatsianSergeyTransient() {
        return MerchantEntity.builder()
                .apiId("Batsian_SV")
                .apiKey("123456789")
                .ipAddresses(List.of("127.0.0.1,0:0:0:0:0:0:0:1"))
                .build();
    }
}
