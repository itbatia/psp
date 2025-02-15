package com.itbatia.psp.entity;

import com.itbatia.psp.enums.PaymentMethod;
import com.itbatia.psp.enums.TranStatus;
import com.itbatia.psp.enums.TranType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * @author Batsian_SV
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Table("data.transactions")
public class TransactionEntity implements Persistable<String> {

    @Id
    @Column("transaction_id")
    private String transactionId;

    @Column("account_id_from")
    private long accountIdFrom;

    @Column("account_id_to")
    private long accountIdTo;

    @Column("payment_method")
    private PaymentMethod paymentMethod;

    @Column("amount")
    private BigDecimal amount;

    @Column("type")
    private TranType type;

    @Column("notification_url")
    private String notificationUrl;

    @Column("language")
    private String language;

    @Column("status")
    private TranStatus status;

    @Column("message")
    private String message;

    @Column("request")
    private String request;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;

    @Override
    public String getId() {
        return transactionId;
    }

    @Override
    public boolean isNew() {
        return !StringUtils.hasText(transactionId);
    }
}
