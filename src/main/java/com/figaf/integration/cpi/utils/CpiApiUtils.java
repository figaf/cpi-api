package com.figaf.integration.cpi.utils;

import com.figaf.integration.cpi.entity.message_processing.CustomHeaderProperty;
import org.apache.commons.lang3.time.FastDateFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public static List<CustomHeaderProperty> parseCustomerHeaderProperties(JSONObject messageProcessingLogElement) {
        List<CustomHeaderProperty> customHeaderProperties = new ArrayList<>();
        if (messageProcessingLogElement.isNull("CustomHeaderProperties")) {
            return customHeaderProperties;
        }
        JSONObject jsonObject = messageProcessingLogElement.getJSONObject("CustomHeaderProperties");
        if (jsonObject.isNull("results")) {
            return customHeaderProperties;
        }
        JSONArray results = jsonObject.getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            String id = (String) result.get("Id");
            String name = (String) result.get("Name");
            String value = (String) result.get("Value");
            CustomHeaderProperty customHeaderProperty = new CustomHeaderProperty(id, name, value);
            customHeaderProperties.add(customHeaderProperty);
        }
        return customHeaderProperties;
    }
}
