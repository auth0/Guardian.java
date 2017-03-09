package com.auth0.guardian.networking;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class RequestFactory {

    private final JsonConverter converter;
    private final OkHttpClient client;

    public RequestFactory(OkHttpClient client) {
        this.converter = new JsonConverter(new ObjectMapper());
        this.client = client;
    }

    public <T> Request<T> newRequest(String method,
                                     HttpUrl url,
                                     Class<T> classOfT) {
        return new Request<>(method, url, converter, client, classOfT);
    }
}
