package org.cheong.fittrackermiddleware.dto;

public record UserFitnessDTO (Integer steps,
                              Double distance,
                              Double averageHeartBeat,
                              Double kcal){
}
