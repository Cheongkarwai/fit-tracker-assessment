package org.cheong.fittrackermiddleware.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public class CustomUserFitnessMetadataRepositoryImpl implements CustomUserFitnessMetadataRepository{

    private final DatabaseClient databaseClient;

    public CustomUserFitnessMetadataRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Void> createUserFitnessMetadataTablePartition(String partitionName, LocalDate from, LocalDate to){

        String sql = "CREATE TABLE IF NOT EXISTS user_fitness_metadata_%s PARTITION OF user_fitness_metadata FOR VALUES FROM ('%s') TO ('%s')";

        return databaseClient.sql(String.format(sql, partitionName, from, to))
                .fetch()
                .rowsUpdated()
                .then();
    }
}
