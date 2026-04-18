package org.cheong.fittrackermiddleware;

import org.cheong.fittrackermiddleware.adapter.FitTrackerAdapter;
import org.cheong.fittrackermiddleware.adapter.FitTrackerAuthorizedAdapter;
import org.cheong.fittrackermiddleware.dto.HealthResponse;
import org.cheong.fittrackermiddleware.dto.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
public class TestController {

    private final FitTrackerAuthorizedAdapter fitTrackerAdapter;

    public TestController(FitTrackerAuthorizedAdapter fitTrackerAdapter) {
        this.fitTrackerAdapter = fitTrackerAdapter;
    }

    @GetMapping("/{userId}")
    public Mono<UserResponse> getUserHealth(@PathVariable String userId) {
        return fitTrackerAdapter.getUser(userId);
    }
}
