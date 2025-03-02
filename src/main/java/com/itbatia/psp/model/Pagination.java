package com.itbatia.psp.model;

import lombok.Builder;
import lombok.Getter;

/**
 * @author Batsian_SV
 */
@Builder
public class Pagination {

    @Getter
    private long limit;
    private long totalElements;
    private boolean hasNextPage;

    public boolean hasNextPage() {
        return hasNextPage;
    }

    public static Pagination init(int limit, long totalElements) {
        return Pagination.builder()
                .limit(limit)
                .totalElements(totalElements)
                .hasNextPage(totalElements > limit)
                .build();
    }

    public void moveToNextPage() {
        totalElements = totalElements - limit;
        hasNextPage = totalElements > limit;
        limit = hasNextPage ? limit : totalElements;
    }
}
