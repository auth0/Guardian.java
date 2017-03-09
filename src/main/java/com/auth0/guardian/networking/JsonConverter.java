package com.auth0.guardian.networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

class JsonConverter {

    private final ObjectMapper mapper;

    JsonConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    String serialize(Object body) throws IllegalArgumentException {
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Couldn't create request body for data: " + body, e);
        }
    }

    <T> T parse(Type typeOfT, Reader reader) throws IOException {
        JavaType typeReference = mapper.getTypeFactory().constructType(typeOfT);
        return mapper.readValue(reader, typeReference);
    }

    <K, V> Map<K, V> parseMap(Class<K> classOfKey, Class<V> classOfValue, Reader reader) throws IOException {
        MapType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, classOfKey, classOfValue);
        return mapper.readValue(reader, mapType);
    }
}
