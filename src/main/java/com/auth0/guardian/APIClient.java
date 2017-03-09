package com.auth0.guardian;

import com.auth0.guardian.networking.Request;
import com.auth0.guardian.networking.RequestFactory;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

class APIClient {

    private final HttpUrl baseUrl;
    private final RequestFactory requestFactory;

    APIClient(HttpUrl baseUrl) {
        this(baseUrl, new OkHttpClient());
    }

    APIClient(HttpUrl baseUrl, OkHttpClient client) {
        this(baseUrl, new RequestFactory(client));
    }

    APIClient(HttpUrl baseUrl, RequestFactory requestFactory) {
        this.baseUrl = baseUrl;
        this.requestFactory = requestFactory;
    }

    Request<StartFlowResponse> startFlow(String ticket) {
        return requestFactory
                .newRequest("POST", baseUrl.resolve("api/start-flow"), StartFlowResponse.class)
                .setHeader("Authorization", String.format("Ticket id=\"%s\"", ticket))
                .setParameter("state_transport", "polling");
    }

    Request<Object> sendEnrollSMS(String transactionToken, String deviceAccountId, String phoneNumber) {
        return requestFactory
                .newRequest("POST", baseUrl.resolve(String.format("api/device-accounts/%s/sms-enroll", deviceAccountId)), Object.class)
                .setHeader("Authorization", String.format("Bearer %s", transactionToken))
                .setParameter("phone_number", phoneNumber);
    }

    Request<Object> verifyOTP(String transactionToken, String otp) {
        return requestFactory
                .newRequest("POST", baseUrl.resolve("api/verify-otp"), Object.class)
                .setHeader("Authorization", String.format("Bearer %s", transactionToken))
                .setParameter("type", "manual_input")
                .setParameter("code", otp);
    }
}
