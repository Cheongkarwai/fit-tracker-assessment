package org.cheong.fittrackermiddleware.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * @author cheong
 * Implementation for custom repository for user fitness metadata
 */
public class CustomUserFitnessMetadataRepositoryImpl implements CustomUserFitnessMetadataRepository{

    private final DatabaseClient databaseClient;

    /**
     * Constructor for CustomUserFitnessMetadataRepositoryImpl
     * @param databaseClient API provided by Spring Data R2DBC
     */
    public CustomUserFitnessMetadataRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    /**
     * Create user fitness metadata table partition
     * @param partitionName table partition name prefix
     * @param from date_created from
     * @param to date_created until
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> createUserFitnessMetadataTablePartition(String partitionName, LocalDate from, LocalDate to){

        String sql = "CREATE TABLE IF NOT EXISTS user_fitness_metadata_%s PARTITION OF user_fitness_metadata FOR VALUES FROM ('%s') TO ('%s')";

        return databaseClient.sql(String.format(sql, partitionName, from, to))
                .fetch()
                .rowsUpdated()
                .then();
    }
}
