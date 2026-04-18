package org.cheong.fittrackermiddleware.repository;

import org.cheong.fittrackermiddleware.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, String> {

    Mono<Integer> countById(String id);
}
