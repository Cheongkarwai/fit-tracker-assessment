package org.cheong.fittrackermiddleware;

import org.cheong.fittrackermiddleware.service.UserTraceSubscriptionService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FitTrackerMiddlewareApplication implements ApplicationRunner {

    private final UserTraceSubscriptionService userTraceSubscriptionService;

    public FitTrackerMiddlewareApplication(UserTraceSubscriptionService userTraceSubscriptionService) {
        this.userTraceSubscriptionService = userTraceSubscriptionService;
    }

    public static void main(String[] args) {
        SpringApplication.run(FitTrackerMiddlewareApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        userTraceSubscriptionService.startSubscription();
    }
}
