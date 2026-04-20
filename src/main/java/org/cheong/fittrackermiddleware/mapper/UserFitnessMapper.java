package org.cheong.fittrackermiddleware.mapper;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.model.UserFitnessMetadata;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.util.Base64;

/**
 * @author cheong
 * Mapper for user fitness data
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserFitnessMapper {

    Logger log = LoggerFactory.getLogger(UserFitnessMapper.class);

    /**
     * Map base64 encoded string to UserFitnessMetadata
     * @param base64EncodedString base64 encoded string
     * @param objectMapper ObjectMapper
     * @return UserFitnessMetadata
     */
    default UserFitnessMetadata mapBase64EncodedStringToUserTrace(String base64EncodedString, @Context ObjectMapper objectMapper) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decodedBytes = decoder.decode(base64EncodedString);
        try {
            log.info("Base64 decoded string: {}", base64EncodedString);
            return objectMapper.readValue(decodedBytes, UserFitnessMetadata.class);
        } catch (JacksonException e) {
            log.error("Error while mapping base64 encoded string to user trace", e);
            throw e;
        }
    }
}
