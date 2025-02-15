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
import java.util.Objects;

/**
 * @author Batsian_SV
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Table("data.webhooks")
public class WebhookEntity implements Persistable<Long> {

    @Id
    @Column("id")
    private Long id;

    @Column("transaction_id")
    private String transactionId;

    @Column("notification_url")
    private String notificationUrl;

    @Column("attempt")
    private Integer attempt;

    @Column("request")
    private String request;

    @Column("response")
    private String response;

    @Column("response_status")
    private Integer responseStatus;

    @Column("to_resend")
    private Boolean toResend;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;

    @Override
    public boolean isNew() {
        return Objects.isNull(id);
    }
}
