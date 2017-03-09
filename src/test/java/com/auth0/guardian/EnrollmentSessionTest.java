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
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.*;

public class EnrollmentSessionTest {

    private static final String ENROLLMENT_TICKET = "ENROLLMENT_TICKET";
    private static final String PHONE_NUMBER = "PHONE_NUMBER";
    private static final String OTP_CODE = "OTP_CODE";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private MockServer server;
    private EnrollmentSession enrollmentSession;

    @Before
    public void setUp() throws Exception {
        server = new MockServer();
        enrollmentSession = new EnrollmentSession(server.getBaseUrl().toString(), ENROLLMENT_TICKET);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void shouldRequestEnrollWithTOTP() throws Exception {
        server.jsonResponse(MockServer.START_FLOW_VALID, 201);

        TOTPData totpData = enrollmentSession
                .requestEnroll(EnrollmentType.TOTP());

        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/start-flow"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json; charset=utf-8"));
        assertThat(recordedRequest, hasHeader("Authorization", "Ticket id=\"ENROLLMENT_TICKET\""));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body, hasEntry("state_transport", (Object) "polling"));

        assertThat(totpData, is(notNullValue()));
        assertThat(totpData.getSecret(), is(equalTo("THE_OTP_SECRET")));
        assertThat(totpData.getAlgorithm(), is(equalTo("sha1")));
        assertThat(totpData.getDigits(), is(equalTo(6)));
        assertThat(totpData.getPeriod(), is(equalTo(30)));
    }

    @Test
    public void shouldRequestEnrollWithSMS() throws Exception {
        server.jsonResponse(MockServer.START_FLOW_VALID, 201);
        server.jsonResponse(MockServer.SMS_ENROLL_VALID, 200);

        TOTPData totpData = enrollmentSession
                .requestEnroll(EnrollmentType.SMS(PHONE_NUMBER));

        RecordedRequest startFlowRequest = server.takeRequest();

        assertThat(startFlowRequest, hasMethodAndPath("POST", "/api/start-flow"));
        assertThat(startFlowRequest, hasHeader("Content-Type", "application/json; charset=utf-8"));
        assertThat(startFlowRequest, hasHeader("Authorization", "Ticket id=\"ENROLLMENT_TICKET\""));

        Map<String, Object> startFlowBody = bodyFromRequest(startFlowRequest);
        assertThat(startFlowBody, hasEntry("state_transport", (Object) "polling"));

        RecordedRequest sendSmsRequest = server.takeRequest();

        assertThat(sendSmsRequest, hasMethodAndPath("POST", "/api/device-accounts/THE_ENROLLMENT_ID/sms-enroll"));
        assertThat(sendSmsRequest, hasHeader("Content-Type", "application/json; charset=utf-8"));
        assertThat(sendSmsRequest, hasHeader("Authorization", "Bearer THE_TRANSACTION_TOKEN"));

        Map<String, Object> sendSmsBody = bodyFromRequest(sendSmsRequest);
        assertThat(sendSmsBody, hasEntry("phone_number", (Object) "PHONE_NUMBER"));

        assertThat(totpData, is(nullValue()));
    }

    @Test
    public void shouldConfirmEnroll() throws Exception {
        server.jsonResponse(MockServer.START_FLOW_VALID, 201);
        server.jsonResponse(MockServer.VERIFY_OTP_VALID, 201);

        enrollmentSession
                .requestEnroll(EnrollmentType.TOTP());

        String recoveryCode = enrollmentSession
                .confirmEnroll(OTP_CODE);

        RecordedRequest startFlowRequest = server.takeRequest();

        assertThat(startFlowRequest, hasMethodAndPath("POST", "/api/start-flow"));
        assertThat(startFlowRequest, hasHeader("Content-Type", "application/json; charset=utf-8"));
        assertThat(startFlowRequest, hasHeader("Authorization", "Ticket id=\"ENROLLMENT_TICKET\""));

        Map<String, Object> startFlowBody = bodyFromRequest(startFlowRequest);
        assertThat(startFlowBody, hasEntry("state_transport", (Object) "polling"));

        RecordedRequest verifyOtpRequest = server.takeRequest();

        assertThat(verifyOtpRequest, hasMethodAndPath("POST", "/api/verify-otp"));
        assertThat(verifyOtpRequest, hasHeader("Content-Type", "application/json; charset=utf-8"));
        assertThat(verifyOtpRequest, hasHeader("Authorization", "Bearer THE_TRANSACTION_TOKEN"));

        Map<String, Object> verifyOtpBody = bodyFromRequest(verifyOtpRequest);
        assertThat(verifyOtpBody, hasEntry("type", (Object) "manual_input"));
        assertThat(verifyOtpBody, hasEntry("code", (Object) "OTP_CODE"));

        assertThat(recoveryCode, is(equalTo("THE_RECOVERY_CODE")));
    }

    @Test
    public void shouldFailEnrollWhenAlreadyConfirmed() throws Exception {
        exception.expect(GuardianException.class);
        exception.expectMessage("Account already has an enrollment");

        server.jsonResponse(MockServer.START_FLOW_CONFIRMED, 201);

        try {
            enrollmentSession
                    .requestEnroll(EnrollmentType.TOTP());
        } catch (GuardianException e) {
            assertThat(e.isAlreadyEnrolled(), is(equalTo(true)));
            throw e;
        }
    }

    @Test
    public void shouldFailConfirmationWhenTransationNotStarted() throws Exception {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Enrollment transaction has not been started");

        server.jsonResponse(MockServer.VERIFY_OTP_VALID, 201);

        enrollmentSession
                .confirmEnroll(OTP_CODE);
    }
}