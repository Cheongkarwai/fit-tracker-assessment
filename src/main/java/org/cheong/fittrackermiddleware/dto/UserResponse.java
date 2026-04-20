package org.cheong.fittrackermiddleware.dto;

/**
 * @author cheong
 * DTO for user response
 * @param id
 * @param weight
 * @param height
 */
public record UserResponse(String id,
                           int weight,
                           int height) {
}
