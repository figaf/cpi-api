package com.figaf.integration.cpi.client.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectMapperFactory {

    private static final ObjectMapper JSON_OBJECT_MAPPER;

    private static final XmlMapper XML_OBJECT_MAPPER;

    static {
        JSON_OBJECT_MAPPER = createJsonObjectMapper();
        XML_OBJECT_MAPPER = createXmlObjectMapper();
    }

    public static ObjectMapper getJsonObjectMapper() {
        return JSON_OBJECT_MAPPER;
    }

    public static XmlMapper getXmlObjectMapper() {
        return XML_OBJECT_MAPPER;
    }

    private static ObjectMapper createJsonObjectMapper() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return jsonObjectMapper;
    }

    private static XmlMapper createXmlObjectMapper() {
        XmlMapper xmlObjectMapper = new XmlMapper();
        xmlObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xmlObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        xmlObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return xmlObjectMapper;
    }
}
