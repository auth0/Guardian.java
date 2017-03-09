package com.auth0.guardian.networking;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JsonConverterTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ObjectMapper mapper;
    private JsonConverter converter;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        converter = new JsonConverter(mapper);
    }

    @Test
    public void shouldSerializeObject() throws Exception {
        DummyObject dummyObject = new DummyObject();
        dummyObject.someString = "theString";
        dummyObject.someInteger = 456;

        String serialized = converter.serialize(dummyObject);
        assertThat(serialized, is(equalTo("{\"someString\":\"theString\",\"someInteger\":456}")));
    }

    @Test
    public void shouldSerializeMap() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("someString", "theString");
        map.put("someInteger", 456);
        String serialized = converter.serialize(map);
        assertThat(serialized, containsString("\"someString\":\"theString\""));
        assertThat(serialized, containsString("\"someInteger\":456"));
    }

    @Test
    public void shouldParseClass() throws Exception {
        Type type = mapper.getTypeFactory().constructSimpleType(DummyObject.class, null);
        DummyObject parsed = converter.parse(type, new StringReader("{\"someString\":\"theStringValue\",\"someInteger\":123}"));
        assertThat(parsed.someString, is(equalTo("theStringValue")));
        assertThat(parsed.someInteger, is(equalTo(123)));
    }

    @Test
    public void shouldParseGenericMap() throws Exception {
        Type type = mapper.getTypeFactory().constructMapType(HashMap.class, Object.class, Object.class);
        Map<Object, Object> parsed = converter.parse(type, new StringReader("{\"someString\":\"theStringValue\",\"someNumber\":123.3}"));
        assertThat(parsed, hasEntry("someString", (Object) "theStringValue"));
        assertThat(parsed, hasEntry("someNumber", (Object) 123.3));
    }

    @Test
    public void shouldParseMap() throws Exception {
        Map<String, Object> parsed = converter.parseMap(String.class, Object.class, new StringReader("{\"someString\":\"theStringValue\",\"someNumber\":123.3}"));
        assertThat(parsed, hasEntry("someString", (Object) "theStringValue"));
        assertThat(parsed, hasEntry("someNumber", (Object) 123.3));
    }

    @Test
    public void shouldCatchExceptionAndThrowIllegalArgument() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(startsWith("Couldn't create request body for data: java.lang.Object@"));

        converter.serialize(new Object());
    }

    static class DummyObject {

        @JsonProperty("someString")
        String someString;

        @JsonProperty("someInteger")
        Integer someInteger;
    }
}