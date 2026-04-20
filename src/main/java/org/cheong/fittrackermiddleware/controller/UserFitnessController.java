package org.cheong.fittrackermiddleware.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.dto.UserFitnessDTO;
import org.cheong.fittrackermiddleware.service.UserFitnessService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * @author cheong
 * Controller for user fitness data
 */
@Slf4j
@RestController
@RequestMapping("/api/{version}/users")
public class UserFitnessController {

    private final UserFitnessService userFitnessService;

    public UserFitnessController(UserFitnessService userFitnessService) {
        this.userFitnessService = userFitnessService;
    }

    /**
     * Get user fitness data for a specific user and date
     * @param userId User ID
     * @param date Date
     * @return UserFitnessDTO
     */
    @RateLimiter(name = "user-fitness-api")
    @GetMapping(value = "/{userId}/fitness", version = "v1")
    public Mono<UserFitnessDTO> getUserFitness(@PathVariable String userId,
                                               @RequestParam LocalDate date) {
        return userFitnessService.calculateUserFitness(userId, date);
    }
}
