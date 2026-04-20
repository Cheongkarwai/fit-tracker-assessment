package org.cheong.fittrackermiddleware.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.r2dbc.spi.R2dbcException;
import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.exception.UserNotFoundException;
import org.redisson.client.RedisException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.scheduling.SchedulingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.ServerWebInputException;

/**
 * @author cheong
 * Global exception handler
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle UserNotFoundException
     * @param ex Exception
     * @return ProblemDetail
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleNotFoundException(Throwable ex) {
        log.error("Handling 404 error", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(404), ex.getMessage());
        problemDetail.setTitle(ex.getMessage());
        return problemDetail;
    }

    /**
     * Handle MissingRequestValueException and ServerWebInputException
     * @param ex Exception
     * @return ProblemDetail
     */
    @ExceptionHandler({MissingRequestValueException.class, ServerWebInputException.class})
    public ProblemDetail handleBadRequestException(Throwable ex) {
        log.error("Handling 400 error", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), ex.getMessage());
        problemDetail.setTitle(ex.getMessage());
        return problemDetail;
    }

    /**
     * Handle R2dbcException, RedisException, SchedulingException, WebClientException
     * @param ex Exception
     * @return ProblemDetail
     */
    @ExceptionHandler({R2dbcException.class, RedisException.class, SchedulingException.class, WebClientException.class})
    public ProblemDetail handleInternalServerError(Throwable ex) {
        log.error("Handling server exception", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), "Internal Server Error");
        problemDetail.setTitle("Internal Server Error");
        return problemDetail;
    }

    /**
     * Handle RequestNotPermitted
     * @param ex Exception
     * @return ProblemDetail
     */
    @ExceptionHandler({RequestNotPermitted.class})
    public ProblemDetail handleRequestNotPermitted(RequestNotPermitted ex) {
        log.error("Handling 429 error", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(429), "Too Many Requests");
        problemDetail.setTitle("Too Many Requests");
        return problemDetail;
    }
}
