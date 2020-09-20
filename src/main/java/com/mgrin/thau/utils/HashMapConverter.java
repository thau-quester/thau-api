package com.mgrin.thau.utils;

import java.io.IOException;
import java.util.Map;

import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashMapConverter.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> customerInfo) {
        try {
            return mapper.writeValueAsString(customerInfo);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to pars JSON", e);
            return null;
        }
    }

    public String convertMapToJSONString(Map<String, String> data) {
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to pars JSON", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String customerInfoJSON) {
        Map<String, Object> map = null;
        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
            };
            map = mapper.readValue(customerInfoJSON, typeRef);
        } catch (final IOException e) {
            LOGGER.error("JSON reading error", e);
        }

        return map;
    }

    public Map<String, String> convertJSONStringToMap(String json)
            throws JsonMappingException, JsonProcessingException {
        return mapper.readValue(json, Map.class);
    }

    public static Map<String, Object> convertObjectToMap(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
        };
        return mapper.convertValue(object, typeRef);
    }
}