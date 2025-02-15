package com.itbatia.psp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author Batsian_SV
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Table("data.merchants")
public class MerchantEntity implements Persistable<Long> {

    @Id
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("api_id")
    private String apiId;

    @Column("api_key")
    private String apiKey;

    @Column("ip_addresses")
    private List<String> ipAddresses;

    @Column("note")
    private String note;

    @Column("updated_at")
    private OffsetDateTime updatedAt;

    @Override
    public boolean isNew() {
        return Objects.isNull(id);
    }
}
