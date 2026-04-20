package org.cheong.fittrackermiddleware.dto;

/**
 * @author cheong
 * DTO for token request
 * @param clientId
 * @param clientSecret
 */
public record TokenRequest (String clientId, String clientSecret) {
}
