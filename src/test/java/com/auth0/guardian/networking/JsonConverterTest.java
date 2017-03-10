/*
 * Copyright (c) 2017 Auth0 (http://auth0.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.auth0.guardian.networking;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JsonConverterTest {

    private static final String JSON_OBJECT_TWO_FIELDS_REGEXP = "\\{\\s*?\\\"\\w+?\\\"\\s*?:\\s*?.+?,\\s*?\\\"\\w+?\\\"\\s*?:\\s*?.+\\s*?\\}";

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

        String serialized = new String(converter.serialize(dummyObject));
        assertThat(serialized, startsWith("{"));
        assertThat(serialized, endsWith("}"));
        assertThat(serialized, matchesPattern(JSON_OBJECT_TWO_FIELDS_REGEXP));
        assertThat(serialized, containsString("\"someString\":\"theString\""));
        assertThat(serialized, containsString("\"someInteger\":456"));
    }

    @Test
    public void shouldSerializeMap() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("someString", "theString");
        map.put("someInteger", 456);
        String serialized = new String(converter.serialize(map));
        assertThat(serialized, startsWith("{"));
        assertThat(serialized, endsWith("}"));
        assertThat(serialized, matchesPattern(JSON_OBJECT_TWO_FIELDS_REGEXP));
        assertThat(serialized, containsString("\"someString\":\"theString\""));
        assertThat(serialized, containsString("\"someInteger\":456"));
    }

    @Test
    public void shouldParseClass() throws Exception {
        DummyObject parsed = converter.parse(DummyObject.class, new StringReader("{\"someString\":\"theStringValue\",\"someInteger\":123}"));
        assertThat(parsed.someString, is(equalTo("theStringValue")));
        assertThat(parsed.someInteger, is(equalTo(123)));
    }

    @Test
    public void shouldParseGenericMap() throws Exception {
        Map<Object, Object> parsed = converter.parse(Map.class, new StringReader("{\"someString\":\"theStringValue\",\"someNumber\":123.3}"));
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