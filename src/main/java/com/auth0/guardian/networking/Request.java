package com.auth0.guardian.networking;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.auth0.guardian.GuardianException;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Request<T> {

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final JsonConverter converter;
    private final OkHttpClient client;
    private final Type typeOfT;
    private final HttpUrl url;
    private final String method;

    private Object body;
    private final Map<String, String> headers;
    private final Map<String, Object> bodyParameters;
    private final Map<String, String> queryParameters;

    Request(String method,
            HttpUrl url,
            JsonConverter converter,
            OkHttpClient client,
            Type typeOfT) {
        this.method = method;
        this.url = url;
        this.converter = converter;
        this.client = client;
        this.typeOfT = typeOfT;

        this.headers = new HashMap<>();
        this.bodyParameters = new HashMap<>();
        this.queryParameters = new HashMap<>();
    }

    public Request<T> setParameter(String name, Object value) throws IllegalArgumentException {
        if (body != null) {
            throw new IllegalArgumentException("Cannot set body and parameters at the same time");
        }
        if (value != null) {
            bodyParameters.put(name, value);
        } else {
            bodyParameters.remove(name);
        }
        return this;
    }

    public Request<T> setHeader(String name, String value) {
        if (value != null) {
            headers.put(name, value);
        } else {
            headers.remove(name);
        }
        return this;
    }

    public Request<T> setBody(Object body) {
        this.body = body;
        return this;
    }

    public T execute() throws IOException {
        Response response = buildCall().execute();
        if (response.isSuccessful()) {
            return payloadFromResponse(response);
        }

        throw exceptionFromErrorResponse(response);
    }

    private Call buildCall() {
        HttpUrl.Builder urlBuilder = url.newBuilder();

        for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                .url(urlBuilder.build());

        RequestBody requestBody = null;
        if (body != null) {
            requestBody = RequestBody.create(MEDIA_TYPE, converter.serialize(body));
        } else if (!bodyParameters.isEmpty()) {
            requestBody = RequestBody.create(MEDIA_TYPE, converter.serialize(bodyParameters));
        }

        requestBuilder.method(method, requestBody);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        return client.newCall(requestBuilder.build());
    }

    private T payloadFromResponse(Response response) throws GuardianException {
        try {
            final Reader reader = response.body().charStream();
            return converter.parse(typeOfT, reader);
        } catch (Exception e) {
            throw new GuardianException("Error parsing server response", e);
        }
    }

    private GuardianException exceptionFromErrorResponse(Response response) {
        try {
            final Reader reader = response.body().charStream();
            Map<String, Object> error = converter.parseMap(String.class, Object.class, reader);
            return new GuardianException(error);
        } catch (Exception e) {
            return new GuardianException("Error parsing server error response", e);
        }
    }
}
