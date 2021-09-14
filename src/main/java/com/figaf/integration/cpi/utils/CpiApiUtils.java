package com.figaf.integration.cpi.utils;

import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Arsenii Istlentev
 */
public class CpiApiUtils {

    private static final transient FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static Date parseDate(String date) {
        try {
            if (date == null) {
                return null;
            }
            if (date.matches(".*Date\\(.*\\).*")) {
                return new Timestamp(
                        Long.parseLong(
                                date.replaceAll("[^0-9]", "")
                        )
                );
            } else {
                return DATE_FORMAT.parse(date);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Can't parse date: ", ex);
        }
    }
}
