package com.itbatia.psp.util;

import java.time.OffsetDateTime;

/**
 * @author Batsian_SV
 */
public class DateTimeUtils {

    private final static OffsetDateTime createdAt = OffsetDateTime.now().plusMinutes(1);
    private final static OffsetDateTime updatedAt = OffsetDateTime.now().plusMinutes(1);

    public static OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public static OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
