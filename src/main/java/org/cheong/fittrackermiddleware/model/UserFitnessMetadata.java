package org.cheong.fittrackermiddleware.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * @author cheong
 * Model for user fitness metadata
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("user_fitness_metadata")
public class UserFitnessMetadata {

    /**
     * User id
     */
    @Column("user_id")
    private String userId;

    /**
     * Steps taken by user
     */
    @Column("steps")
    private int steps;

    /**
     * User's met
     */
    @Column("met")
    private int met;

    /**
     * Heart beat of user for specific time
     */
    @Column("heart_beat")
    private int heartBeat;

    /**
     * Created at timestamp
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}
