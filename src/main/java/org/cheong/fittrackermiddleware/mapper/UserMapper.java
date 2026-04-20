package org.cheong.fittrackermiddleware.mapper;

import org.cheong.fittrackermiddleware.model.User;
import org.cheong.fittrackermiddleware.model.UserFitnessMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * @author cheong
 * Mapper for user data
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Map UserFitnessMetadata to User
     * @param userFitnessMetadata UserFitnessMetadata
     * @return User
     */
    @Mapping(target = "id", source = "userId")
    User mapToUser(UserFitnessMetadata userFitnessMetadata);
}
