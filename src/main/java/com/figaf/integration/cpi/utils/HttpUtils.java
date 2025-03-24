package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.exception.ClientIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


@Slf4j
public class HttpUtils {

    private static final int MAX_ATTEMPTS = 5;

    private static final long INITIAL_DELAY = 2000L;

    public static <T> Optional<T> executeHttpCallWithRetry(Callable<T> operation) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return Optional.ofNullable(operation.call());
            } catch (HttpClientErrorException.TooManyRequests ex) {
                if (attempt < MAX_ATTEMPTS) {
                    safeHandleTooManyRequests(ex, attempt);
                }
            } catch (HttpClientErrorException.NotFound ex) {
                log.warn("Resource not found: {}", ex.getMessage());
                return Optional.empty();
            } catch (Exception ex) {
                throw new ClientIntegrationException(ex);
            }
        }
        log.error("Max retry attempts exceeded");
        return Optional.empty();
    }

    public static <T> T executeWithExceptionHandling(Supplier<T> action, String errorMessagePrefix) {
        try {
            return action.get();
        } catch (Exception ex) {
            String errorMessage = String.format("%s: %s", errorMessagePrefix, ex.getMessage());
            throw new ClientIntegrationException(errorMessage, ex);
        }
    }

    public static <T> T executeAndHandleNotFound(Supplier<T> action, String notFoundLogMessage) {
        try {
            return action.get();
        } catch (ClientIntegrationException ex) {
            if ((ex.getCause() != null && isNotFound(ex.getCause()))) {
                log.warn(notFoundLogMessage);
                return null;
            }
            String errorMessage = String.format("%s: %s", notFoundLogMessage, ex.getMessage());
            throw new ClientIntegrationException(errorMessage, ex);
        }
    }

    public static <T> void assertSuccessfulResponse(ResponseEntity<T> responseEntity, String operation, String context) {
        int statusCode = responseEntity.getStatusCode().value();
        switch (statusCode) {
            case 200, 202, 204 -> log.debug("{} operation on {} was successful", operation, context);
            default -> throw new ClientIntegrationException(String.format(
                "%s operation failed for %s : Code: %d, Message: %s",
                operation,
                context,
                statusCode,
                responseEntity.getBody()
            ));
        }
    }

    private static void handleTooManyRequests(HttpClientErrorException.TooManyRequests tooManyRequestsEx, int attempt) throws Exception {
        if (tooManyRequestsEx.getResponseHeaders() != null) {
            String retryAfter = tooManyRequestsEx.getResponseHeaders().getFirst("Retry-After");
            if (retryAfter != null) {
                try {
                    long retryAfterSeconds = Long.parseLong(retryAfter);
                    log.warn("Rate limit exceeded. Retrying after {} seconds (attempt {}/{})", retryAfterSeconds, attempt, HttpUtils.MAX_ATTEMPTS);
                    TimeUnit.SECONDS.sleep(retryAfterSeconds);
                    return;
                } catch (NumberFormatException nfe) {
                    log.error("Invalid Retry-After header value: {}", retryAfter, nfe);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ie;
                }
            }
        }
        applyDefaultSleep(attempt);
    }

    private static void applyDefaultSleep(int attempt) throws Exception {
        try {
            log.warn(
                "Rate limit exceeded. Retrying after {} milliseconds (attempt {}/{})",
                HttpUtils.INITIAL_DELAY,
                attempt,
                HttpUtils.MAX_ATTEMPTS
            );
            TimeUnit.MILLISECONDS.sleep(HttpUtils.INITIAL_DELAY);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw ex;
        }
    }

    private static void safeHandleTooManyRequests(HttpClientErrorException.TooManyRequests ex, int attempt) {
        try {
            handleTooManyRequests(ex, attempt);
        } catch (Exception e) {
            throw new ClientIntegrationException(e);
        }
    }

    private static boolean isNotFound(Throwable throwable) {
        return throwable instanceof HttpClientErrorException httpEx
            && httpEx.getStatusCode() == HttpStatus.NOT_FOUND;
    }
}