package org.cheong.fittrackermiddleware.service;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.adapter.FitTrackerWebSocketAdapter;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
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

    public void startSubscription() {
        RLockReactive rLock = redissonReactiveClient.getLock("fit-tracker-subscription-lock");
        rLock.tryLock()
                .filter(acquired -> acquired)
                .doOnNext(acquired -> log.info("Acquired lock"))
                .doOnNext(trace -> log.info("Start subscription"))
                .flatMap(acquired -> fitTrackerWebSocketAdapter
                        .getTraces()
                        .flatMap(userFitnessService::saveUserFitnessRealTimeMetadata)
                        .doOnNext(System.out::println)
                        .then())
                .onErrorResume(err -> rLock.unlock())
                .doOnTerminate(rLock::unlock)
                .subscribe();
    }

    @EventListener(ContextClosedEvent.class)
    public void cleanUp(){
        if(redissonReactiveClient.isShutdown()) {
            return;
        }
        RLockReactive rLock = redissonReactiveClient.getLock("fit-tracker-subscription-lock");
        rLock.forceUnlock()
                .subscribe();
    }
}
