package org.cheong.fittrackermiddleware.repository;

import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface CustomUserFitnessMetadataRepository {

    Mono<Void> createUserFitnessMetadataTablePartition(String partitionName, LocalDate from, LocalDate to);
}
