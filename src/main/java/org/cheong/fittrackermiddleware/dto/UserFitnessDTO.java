package org.cheong.fittrackermiddleware.dto;

/**
 * @author cheong
 * DTO for user fitness data
 * @param steps
 * @param distance
 * @param averageHeartBeat
 * @param kcal
 */
public record UserFitnessDTO (Integer steps,
                              Double distance,
                              Double averageHeartBeat,
                              Double kcal){
}
