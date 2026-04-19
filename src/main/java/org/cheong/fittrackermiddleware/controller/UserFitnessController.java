package org.cheong.fittrackermiddleware.controller;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.dto.UserFitnessDTO;
import org.cheong.fittrackermiddleware.service.UserFitnessService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/{version}/users")
public class UserFitnessController {

    private final UserFitnessService userFitnessService;

    public UserFitnessController(UserFitnessService userFitnessService) {
        this.userFitnessService = userFitnessService;
    }

    @GetMapping(value = "/{userId}/fitness", version = "v1")
    public Mono<UserFitnessDTO> getUserFitness(@PathVariable String userId,
                                               @RequestParam LocalDate date) {
        return userFitnessService.calculateUserFitness(userId, date);
    }
}
