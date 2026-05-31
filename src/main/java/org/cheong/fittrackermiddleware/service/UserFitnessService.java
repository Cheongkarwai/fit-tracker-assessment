package org.cheong.fittrackermiddleware.service;

import io.github.resilience4j.retry.annotation.Retry;
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

/**
 * @author cheong
 * Service for user fitness data
 */
@Service
@Slf4j
public class UserFitnessService {

    private final UserFitnessMapper userFitnessMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final UserFitnessMetadataRepository userFitnessMetadataRepository;
    private final UserService userService;

    /**
     * Constructor for UserFitnessService
     * @param userFitnessMapper UserFitnessMapper
     * @param userMapper UserMapper
     * @param objectMapper ObjectMapper
     * @param userFitnessMetadataRepository UserFitnessMetadataRepository
     * @param userService UserService
     */
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

    /**
     * Save user fitness metadata in a transaction
     * @param base64EncodedStringFitnessData base64 encoded string of user fitness metadata
     * @return Mono<UserFitnessMetadata>
     */
    @Retry(name = "user-fitness-service", fallbackMethod = "saveUserFitnessRealTimeMetadataFallback")
    @Transactional(timeout = 30)
    public Mono<UserFitnessMetadata> saveUserFitnessRealTimeMetadata(String base64EncodedStringFitnessData) {
        return Mono.just(base64EncodedStringFitnessData)
                .map(fitnessDataString -> userFitnessMapper.mapBase64EncodedStringToUserTrace(fitnessDataString, objectMapper))
                .flatMap(userFitnessMetadata -> userService.checkIfUserExists(userFitnessMetadata.getUserId())
                        .filter(isExists -> !isExists)
                        .map(v -> userMapper.mapToUser(userFitnessMetadata))
                        .flatMap(userService::saveUser)
                        .then(Mono.just(userFitnessMetadata)))
                .flatMap(userFitnessMetadataRepository::save);
    }

    /**
     * Fallback method for saveUserFitnessRealTimeMetadata
     * @param base64EncodedStringFitnessData base64 encoded string of user fitness metadata
     * @param throwable Throwable
     * @return Mono<UserFitnessMetadata>
     */
    public Mono<UserFitnessMetadata> saveUserFitnessRealTimeMetadataFallback(String base64EncodedStringFitnessData, Throwable throwable) {
        log.error("Error occurred while saving user fitness metadata", throwable);
        return Mono.empty();
    }

    /**
     * Calculate user fitness data
     * @param userId identifie of the user
     * @param date date of the user fitness data
     * @return Mono<UserFitnessDTO>
     */
    @Retry(name = "user-fitness-service", fallbackMethod = "calculateUserFitnessFallback")
    @Transactional(readOnly = true, timeout = 30)
    public Mono<UserFitnessDTO> calculateUserFitness(String userId, LocalDate date) {
        return userService.checkIfUserExists(userId)
                .filter(isExists -> isExists)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")))
                .flatMap(isExists -> userService.getFitTrackerUser(userId))
                .flatMap(userResponse -> userFitnessMetadataRepository.calculateUserFitness(userId, userResponse.weight(), date.atStartOfDay(), date.atTime(LocalTime.MAX)));
    }

    /**
     * Fallback method for calculateUserFitness
     * @param userId identifier of the user
     * @param date date of the user fitness data
     * @param throwable Throwable
     * @return Mono<UserFitnessDTO>
     */
    public Mono<UserFitnessDTO> calculateUserFitnessFallback(String userId, LocalDate date, Throwable throwable) {
        log.error("Error occurred while calculating user fitness", throwable);
        return Mono.empty();
    }
}
