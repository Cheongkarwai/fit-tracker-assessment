package org.cheong.fittrackermiddleware.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("user_fitness_metadata")
public class UserFitnessMetadata {

    @Column("user_id")
    private String userId;

    @Column("steps")
    private int steps;

    @Column("met")
    private int met;

    @Column("heart_beat")
    private int heartBeat;
}
