package org.cheong.fittrackermiddleware.repository;

import org.cheong.fittrackermiddleware.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

/**
 * @author cheong
 * Repository for user
 */
public interface UserRepository extends R2dbcRepository<User, String> {
}
