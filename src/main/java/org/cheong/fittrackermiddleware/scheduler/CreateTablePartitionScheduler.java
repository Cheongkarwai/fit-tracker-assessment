package org.cheong.fittrackermiddleware.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.repository.UserFitnessMetadataRepository;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CreateTablePartitionScheduler {

    private final UserFitnessMetadataRepository userFitnessMetadataRepository;

    private final RedissonReactiveClient redissonReactiveClient;

    public CreateTablePartitionScheduler(UserFitnessMetadataRepository userFitnessMetadataRepository,
                                         RedissonReactiveClient redissonReactiveClient) {
        this.userFitnessMetadataRepository = userFitnessMetadataRepository;
        this.redissonReactiveClient = redissonReactiveClient;
    }

    @Scheduled(cron = "${cron.expression.create-user-metadata-partition:0 0 0 1 * *}")
    public void createUserMetadataTablePartition(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);

        RLockReactive rLockReactive = redissonReactiveClient.getLock("create-user-metadata-partition-lock");

        rLockReactive.tryLock(10, TimeUnit.SECONDS)
                .filter(acquired-> acquired)
                .doOnNext(acquired-> log.info("Creating user metadata partition for current month: {}", formatter.format(firstDayOfCurrentMonth)))
                .flatMap(acquired-> this.userFitnessMetadataRepository.createUserFitnessMetadataTablePartition(formatter.format(firstDayOfCurrentMonth), firstDayOfCurrentMonth, firstDayOfCurrentMonth.plusMonths(1).withDayOfMonth(1)))
                .onErrorResume(SchedulingException.class, err -> {
                    log.error("Error occurred in scheduler while creating user metadata partition", err);
                    return rLockReactive.unlock();
                })
                .subscribe();
    }

    @EventListener(ContextClosedEvent.class)
    public void cleanUp(){
        if(redissonReactiveClient.isShutdown()) {
            return;
        }
        RLockReactive rLock = redissonReactiveClient.getLock("create-user-metadata-partition-lock");
        rLock.forceUnlock()
                .subscribe();
    }
}
