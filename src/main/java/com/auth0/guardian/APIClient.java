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

package com.auth0.guardian;

import com.auth0.guardian.networking.Request;
import com.auth0.guardian.networking.RequestFactory;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class APIClient {

    private final HttpUrl baseUrl;
    private final RequestFactory requestFactory;

    public APIClient(HttpUrl baseUrl) {
        this(baseUrl, new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build());
    }

    public APIClient(HttpUrl baseUrl, OkHttpClient client) {
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

    Request<Void> sendEnrollSMS(String transactionToken, String deviceAccountId, String phoneNumber) {
        return requestFactory
                .newRequest("POST", baseUrl.resolve(String.format("api/device-accounts/%s/sms-enroll", deviceAccountId)), Void.class)
                .setHeader("Authorization", String.format("Bearer %s", transactionToken))
                .setParameter("phone_number", phoneNumber);
    }

    Request<Void> verifyOTP(String transactionToken, String otp) {
        return requestFactory
                .newRequest("POST", baseUrl.resolve("api/verify-otp"), Void.class)
                .setHeader("Authorization", String.format("Bearer %s", transactionToken))
                .setParameter("type", "manual_input")
                .setParameter("code", otp);
    }
}
