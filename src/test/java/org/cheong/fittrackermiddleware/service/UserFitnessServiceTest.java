package org.cheong.fittrackermiddleware.service;

import org.cheong.fittrackermiddleware.mapper.UserFitnessMapper;
import org.cheong.fittrackermiddleware.mapper.UserFitnessMapperImpl;
import org.cheong.fittrackermiddleware.mapper.UserMapper;
import org.cheong.fittrackermiddleware.mapper.UserMapperImpl;
import org.cheong.fittrackermiddleware.model.UserFitnessMetadata;
import org.cheong.fittrackermiddleware.repository.UserFitnessMetadataRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        UserFitnessService.class, UserFitnessMapperImpl.class,
        UserMapperImpl.class, ObjectMapper.class})
public class UserFitnessServiceTest {

    @Autowired
    private UserFitnessService userFitnessService;

    @Autowired
    private UserFitnessMapper userFitnessMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserFitnessMetadataRepository userFitnessMetadataRepository;

    private static String BASE64_ENCODED_STRING_FITNESS_DATA = "";

    @BeforeEach
    void setUp() {
        UserFitnessMetadata userFitnessMetadata = new UserFitnessMetadata();
        userFitnessMetadata.setUserId("abc123def456");
        userFitnessMetadata.setSteps(20);
        userFitnessMetadata.setHeartBeat(51);
        userFitnessMetadata.setMet(51);
        BASE64_ENCODED_STRING_FITNESS_DATA = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(userFitnessMetadata));
    }

    @Test
    @DisplayName("Save user fitness should save user when user not exists in database")
    void saveUserFitness_shouldSaveUserInDatabase_whenUserNotExists() {
        when(userService.checkIfUserExists(any())).then(answer -> Mono.just(false));
        when(userService.saveUser(any())).then(answer -> Mono.just(answer.getArgument(0)));
        when(userFitnessMetadataRepository.save(any())).then(answer -> Mono.just(answer.getArgument(0)));

        StepVerifier.create(userFitnessService.saveUserFitnessRealTimeMetadata(BASE64_ENCODED_STRING_FITNESS_DATA))
                .expectNextMatches(userFitnessMetadata -> userFitnessMetadata.getUserId().equals("abc123def456"))
                .verifyComplete();

        verify(userService, times(1)).saveUser(any());
        verify(userFitnessMetadataRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Save user fitness should not save user when user exists in database")
    void saveUserFitness_shouldNotSaveUserInDatabase_whenUserExists() {
        when(userService.checkIfUserExists(any())).then(answer -> Mono.just(true));
        when(userService.saveUser(any())).then(answer -> Mono.just(answer.getArgument(0)));
        when(userFitnessMetadataRepository.save(any())).then(answer -> Mono.just(answer.getArgument(0)));

        StepVerifier.create(userFitnessService.saveUserFitnessRealTimeMetadata(BASE64_ENCODED_STRING_FITNESS_DATA))
                .expectNextMatches(userFitnessMetadata -> userFitnessMetadata.getUserId().equals("abc123def456"))
                .verifyComplete();

        verify(userService, times(0)).saveUser(any());
        verify(userFitnessMetadataRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Save user fitness should not save user fitness into database when error occurs")
    void saveUserFitness_shouldNotSaveUserFitnessIntoDatabase_whenErrorOccurs() {
        when(userService.checkIfUserExists(any())).then(answer -> Mono.just(false));
        when(userService.saveUser(any())).thenReturn(Mono.error(new RuntimeException("Database error")));
        when(userFitnessMetadataRepository.save(any())).then(answer -> Mono.just(answer.getArgument(0)));

        StepVerifier.create(userFitnessService.saveUserFitnessRealTimeMetadata(BASE64_ENCODED_STRING_FITNESS_DATA))
                .expectError(RuntimeException.class)
                .verify();

        verify(userService, times(1)).saveUser(any());
        verifyNoInteractions(userFitnessMetadataRepository);
    }
}
