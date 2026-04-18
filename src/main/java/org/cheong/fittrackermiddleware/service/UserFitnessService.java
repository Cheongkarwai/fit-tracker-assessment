package org.cheong.fittrackermiddleware.service;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.mapper.UserFitnessMapper;
import org.cheong.fittrackermiddleware.mapper.UserMapper;
import org.cheong.fittrackermiddleware.model.UserFitnessMetadata;
import org.cheong.fittrackermiddleware.repository.UserFitnessMetadataRepository;
import org.cheong.fittrackermiddleware.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class UserFitnessService {

    private final UserFitnessMapper userFitnessMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final UserFitnessMetadataRepository userFitnessMetadataRepository;
    private final UserService userService;

    public UserFitnessService(UserFitnessMapper userFitnessMapper,
                              UserMapper userMapper,
                              ObjectMapper objectMapper, UserFitnessMetadataRepository userFitnessMetadataRepository,
                              UserService userService){
        this.userFitnessMapper = userFitnessMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.userFitnessMetadataRepository = userFitnessMetadataRepository;
        this.userService = userService;
    }

    @Transactional
    public Mono<UserFitnessMetadata> saveUserFitnessRealTimeMetadata(String base64EncodedStringFitnessData){
        return Mono.just(base64EncodedStringFitnessData)
                .map(fitnessDataString-> userFitnessMapper.mapBase64EncodedStringToUserTrace(fitnessDataString, objectMapper))
                .flatMap(userFitnessMetadata -> userService.checkIfUserExists(userFitnessMetadata.getUserId())
                        .filter(isExists -> !isExists)
                        .map(count -> userMapper.mapToUser(userFitnessMetadata))
                        .flatMap(userService::saveUser)
                        .thenReturn(userFitnessMetadata))
                .flatMap(userFitnessMetadataRepository::save);
    }
}
