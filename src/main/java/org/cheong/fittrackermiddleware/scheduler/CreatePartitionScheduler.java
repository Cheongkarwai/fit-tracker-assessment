package org.cheong.fittrackermiddleware.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.repository.UserFitnessMetadataRepository;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class CreatePartitionScheduler {

    private final UserFitnessMetadataRepository userFitnessMetadataRepository;

    private final RedissonReactiveClient redissonReactiveClient;

    public CreatePartitionScheduler(UserFitnessMetadataRepository userFitnessMetadataRepository,
                                    RedissonReactiveClient redissonReactiveClient) {
        this.userFitnessMetadataRepository = userFitnessMetadataRepository;
        this.redissonReactiveClient = redissonReactiveClient;
    }

    @Scheduled(cron = "${cron.expression.create-user-metadata-partition:0 0 0 1 * *}")
    public void createUserMetadataPartition(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        LocalDate firstDayOfNextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);

        RLockReactive rLockReactive = redissonReactiveClient.getLock("create-user-metadata-partition-lock");

        rLockReactive.tryLock()
                .filter(acquired-> acquired)
                .doOnNext(acquired-> log.info("Creating user metadata partition for two month: {} {}", formatter.format(firstDayOfCurrentMonth), formatter.format(firstDayOfNextMonth)))
                .flatMap(acquired-> this.userFitnessMetadataRepository.createUserFitnessMetadataTablePartition(formatter.format(firstDayOfCurrentMonth), firstDayOfCurrentMonth, firstDayOfCurrentMonth.plusMonths(1).withDayOfMonth(1)))
                .then(this.userFitnessMetadataRepository.createUserFitnessMetadataTablePartition(formatter.format(firstDayOfNextMonth), firstDayOfNextMonth, firstDayOfNextMonth.plusMonths(1).withDayOfMonth(1)))
                .onErrorResume(err -> {
                    log.error("Error occurred while creating user metadata partition", err);
                    return rLockReactive.unlock();
                })
                .doOnTerminate(rLockReactive::unlock)
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
