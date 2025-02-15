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
        this.totalElements = this.totalElements - this.limit;
        this.hasNextPage = this.totalElements > this.limit;
        this.limit = this.hasNextPage ? this.limit : this.totalElements;
    }
}
