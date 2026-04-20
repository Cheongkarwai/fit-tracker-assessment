package org.cheong.fittrackermiddleware.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.adapter.AuthorizedFitTrackerAdapter;
import org.cheong.fittrackermiddleware.dto.UserResponse;
import org.cheong.fittrackermiddleware.model.User;
import org.cheong.fittrackermiddleware.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author cheong
 * Service for user management
 */
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final AuthorizedFitTrackerAdapter authorizedFitTrackerAdapter;

    /**
     * Constructor for UserService
     * @param userRepository userRepository
     * @param authorizedFitTrackerAdapter authorizedFitTrackerAdapter
     */
    public UserService(UserRepository userRepository,
                       AuthorizedFitTrackerAdapter authorizedFitTrackerAdapter){
        this.userRepository = userRepository;
        this.authorizedFitTrackerAdapter = authorizedFitTrackerAdapter;
    }

    /**
     * Mark the user as new and save user to database
     * @param user user
     * @return Mono<User>
     */
    public Mono<User> saveUser(User user) {
        user.setNew(true);
        return userRepository.save(user);
    }

    /**
     * Check if user exists in database
     * @param userId identifier of the user
     * @return Mono<Boolean>
     */
    @CircuitBreaker(name = "cache-client", fallbackMethod = "checkIfUserExistsFallback")
    @Retry(name = "cache-client", fallbackMethod = "checkIfUserExistsFallback")
    @Cacheable(value = "user-exists", key = "#userId")
    public Mono<Boolean> checkIfUserExists(String userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Fallback method for checkIfUserExists
     * @param userId identifier of the user
     * @param throwable Throwable
     * @return Mono<Boolean>
     */
    public Mono<Boolean> checkIfUserExistsFallback(String userId, Throwable throwable){
        log.error("Redis server is unavailable", throwable);
        return userRepository.existsById(userId);
    }

    /**
     * Get user from Fit Tracker server
     * @param userId identifier of the user
     * @return Mono<UserResponse>
     */
    public Mono<UserResponse> getFitTrackerUser(String userId) {
        return authorizedFitTrackerAdapter.getUser(userId)
                .doOnNext(userResponse -> log.info("Get user from fit tracker={}", userResponse));
    }
}
