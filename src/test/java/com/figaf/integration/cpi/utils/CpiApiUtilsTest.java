package com.figaf.integration.cpi.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CpiApiUtilsTest {

    @Test
    void testWithZNoFractionalSeconds() {
        String dateStr = "2024-12-18T10:15:30Z";

        Date result = CpiApiUtils.parseDate(dateStr);

        assertNotNull(result, "Result should not be null");
        assertEquals(Instant.parse("2024-12-18T10:15:30Z").toEpochMilli(), result.toInstant().toEpochMilli());
    }

    @Test
    void testWithZFractionalSeconds() {
        String dateStr = "2024-11-13T08:36:29Z";

        Date result = CpiApiUtils.parseDate(dateStr);

        assertNotNull(result);
        assertEquals(Instant.parse("2024-11-13T08:36:29Z").toEpochMilli(), result.toInstant().toEpochMilli());
    }

    @Test
    void testWithoutZNoFractionalSeconds() {
        String dateStr = "2024-12-18T10:15:30";

        Date result = CpiApiUtils.parseDate(dateStr);

        assertNotNull(result);
        assertEquals(Instant.parse("2024-12-18T10:15:30Z").toEpochMilli(), result.toInstant().toEpochMilli());
    }

    @Test
    void testWithoutZFractionalSeconds() {
        String dateStr = "2024-12-18T10:15:30.123";

        Date result = CpiApiUtils.parseDate(dateStr);

        assertNotNull(result);
        assertEquals(Instant.parse("2024-12-18T10:15:30.123Z").toEpochMilli(), result.toInstant().toEpochMilli());
    }
}
