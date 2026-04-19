package org.cheong.fittrackermiddleware.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.cheong.fittrackermiddleware.adapter.FitTrackerAdapter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
@Slf4j
public class FitTrackerAspect {

    private final FitTrackerAdapter fitTrackerAdapter;

    public FitTrackerAspect(FitTrackerAdapter fitTrackerAdapter) {
        this.fitTrackerAdapter = fitTrackerAdapter;
    }

    @Around("@annotation(org.cheong.fittrackermiddleware.aspect.PingFitTrackerServer)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        log.info("Pinging Fit Tracker Server");

        Object result = joinPoint.proceed();

        if(result instanceof Mono<?> mono){
            return fitTrackerAdapter.pingHealth()
                    .then(mono);
        }
        if(result instanceof Flux<?> flux){
            return fitTrackerAdapter.pingHealth()
                    .thenMany(flux);
        }

        return result;
    }
}
