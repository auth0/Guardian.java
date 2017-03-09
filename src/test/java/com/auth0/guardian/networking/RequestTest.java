package com.auth0.guardian.networking;

import com.auth0.guardian.GuardianException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class RequestTest {

    private static final String BASE_URL = "http://example.com";
    private static final Object BODY = new DummyBody();
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private JsonConverter converter;

    @Mock
    private OkHttpClient client;

    @Mock
    private Call call;

    @Captor
    private ArgumentCaptor<okhttp3.Request> requestCaptor;

    @Captor
    private ArgumentCaptor<Object> objectCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapCaptor;

    @Captor
    private ArgumentCaptor<Throwable> errorCaptor;

    @Captor
    private ArgumentCaptor<okhttp3.Callback> callbackCaptor;

    private Response successResponse = new Response.Builder()
            .request(new okhttp3.Request.Builder()
                    .url("https://example.com/")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .body(ResponseBody.create(MEDIA_TYPE, "{}"))
            .build();

    private Response errorResponse = new Response.Builder()
            .request(new okhttp3.Request.Builder()
                    .url("https://example.com/")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .body(ResponseBody.create(MEDIA_TYPE, "{}"))
            .build();

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(call.execute())
                .thenReturn(successResponse);

        when(converter.serialize(any(Object.class)))
                .thenReturn("{}");

        when(client.newCall(any(okhttp3.Request.class)))
                .thenReturn(call);
    }

    private Request<Object> getRequest(String method, String url) {
        Type type = new ObjectMapper().getTypeFactory().constructSimpleType(Object.class, null);
        return new Request<>(method, HttpUrl.parse(url), converter, client, type);
    }

    private String getUrl(String path) {
        return String.format("%s%s", BASE_URL, path);
    }

    @Test
    public void testHttpBaseUrl() throws Exception {
        getRequest("GET", "http://hello.example.com/something")
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.url().toString(), is(equalTo("http://hello.example.com/something")));
    }

    @Test
    public void testHttpsBaseUrl() throws Exception {
        getRequest("GET", "https://hello.example.com/something")
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.url().toString(), is(equalTo("https://hello.example.com/something")));
    }

    @Test
    public void testMethod() throws Exception {
        getRequest("METHOD", getUrl("/user/123"))
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.method(), is(equalTo("METHOD")));
        assertThat(request.url().encodedPath(), is(equalTo("/user/123")));
    }

    @Test
    public void testGetPath() throws Exception {
        getRequest("GET", getUrl("/user/123"))
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.method(), is(equalTo("GET")));
        assertThat(request.url().encodedPath(), is(equalTo("/user/123")));
    }

    @Test
    public void shouldFailGetWithBody() throws Exception {
        exception.expect(IllegalArgumentException.class);

        getRequest("GET", getUrl("/user/123"))
                .setParameter("some", "parameter")
                .execute();
    }

    @Test
    public void testDeletePath() throws Exception {
        getRequest("DELETE", getUrl("/user/123"))
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.method(), is(equalTo("DELETE")));
        assertThat(request.url().encodedPath(), is(equalTo("/user/123")));
    }

    @Test
    public void shouldNotFailIfDeleteHasBody() throws Exception {
        getRequest("DELETE", getUrl("/user/123"))
                .setParameter("some", "parameter")
                .execute();

        verify(converter).serialize(mapCaptor.capture());
        verify(converter).parse(any(Type.class), any(Reader.class));
        verifyNoMoreInteractions(converter);
        Map<String, Object> body = mapCaptor.getValue();
        assertThat(body, hasEntry("some", (Object) "parameter"));

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.method(), is(equalTo("DELETE")));
        assertThat(request.url().encodedPath(), is(equalTo("/user/123")));
    }

    @Test
    public void testPatchPath() throws Exception {
        getRequest("PATCH", getUrl("/user/123"))
                .setParameter("parameter", "value")
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.method(), is(equalTo("PATCH")));
        assertThat(request.url().encodedPath(), is(equalTo("/user/123")));
    }

    @Test
    public void shouldFailPatchWithEmptyBody() throws Exception {
        exception.expect(IllegalArgumentException.class);

        getRequest("PATCH", getUrl("/user/123"))
                .execute();
    }

    @Test
    public void testPostPath() throws Exception {
        getRequest("POST", getUrl("/user/123"))
                .setParameter("parameter", "value")
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.method(), is(equalTo("POST")));
        assertThat(request.url().encodedPath(), is(equalTo("/user/123")));
    }

    @Test
    public void shouldFailPostWithEmptyBody() throws Exception {
        exception.expect(IllegalArgumentException.class);

        getRequest("POST", getUrl("/user/123"))
                .execute();
    }

    @Test
    public void testPutPath() throws Exception {
        getRequest("PUT", getUrl("/user/123"))
                .setParameter("parameter", "value")
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.method(), is(equalTo("PUT")));
        assertThat(request.url().encodedPath(), is(equalTo("/user/123")));
    }

    @Test
    public void shouldFailPutWithEmptyBody() throws Exception {
        exception.expect(IllegalArgumentException.class);

        getRequest("PUT", getUrl("/user/123"))
                .execute();
    }

    @Test
    public void shouldNotSerializeNothing() throws Exception {
        getRequest("GET", getUrl("/user/123"))
                .execute();

        verify(converter).parse(any(Type.class), any(Reader.class));
        verifyNoMoreInteractions(converter);

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.method(), is(equalTo("GET")));
        assertThat(request.url().encodedPath(), is(equalTo("/user/123")));
    }

    @Test
    public void shouldSerializeFromParameters() throws Exception {
        getRequest("PUT", getUrl("/user/123"))
                .setParameter("string", "value")
                .setParameter("number", 123)
                .setParameter("boolean", true)
                .execute();

        verify(converter).serialize(mapCaptor.capture());
        verify(converter).parse(any(Type.class), any(Reader.class));
        verifyNoMoreInteractions(converter);
        Map<String, Object> body = mapCaptor.getValue();
        assertThat(body, hasEntry("string", (Object) "value"));
        assertThat(body, hasEntry("number", (Object) 123));
        assertThat(body, hasEntry("boolean", (Object) true));

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.method(), is(equalTo("PUT")));
        assertThat(request.url().encodedPath(), is(equalTo("/user/123")));
    }

    @Test
    public void shouldSerializeFromBodyObject() throws Exception {
        getRequest("PUT", getUrl("/user/123"))
                .setBody(BODY)
                .execute();

        verify(converter).serialize(objectCaptor.capture());
        Object body = objectCaptor.getValue();
        assertThat(body, is(instanceOf(DummyBody.class)));

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.method(), is(equalTo("PUT")));
        assertThat(request.url().encodedPath(), is(equalTo("/user/123")));
    }

    @Test
    public void shouldFailWhenAddingParametersAndAlreadyHadBody() throws Exception {
        exception.expect(IllegalArgumentException.class);

        getRequest("PUT", getUrl("/user/123"))
                .setBody(BODY)
                .setParameter("parameter", "value")
                .execute();
    }

    @Test
    public void shouldAddParametersAndRemoveItWhenNull() throws Exception {
        getRequest("PUT", getUrl("/user/123"))
                .setParameter("string", "value")
                .setParameter("number", 123)
                .setParameter("boolean", true)
                .setParameter("string", null)
                .execute();

        verify(converter).serialize(mapCaptor.capture());
        verify(converter).parse(any(Type.class), any(Reader.class));
        verifyNoMoreInteractions(converter);
        Map<String, Object> body = mapCaptor.getValue();
        assertThat(body, hasEntry("number", (Object) 123));
        assertThat(body, hasEntry("boolean", (Object) true));
        assertThat(body.size(), is(equalTo(2)));

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.method(), is(equalTo("PUT")));
        assertThat(request.url().encodedPath(), is(equalTo("/user/123")));
    }

    @Test
    public void shouldAddQueryParameter() throws Exception {
        getRequest("GET", getUrl("/user/123"))
                .setQueryParameter("string", "value")
                .setQueryParameter("number", String.valueOf(123))
                .setQueryParameter("boolean", String.valueOf(true))
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.url().queryParameter("string"), is(equalTo("value")));
        assertThat(request.url().queryParameter("number"), is(equalTo("123")));
        assertThat(request.url().queryParameter("boolean"), is(equalTo("true")));
    }

    @Test
    public void shouldAddQueryParameterAndRemoveItWhenNull() throws Exception {
        getRequest("GET", getUrl("/user/123"))
                .setQueryParameter("string", "value")
                .setQueryParameter("number", String.valueOf(123))
                .setQueryParameter("boolean", String.valueOf(true))
                .setQueryParameter("string", null)
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.url().queryParameter("string"), is(nullValue()));
        assertThat(request.url().queryParameter("number"), is(equalTo("123")));
        assertThat(request.url().queryParameter("boolean"), is(equalTo("true")));
        assertThat(request.url().querySize(), is(equalTo(2)));
    }

    @Test
    public void shouldAddHeaders() throws Exception {
        getRequest("GET", getUrl("/user/123"))
                .setHeader("header", "value")
                .setHeader("anotherHeader", "anotherValue")
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.header("header"), is(equalTo("value")));
        assertThat(request.header("anotherHeader"), is(equalTo("anotherValue")));
    }

    @Test
    public void shouldAddHeadersAndRemoveItWhenNull() throws Exception {
        getRequest("GET", getUrl("/user/123"))
                .setHeader("anotherHeader", "this should be deleted after setting same header to null")
                .setHeader("Authorization", "Bearer some_token")
                .setHeader("anotherHeader", null)
                .execute();

        verify(client).newCall(requestCaptor.capture());
        okhttp3.Request request = requestCaptor.getValue();

        assertThat(request.header("Authorization"), is(equalTo("Bearer some_token")));
        assertThat(request.header("anotherHeader"), is(nullValue()));
        assertThat(request.headers().size(), is(equalTo(1)));
    }

    @Test
    public void shouldExecuteRealCall() throws Exception {
        getRequest("GET", getUrl("/user/123"))
                .execute();

        verify(call).execute();
    }

    @Test
    public void shouldParseSuccessfulResponse() throws Exception {
        when(call.execute())
                .thenReturn(successResponse);

        Object parsedResponse = new Object();

        when(converter.parse(any(Type.class), any(Reader.class)))
                .thenReturn(parsedResponse);

        Object response = getRequest("GET", getUrl("/user/123"))
                .execute();
        assertThat(response, is(sameInstance(parsedResponse)));

        verify(converter).parse(any(Type.class), any(Reader.class));
    }

    @Test
    public void shouldParseErrorResponse() throws Exception {
        when(call.execute())
                .thenReturn(errorResponse);

        Map<String, String> parsedErrorResponse = new HashMap<>();
        parsedErrorResponse.put("errorCode", "invalid_token");

        when(converter.parse(any(Type.class), any(Reader.class)))
                .thenReturn(parsedErrorResponse);

        Exception thrownException = null;
        try {
            getRequest("GET", getUrl("/something"))
                    .execute();
        } catch (Exception error) {
            thrownException = error;
        }

        verify(converter).parseMap(eq(String.class), eq(Object.class), any(Reader.class));

        assertThat(thrownException, is(notNullValue()));
        assertThat(thrownException, is(instanceOf(GuardianException.class)));
    }

    @Test
    public void shouldFailParseWithSuccessResponse() throws Exception {
        exception.expect(GuardianException.class);

        when(call.execute())
                .thenReturn(successResponse);

        when(converter.parse(any(Type.class), any(Reader.class)))
                .thenThrow(new RuntimeException());

        getRequest("GET", getUrl("/something"))
                .execute();
    }

    @Test
    public void shouldFailParseWithErrorResponse() throws Exception {
        exception.expect(GuardianException.class);

        when(call.execute())
                .thenReturn(errorResponse);

        when(converter.parse(any(Type.class), any(Reader.class)))
                .thenThrow(new RuntimeException());

        getRequest("GET", getUrl("/something"))
                .execute();
    }

    static class DummyBody {
        String someString = "someString";
    }
}