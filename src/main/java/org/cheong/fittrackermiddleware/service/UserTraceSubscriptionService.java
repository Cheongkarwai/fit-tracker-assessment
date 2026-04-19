package org.cheong.fittrackermiddleware.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.adapter.FitTrackerWebSocketAdapter;
import org.cheong.fittrackermiddleware.aspect.PingFitTrackerServer;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@DependsOn("createPartitionScheduler")
public class UserTraceSubscriptionService {

    private final FitTrackerWebSocketAdapter fitTrackerWebSocketAdapter;
    private final UserFitnessService userFitnessService;
    private final RedissonReactiveClient redissonReactiveClient;

    public UserTraceSubscriptionService(FitTrackerWebSocketAdapter fitTrackerWebSocketAdapter,
                                        UserFitnessService userFitnessService,
                                        RedissonReactiveClient redissonReactiveClient) {
        this.fitTrackerWebSocketAdapter = fitTrackerWebSocketAdapter;
        this.userFitnessService = userFitnessService;
        this.redissonReactiveClient = redissonReactiveClient;
    }

    @PingFitTrackerServer
    public Mono<Void> startSubscription() {
        RLockReactive rLock = redissonReactiveClient.getLock("fit-tracker-subscription-lock");
        return rLock.tryLock()
                .filter(acquired -> acquired)
                .doOnNext(acquired -> log.info("Acquired lock"))
                .doOnNext(trace -> log.info("Start subscription"))
                .flatMap(acquired -> fitTrackerWebSocketAdapter
                        .getTraces()
                        .flatMap(userFitnessService::saveUserFitnessRealTimeMetadata)
                        .doOnNext(trace -> log.info("Saved user fitness metadata={}", trace))
                        .then())
                .onErrorResume(err -> {
                    log.error("Error occurred while subscribing to user fitness metadata", err);
                    return rLock.unlock();
                })
                .doOnTerminate(rLock::unlock);
    }

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
