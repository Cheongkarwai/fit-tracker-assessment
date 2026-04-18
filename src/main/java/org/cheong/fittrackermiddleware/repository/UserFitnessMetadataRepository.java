package org.cheong.fittrackermiddleware.repository;

import org.cheong.fittrackermiddleware.model.UserFitnessMetadata;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface UserFitnessMetadataRepository extends R2dbcRepository<UserFitnessMetadata, String> {
}
