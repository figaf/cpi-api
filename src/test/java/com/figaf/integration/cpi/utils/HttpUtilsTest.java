package com.figaf.integration.cpi.utils;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import java.util.Optional;
import java.util.concurrent.Callable;
import static org.junit.jupiter.api.Assertions.*;

public class HttpUtilsTest {

    @Test
    public void testSuccessfulCall() {
        Callable<String> successfulCallable = () -> "Success";

        Optional<String> result = HttpUtils.executeHttpCallWithRetry(successfulCallable);

        assertTrue(result.isPresent());
        assertEquals("Success", result.get());
    }

    @Test
    public void testTooManyRequestsThenSuccessWithValidHeader() {
        final int[] callCount = {0};
        Callable<String> callable = () -> {
            callCount[0]++;
            if (callCount[0] == 1) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Retry-After", "0");
                throw HttpClientErrorException.create(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too Many Requests",
                    headers,
                    null,
                    null
                );
            }
            return "Recovered";
        };

        Optional<String> result = HttpUtils.executeHttpCallWithRetry(callable);

        assertTrue(result.isPresent());
        assertEquals("Recovered", result.get());
    }

    @Test
    public void testTooManyRequestsAlways() {
        Callable<String> callable = () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Retry-After", "0");
            throw HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                headers,
                null,
                null
            );
        };

        Optional<String> result = HttpUtils.executeHttpCallWithRetry(callable);

        assertFalse(result.isPresent());
    }

    @Test
    public void testNotFound() {
        Callable<String> callable = () -> {
            throw HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                null,
                null
            );
        };

        Optional<String> result = HttpUtils.executeHttpCallWithRetry(callable);

        assertFalse(result.isPresent());
    }

    @Test
    public void testGenericException() {
        Callable<String> callable = () -> {
            throw new Exception("Generic error");
        };

        Optional<String> result = HttpUtils.executeHttpCallWithRetry(callable);

        assertFalse(result.isPresent());
    }

    @Test
    public void testTooManyRequestsWithInvalidHeaderThenSuccess() {
        final int[] callCount = {0};
        Callable<String> callable = () -> {
            callCount[0]++;
            if (callCount[0] == 1) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Retry-After", "invalid");
                throw HttpClientErrorException.create(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too Many Requests",
                    headers,
                    null,
                    null
                );
            }
            return "Recovered";
        };

        Optional<String> result = HttpUtils.executeHttpCallWithRetry(callable);

        assertTrue(result.isPresent());
        assertEquals("Recovered", result.get());
    }

    @Test
    public void testTooManyRequestsWithNullHeaderThenSuccess() {
        final int[] callCount = {0};
        Callable<String> callable = () -> {
            callCount[0]++;
            if (callCount[0] == 1) {
                throw HttpClientErrorException.create(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too Many Requests",
                    null,
                    null,
                    null
                );
            }
            return "Recovered";
        };

        Optional<String> result = HttpUtils.executeHttpCallWithRetry(callable);

        assertTrue(result.isPresent());
        assertEquals("Recovered", result.get());
    }
}
