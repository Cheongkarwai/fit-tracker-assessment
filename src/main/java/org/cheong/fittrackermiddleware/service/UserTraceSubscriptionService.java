package org.cheong.fittrackermiddleware.service;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.adapter.FitTrackerWebSocketAdapter;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author cheong
 * Service for user fitness trace subscription
 */
@Slf4j
@Service
@DependsOn("createTablePartitionScheduler")
public class UserTraceSubscriptionService {

    private final FitTrackerWebSocketAdapter fitTrackerWebSocketAdapter;
    private final UserFitnessService userFitnessService;
    private final RedissonReactiveClient redissonReactiveClient;
    private final AtomicBoolean isSubscribed = new AtomicBoolean(false);

    /**
     * Constructor for UserTraceSubscriptionService
     * @param fitTrackerWebSocketAdapter fitTrackerWebSocketAdapter
     * @param userFitnessService userFitnessService
     * @param redissonReactiveClient redissonReactiveClient
     */
    public UserTraceSubscriptionService(FitTrackerWebSocketAdapter fitTrackerWebSocketAdapter,
                                        UserFitnessService userFitnessService,
                                        RedissonReactiveClient redissonReactiveClient) {
        this.fitTrackerWebSocketAdapter = fitTrackerWebSocketAdapter;
        this.userFitnessService = userFitnessService;
        this.redissonReactiveClient = redissonReactiveClient;
    }

    /**
     * Start establishing subscription to Fit Tracker server and maintain lock to prevent multiple instances from running concurrently
     * Also ensure that subscription is only started once by checking isSubscribed AtomicBoolean
     * @return Mono<Void>
     */
    public Mono<Void> startSubscription() {
        if (isSubscribed.get()) {
            log.info("Subscription already started");
            return Mono.empty();
        }

        RLockReactive rLock = redissonReactiveClient.getLock("fit-tracker-subscription-lock");
        return rLock.tryLock(10, TimeUnit.SECONDS)
                .filter(acquired -> acquired)
                .doOnNext(acquired -> log.info("Subscription lock acquired={}", acquired))
                .flatMap(acquired -> {
                    if (isSubscribed.compareAndSet(false,true)) {
                        return fitTrackerWebSocketAdapter
                                .getTraces()
                                .flatMap(userFitnessService::saveUserFitnessRealTimeMetadata)
                                .doOnNext(trace -> log.info("Saved user fitness metadata={}", trace))
                                .then();
                    }
                    return Mono.empty();
                })
                .onErrorResume(err -> {
                    isSubscribed.set(false);
                    return rLock.unlock();
                });
    }

    /**
     * Release the lock when application context is closed
     */
    @EventListener(ContextClosedEvent.class)
    public void cleanUp() {
        if (redissonReactiveClient.isShutdown()) {
            return;
        }
        RLockReactive rLock = redissonReactiveClient.getLock("fit-tracker-subscription-lock");
        rLock.forceUnlock()
                .subscribe();
    }
}
