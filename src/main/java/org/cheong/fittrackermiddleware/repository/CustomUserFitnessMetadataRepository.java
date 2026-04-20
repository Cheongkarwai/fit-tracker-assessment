package org.cheong.fittrackermiddleware.repository;

import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * @author cheong
 * Custom repository for user fitness metadata
 */
public interface CustomUserFitnessMetadataRepository {

    /**
     * Create user fitness metadata table partition
     * @param partitionName table partition name prefix
     * @param from date_created from
     * @param to date_created until
     * @return Mono<Void>
     */
    Mono<Void> createUserFitnessMetadataTablePartition(String partitionName, LocalDate from, LocalDate to);
}
