package org.cheong.fittrackermiddleware.service;

import org.cheong.fittrackermiddleware.model.User;
import org.cheong.fittrackermiddleware.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Check user exists, return true when user exists")
    void checkUserIfExists_ReturnTrue_WhenUserExists() {
        when(userRepository.existsById(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(userService.checkIfUserExists("123"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Check user exists, return false when user not exists")
    void checkUserIfExists_ReturnFalse_WhenUserNotExists() {
        when(userRepository.existsById(anyString())).thenReturn(Mono.just(false));

        StepVerifier.create(userService.checkIfUserExists("123"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Check user exists, throw error when database operation fails")
    void checkUserIfExists_ThrowError_WhenDatabaseOperationFails() {
        when(userRepository.existsById(anyString())).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(userService.checkIfUserExists("123"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Save user, return user when user saved successfully")
    void saveUser_ReturnUser_WhenUserSavedSuccessfully() {
        User user = new User("123");
        when(userRepository.save(user)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.saveUser(user))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    @DisplayName("Save user, throw error when database operation fails")
    void saveUser_ThrowError_WhenDatabaseOperationFails() {
        User user = new User("123");
        when(userRepository.save(user)).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(userService.saveUser(user))
                .expectError(RuntimeException.class)
                .verify();
    }
}
