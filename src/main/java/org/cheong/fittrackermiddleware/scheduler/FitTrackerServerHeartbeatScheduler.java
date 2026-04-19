package org.cheong.fittrackermiddleware.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.adapter.FitTrackerAdapter;
import org.cheong.fittrackermiddleware.dto.HealthResponse;
import org.cheong.fittrackermiddleware.exception.FitTrackerServerUnavailable;
import org.cheong.fittrackermiddleware.service.UserTraceSubscriptionService;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FitTrackerServerHeartbeatScheduler {

    private final FitTrackerAdapter fitTrackerAdapter;

    private final UserTraceSubscriptionService userTraceSubscriptionService;

    private final RedissonReactiveClient redissonReactiveClient;

    public FitTrackerServerHeartbeatScheduler(FitTrackerAdapter fitTrackerAdapter,
                                              UserTraceSubscriptionService userTraceSubscriptionService,
                                              RedissonReactiveClient redissonReactiveClient) {
        this.fitTrackerAdapter = fitTrackerAdapter;
        this.userTraceSubscriptionService = userTraceSubscriptionService;
        this.redissonReactiveClient = redissonReactiveClient;
    }

    @Scheduled(cron = "${cron.expression.ping-fit-tracker-health:* * * * * *}")
    public void pingFitTrackerServer() {

        RLockReactive rLockReactive = redissonReactiveClient.getLock("ping-fit-tracker-health-lock");

        rLockReactive.tryLock(10, TimeUnit.SECONDS)
                .filter(acquired -> acquired)
                .flatMap(acquired -> {
                    log.info("Pinging Fit Tracker Server acquired lock={}", acquired);
                    return fitTrackerAdapter.pingHealth()
                            .filter(HealthResponse::isUp)
                            .flatMap(healthResponse -> userTraceSubscriptionService.startSubscription())
                            .doOnError(err -> log.error("No heartbeat from Fit Tracker Server. {}", err.getMessage()))
                            .onErrorResume(FitTrackerServerUnavailable.class, err -> userTraceSubscriptionService.startSubscription());
                })
                .onErrorResume(SchedulingException.class, err -> {
                    log.error("Error occurred in scheduler", err);
                    return rLockReactive.unlock();
                })
                .subscribe();
    }

    @EventListener(ContextClosedEvent.class)
    public void cleanUp(){
        if(redissonReactiveClient.isShutdown()) {
            return;
        }
        RLockReactive rLock = redissonReactiveClient.getLock("ping-fit-tracker-health-lock");
        rLock.forceUnlock()
                .subscribe();
    }
}
