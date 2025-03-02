package com.itbatia.psp.repository;

import com.itbatia.psp.entity.UserEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

/**
 * @author Batsian_SV
 */
public interface UserRepository extends R2dbcRepository<UserEntity, Long> {
}
