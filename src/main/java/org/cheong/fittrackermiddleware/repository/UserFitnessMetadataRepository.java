package org.cheong.fittrackermiddleware.repository;

import org.cheong.fittrackermiddleware.dto.UserFitnessDTO;
import org.cheong.fittrackermiddleware.model.UserFitnessMetadata;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface UserFitnessMetadataRepository extends R2dbcRepository<UserFitnessMetadata, String>, CustomUserFitnessMetadataRepository {

    @Query("""
            SELECT SUM(steps) AS steps, SUM(steps) * 0.0007 AS distance, AVG(heart_beat) AS average_heart_beat, 1440 * (SUM(met) * 3.5 * :weight / 200) AS kcal FROM user_fitness_metadata
            WHERE user_id = :userId AND (created_at > :from AND created_at <= :to)
            """)
    Mono<UserFitnessDTO> calculateUserFitness(String userId, int weight, LocalDateTime from, LocalDateTime to);
}
