package com.itbatia.psp.util;

import com.itbatia.psp.entity.MerchantEntity;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;

/**
 * @author Batsian_SV
 */
public class AuthDataUtils {

    public static String getMerchantBasicAuth(MerchantEntity merchantEntity) {
        return String.format("Basic %s", HttpHeaders.encodeBasicAuth(merchantEntity.getApiId(), merchantEntity.getApiKey(), StandardCharsets.UTF_8));
    }
}
