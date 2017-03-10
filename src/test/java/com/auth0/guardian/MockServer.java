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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MockServer {

    public static final String START_FLOW_VALID = "src/test/resources/start_flow_valid.json";
    public static final String START_FLOW_CONFIRMED = "src/test/resources/start_flow_confirmed.json";
    public static final String SMS_ENROLL_VALID = "src/test/resources/sms_enroll_valid.json";

    public static final String ERROR_500 = "src/test/resources/error_500.txt";
    public static final String ERROR_INVALID_OTP = "src/test/resources/error_code_invalid_otp.json";
    public static final String ERROR_DEVICE_ACCOUNT_NOT_FOUND = "src/test/resources/error_code_device_account_not_found.json";
    public static final String ERROR_ENROLLMENT_NOT_FOUND = "src/test/resources/error_code_enrollment_not_found.json";
    public static final String ERROR_LOGIN_TRANSACTION_NOT_FOUND = "src/test/resources/error_code_login_transaction_not_found.json";
    public static final String ERROR_INVALID_TOKEN = "src/test/resources/error_code_invalid_token.json";

    private final MockWebServer server;

    public MockServer() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    public void stop() throws IOException {
        server.shutdown();
    }

    public HttpUrl getBaseUrl() {
        return server.url("/");
    }

    public RecordedRequest takeRequest() throws InterruptedException {
        return server.takeRequest();
    }

    private String readTextFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    public void jsonResponse(String path, int statusCode) throws IOException {
        MockResponse response = new MockResponse()
                .setResponseCode(statusCode)
                .addHeader("Content-Type", "application/json")
                .setBody(readTextFile(path));
        server.enqueue(response);
    }

    public void textResponse(String path, int statusCode) throws IOException {
        MockResponse response = new MockResponse()
                .setResponseCode(statusCode)
                .addHeader("Content-Type", "text/plain")
                .setBody(readTextFile(path));
        server.enqueue(response);
    }

    public void emptyResponse() throws IOException {
        MockResponse response = new MockResponse()
                .setResponseCode(204)
                .addHeader("Content-Type", "application/json");
        server.enqueue(response);
    }

    public static Map<String, Object> bodyFromRequest(RecordedRequest request) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        MapType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        Buffer body = request.getBody();
        try {
            return mapper.readValue(body.inputStream(), mapType);
        } catch (IOException e) {
            throw e;
        } finally {
            body.close();
        }
    }
}
