package com.itbatia.psp.entity;

import com.itbatia.psp.enums.CardStatus;
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
@Table("data.cards")
public class CardEntity implements Persistable<Long> {

    @Id
    @Column("id")
    private Long id;

    @Column("account_id")
    private Long accountId;

    @Column("card_number")
    private String cardNumber;

    @Column("exp_date")
    private String expDate;

    @Column("cvv")
    private Integer cvv;

    @Column("card_status")
    private CardStatus cardStatus;

    @Column("note")
    private String note;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;

    @Override
    public boolean isNew() {
        return Objects.isNull(id);
    }
}
