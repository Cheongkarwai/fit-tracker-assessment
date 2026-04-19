package org.cheong.fittrackermiddleware.service;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.dto.UserFitnessDTO;
import org.cheong.fittrackermiddleware.exception.UserNotFoundException;
import org.cheong.fittrackermiddleware.mapper.UserFitnessMapper;
import org.cheong.fittrackermiddleware.mapper.UserMapper;
import org.cheong.fittrackermiddleware.model.UserFitnessMetadata;
import org.cheong.fittrackermiddleware.repository.UserFitnessMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalTime;

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
                              UserService userService) {
        this.userFitnessMapper = userFitnessMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.userFitnessMetadataRepository = userFitnessMetadataRepository;
        this.userService = userService;
    }

    @Transactional(timeout = 30)
    public Mono<UserFitnessMetadata> saveUserFitnessRealTimeMetadata(String base64EncodedStringFitnessData) {
        return Mono.just(base64EncodedStringFitnessData)
                .map(fitnessDataString -> userFitnessMapper.mapBase64EncodedStringToUserTrace(fitnessDataString, objectMapper))
                .flatMap(userFitnessMetadata -> userService.checkIfUserExists(userFitnessMetadata.getUserId())
                        .filter(isExists -> !isExists)
                        .map(count -> userMapper.mapToUser(userFitnessMetadata))
                        .flatMap(userService::saveUser)
                        .thenReturn(userFitnessMetadata))
                .flatMap(userFitnessMetadataRepository::save);
    }

    @Transactional(readOnly = true, timeout = 30)
    public Mono<UserFitnessDTO> calculateUserFitness(String userId, LocalDate date) {
        return userService.checkIfUserExists(userId)
                .filter(isExists -> isExists)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")))
                .flatMap(isExists -> userService.getFitTrackerUser(userId))
                .flatMap(userResponse -> userFitnessMetadataRepository.calculateUserFitness(userId, userResponse.weight(), date.atStartOfDay(), date.atTime(LocalTime.MAX)));
    }
}
