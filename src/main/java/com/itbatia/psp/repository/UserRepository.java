package com.itbatia.psp.repository;

import com.itbatia.psp.entity.UserEntity;
import com.itbatia.psp.enums.Status;
import com.itbatia.psp.enums.UserType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

/**
 * @author Batsian_SV
 */
public interface UserRepository extends R2dbcRepository<UserEntity, Long> {

    @Query("INSERT INTO data.users (type, status) VALUES (:type::data.user_type, :status::data.status) RETURNING *")
    Mono<UserEntity> save(@Param("type") UserType userType, @Param("status") Status status);
}
