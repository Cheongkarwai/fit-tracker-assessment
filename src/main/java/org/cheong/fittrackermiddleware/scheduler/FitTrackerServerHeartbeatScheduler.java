package org.cheong.fittrackermiddleware.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.adapter.FitTrackerAdapter;
import org.cheong.fittrackermiddleware.dto.HealthResponse;
import org.cheong.fittrackermiddleware.service.UserTraceSubscriptionService;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author cheong
 * Scheduler for Fit Tracker server heartbeat
 */
@Slf4j
@Component
public class FitTrackerServerHeartbeatScheduler {

    private final FitTrackerAdapter fitTrackerAdapter;

    private final UserTraceSubscriptionService userTraceSubscriptionService;

    private final RedissonReactiveClient redissonReactiveClient;

    /**
     * Constructor for FitTrackerServerHeartbeatScheduler
     * @param fitTrackerAdapter Fit Tracker adapter
     * @param userTraceSubscriptionService User Trace Subscription Service
     * @param redissonReactiveClient Redisson Reactive Client from Redisson
     */
    public FitTrackerServerHeartbeatScheduler(FitTrackerAdapter fitTrackerAdapter,
                                              UserTraceSubscriptionService userTraceSubscriptionService,
                                              RedissonReactiveClient redissonReactiveClient) {
        this.fitTrackerAdapter = fitTrackerAdapter;
        this.userTraceSubscriptionService = userTraceSubscriptionService;
        this.redissonReactiveClient = redissonReactiveClient;
    }

    /**
     * Ping Fit Tracker server every 10 seconds or depends on the cron expression.
     * Ensure that only one instance can acquire the lock to prevent multiple instances from running concurrently.
     */
    @Scheduled(cron = "${cron.expression.ping-fit-tracker-health:* * * * * *}")
    public void pingFitTrackerServer() {

        RLockReactive rLockReactive = redissonReactiveClient.getLock("ping-fit-tracker-health-lock");

        rLockReactive.tryLock(10, TimeUnit.SECONDS)
                .filter(acquired -> acquired)
                .doOnNext(acquired-> log.info("Ping Fit Tracker Health lock acquired={}", acquired))
                .flatMap(acquired -> fitTrackerAdapter.pingHealth()
                        .filter(HealthResponse::isUp)
                        .flatMap(healthResponse -> userTraceSubscriptionService.startSubscription())
                        .doOnError(err -> log.error("No heartbeat from Fit Tracker Server. {}", err.getMessage())))
                .onErrorResume(SchedulingException.class, err -> {
                    log.error("Error occurred in scheduler", err);
                    return rLockReactive.unlock();
                })
                .subscribe();
    }

    /**
     * Release the lock when application context is closed
     */
    @EventListener(ContextClosedEvent.class)
    public void cleanUp() {
        if (redissonReactiveClient.isShutdown()) {
            return;
        }
        RLockReactive rLock = redissonReactiveClient.getLock("ping-fit-tracker-health-lock");
        rLock.forceUnlock()
                .subscribe();
    }
}
