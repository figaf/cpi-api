package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.cpi.entity.message_processing.CustomHeaderProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

/**
 * @author Arsenii Istlentev
 */
public class CpiApiUtils {

    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
        .optionalStart()
        .appendPattern(".SSS")
        .optionalEnd()
        .optionalStart()
        .appendLiteral('Z')
        .optionalEnd()
        .toFormatter()
        .withZone(ZoneId.of("GMT"));

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
                ZonedDateTime zdt = ZonedDateTime.parse(date, DATE_FORMATTER);
                return Date.from(zdt.toInstant());
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

    public static Document loadXMLFromString(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new ClientIntegrationException(String.format("Can't load XML from string %s: ", xml), ex);
        }
    }

    public static String normalizeUuid(String uuid) {
        if (StringUtils.length(uuid) == 32) {
            return uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5");
        }
        return uuid;
    }

    public static String denormalizeUuid(String uuid) {
        if (StringUtils.length(uuid) == 36) {
            return uuid.replace("-", "");
        }
        return uuid;
    }

    public static boolean isDefaultRuntime(RequestContext requestContext) {
        return isDefaultRuntime(requestContext.getRuntimeLocationId(), requestContext.getDefaultRuntimeLocationId());
    }

    public static boolean isDefaultRuntime(String runtimeLocationId, String defaultRuntimeLocationId) {
        return StringUtils.isBlank(runtimeLocationId) || Objects.equals(runtimeLocationId, defaultRuntimeLocationId);
    }

}
