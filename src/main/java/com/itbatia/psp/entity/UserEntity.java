package com.itbatia.psp.entity;

import com.itbatia.psp.enums.Status;
import com.itbatia.psp.enums.UserType;
import lombok.*;
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
@Table("data.users")
public class UserEntity implements Persistable<Long> {

    @Id
    @Column("id")
    private Long id;

    @Column("type")
    private UserType type;

    @Column("status")
    private Status status;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;

    @Override
    public boolean isNew() {
        return Objects.isNull(id);
    }
}
