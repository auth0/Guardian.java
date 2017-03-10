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

import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.auth0.guardian.MockServer.bodyFromRequest;
import static com.auth0.guardian.RecordedRequestMatcher.hasHeader;
import static com.auth0.guardian.RecordedRequestMatcher.hasMethodAndPath;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class APIClientTest {

    private static final String ENROLLMENT_TICKET = "ENROLLMENT_TICKET";
    private static final String TRANSACTION_TOKEN = "TRANSACTION_TOKEN";
    private static final String DEVICE_ACCOUNT_ID = "DEVICE_ACCOUNT_ID";
    private static final String PHONE_NUMBER = "PHONE_NUMBER";
    private static final String OTP_CODE = "OTP_CODE";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private MockServer server;
    private APIClient apiClient;

    @Before
    public void setUp() throws Exception {
        server = new MockServer();
        apiClient = new APIClient(server.getBaseUrl());
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void shouldStartFlow() throws Exception {
        server.jsonResponse(MockServer.START_FLOW_VALID, 201);

        StartFlowResponse startFlowResponse = apiClient
                .startFlow(ENROLLMENT_TICKET)
                .execute();

        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/start-flow"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json; charset=utf-8"));
        assertThat(recordedRequest, hasHeader("Authorization", "Ticket id=\"ENROLLMENT_TICKET\""));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body, hasEntry("state_transport", (Object) "polling"));

        assertThat(startFlowResponse.getTransactionToken(), is(equalTo("THE_TRANSACTION_TOKEN")));
        assertThat(startFlowResponse.getDeviceAccount(), is(notNullValue()));
        assertThat(startFlowResponse.getDeviceAccount().getId(), is(equalTo("THE_ENROLLMENT_ID")));
        assertThat(startFlowResponse.getDeviceAccount().getStatus(), is(equalTo("confirmation_pending")));
        assertThat(startFlowResponse.getDeviceAccount().getOtpSecret(), is(equalTo("THE_OTP_SECRET")));
        assertThat(startFlowResponse.getDeviceAccount().getRecoveryCode(), is(equalTo("THE_RECOVERY_CODE")));
    }

    @Test
    public void shouldStartFlowAlreadyEnrolled() throws Exception {
        server.jsonResponse(MockServer.START_FLOW_CONFIRMED, 201);

        StartFlowResponse startFlowResponse = apiClient
                .startFlow(ENROLLMENT_TICKET)
                .execute();

        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/start-flow"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json; charset=utf-8"));
        assertThat(recordedRequest, hasHeader("Authorization", "Ticket id=\"ENROLLMENT_TICKET\""));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body, hasEntry("state_transport", (Object) "polling"));

        assertThat(startFlowResponse.getTransactionToken(), is(equalTo("THE_TRANSACTION_TOKEN")));
        assertThat(startFlowResponse.getDeviceAccount(), is(notNullValue()));
        assertThat(startFlowResponse.getDeviceAccount().getId(), is(equalTo("THE_ENROLLMENT_ID")));
        assertThat(startFlowResponse.getDeviceAccount().getStatus(), is(equalTo("confirmed")));
        assertThat(startFlowResponse.getDeviceAccount().getOtpSecret(), is(nullValue()));
        assertThat(startFlowResponse.getDeviceAccount().getRecoveryCode(), is(nullValue()));
    }

    @Test
    public void shouldSendEnrollSMS() throws Exception {
        server.jsonResponse(MockServer.SMS_ENROLL_VALID, 200);

        apiClient
                .sendEnrollSMS(TRANSACTION_TOKEN, DEVICE_ACCOUNT_ID, PHONE_NUMBER)
                .execute();

        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/device-accounts/DEVICE_ACCOUNT_ID/sms-enroll"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json; charset=utf-8"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer TRANSACTION_TOKEN"));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body, hasEntry("phone_number", (Object) "PHONE_NUMBER"));
    }

    @Test
    public void shouldVerifyOTP() throws Exception {
        server.emptyResponse();

        apiClient
                .verifyOTP(TRANSACTION_TOKEN, OTP_CODE)
                .execute();

        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/verify-otp"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json; charset=utf-8"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer TRANSACTION_TOKEN"));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body, hasEntry("code", (Object) "OTP_CODE"));
        assertThat(body, hasEntry("type", (Object) "manual_input"));
    }

    @Test
    public void shouldThrowUnkownParsingError() throws Exception {
        exception.expect(GuardianException.class);
        exception.expectMessage("Error parsing server error response");

        server.textResponse(MockServer.ERROR_500, 500);

        apiClient
                .startFlow(ENROLLMENT_TICKET)
                .execute();
    }

    @Test
    public void shouldThrowDeviceAccountNotFound() throws Exception {
        exception.expect(GuardianException.class);
        exception.expectMessage("Not found");

        server.jsonResponse(MockServer.ERROR_DEVICE_ACCOUNT_NOT_FOUND, 404);

        try {
            apiClient
                    .sendEnrollSMS(TRANSACTION_TOKEN, DEVICE_ACCOUNT_ID, PHONE_NUMBER)
                    .execute();
        } catch (GuardianException e) {
            assertThat(e.getErrorCode(), is(equalTo("device_account_not_found")));
            assertThat(e.isEnrollmentNotFound(), is(equalTo(true)));
            throw e;
        }
    }

    @Test
    public void shouldThrowEnrollmentNotFound() throws Exception {
        exception.expect(GuardianException.class);
        exception.expectMessage("Not found");

        server.jsonResponse(MockServer.ERROR_ENROLLMENT_NOT_FOUND, 404);

        try {
            apiClient
                    .sendEnrollSMS(TRANSACTION_TOKEN, DEVICE_ACCOUNT_ID, PHONE_NUMBER)
                    .execute();
        } catch (GuardianException e) {
            assertThat(e.getErrorCode(), is(equalTo("enrollment_not_found")));
            assertThat(e.isEnrollmentNotFound(), is(equalTo(true)));
            throw e;
        }
    }

    @Test
    public void shouldThrowLoginTransactionNotFound() throws Exception {
        exception.expect(GuardianException.class);
        exception.expectMessage("Not found");

        server.jsonResponse(MockServer.ERROR_LOGIN_TRANSACTION_NOT_FOUND, 400);

        try {
            apiClient
                    .sendEnrollSMS(TRANSACTION_TOKEN, DEVICE_ACCOUNT_ID, PHONE_NUMBER)
                    .execute();
        } catch (GuardianException e) {
            assertThat(e.getErrorCode(), is(equalTo("login_transaction_not_found")));
            assertThat(e.isLoginTransactionNotFound(), is(equalTo(true)));
            throw e;
        }
    }

    @Test
    public void shouldThrowInvalidOTP() throws Exception {
        exception.expect(GuardianException.class);
        exception.expectMessage("Invalid OTP");

        server.jsonResponse(MockServer.ERROR_INVALID_OTP, 401);

        try {
            apiClient
                    .verifyOTP(TRANSACTION_TOKEN, OTP_CODE)
                    .execute();
        } catch (GuardianException e) {
            assertThat(e.getErrorCode(), is(equalTo("invalid_otp")));
            assertThat(e.isInvalidOTP(), is(equalTo(true)));
            throw e;
        }
    }

    @Test
    public void shouldThrowInvalidToken() throws Exception {
        exception.expect(GuardianException.class);
        exception.expectMessage("Unauthorized");

        server.jsonResponse(MockServer.ERROR_INVALID_TOKEN, 400);

        try {
            apiClient
                    .verifyOTP(TRANSACTION_TOKEN, OTP_CODE)
                    .execute();
        } catch (GuardianException e) {
            assertThat(e.getErrorCode(), is(equalTo("invalid_token")));
            assertThat(e.isInvalidToken(), is(equalTo(true)));
            throw e;
        }
    }
}