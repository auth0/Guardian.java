package com.auth0.guardian;

import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import static com.auth0.guardian.MockServer.bodyFromRequest;
import static com.auth0.guardian.RecordedRequestMatcher.hasHeader;
import static com.auth0.guardian.RecordedRequestMatcher.hasMethodAndPath;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

public class GuardianTest {

    private static final String ENROLLMENT_TICKET = "ENROLLMENT_TICKET";
    private static final String PHONE_NUMBER = "PHONE_NUMBER";
    private static final String OTP_CODE = "OTP_CODE";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private MockServer server;
    private Guardian guardian;

    @Before
    public void setUp() throws Exception {
        server = new MockServer();
        guardian = new Guardian(server.getBaseUrl().toString());
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void shouldFailWithNullUrl() throws Exception {
        exception.expect(NullPointerException.class);

        new Guardian(null);
    }

    @Test
    public void shouldFailWithInvalidUrl() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid base URL: some invalid URL");

        new Guardian("some invalid URL");
    }

    @Test
    public void shouldRequestEnrollWithTOTP() throws Exception {
        server.jsonResponse(MockServer.START_FLOW_VALID, 201);

        Transaction transaction = guardian
                .requestEnroll(ENROLLMENT_TICKET, EnrollmentType.TOTP());

        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/start-flow"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json; charset=utf-8"));
        assertThat(recordedRequest, hasHeader("Authorization", "Ticket id=\"ENROLLMENT_TICKET\""));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body, hasEntry("state_transport", (Object) "polling"));

        assertThat(transaction, is(notNullValue()));
        assertThat(transaction.getTransactionToken(), is(equalTo("THE_TRANSACTION_TOKEN")));
        assertThat(transaction.getRecoveryCode(), is(equalTo("THE_RECOVERY_CODE")));
        assertThat(transaction.totpURI("user", "issuer"), is(equalTo("otpauth://totp/issuer:user?secret=THE_OTP_SECRET&issuer=issuer")));
    }

    @Test
    public void shouldRequestEnrollWithSMS() throws Exception {
        server.jsonResponse(MockServer.START_FLOW_VALID, 201);
        server.jsonResponse(MockServer.SMS_ENROLL_VALID, 200);

        Transaction transaction = guardian
                .requestEnroll(ENROLLMENT_TICKET, EnrollmentType.SMS(PHONE_NUMBER));

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

        assertThat(transaction, is(notNullValue()));
        assertThat(transaction.getTransactionToken(), is(equalTo("THE_TRANSACTION_TOKEN")));
        assertThat(transaction.getRecoveryCode(), is(equalTo("THE_RECOVERY_CODE")));
        assertThat(transaction.totpURI("user", "issuer"), is(equalTo("otpauth://totp/issuer:user?secret=THE_OTP_SECRET&issuer=issuer")));
    }

    @Test
    public void shouldConfirmEnroll() throws Exception {
        server.jsonResponse(MockServer.START_FLOW_VALID, 201);
        server.jsonResponse(MockServer.VERIFY_OTP_VALID, 201);

        Transaction transaction = guardian
                .requestEnroll(ENROLLMENT_TICKET, EnrollmentType.TOTP());

        Enrollment enrollment = guardian
                .confirmEnroll(transaction, OTP_CODE);

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

        assertThat(enrollment.getRecoveryCode(), is(equalTo("THE_RECOVERY_CODE")));
    }

    @Test
    public void shouldConfirmEnrollAfterSerialization() throws Exception {
        server.jsonResponse(MockServer.START_FLOW_VALID, 201);
        server.jsonResponse(MockServer.VERIFY_OTP_VALID, 201);

        Transaction transaction = guardian
                .requestEnroll(ENROLLMENT_TICKET, EnrollmentType.TOTP());

        // save
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(transaction);
        objectOutputStream.close();
        outputStream.close();

        byte[] binaryData = outputStream.toByteArray();

        // restore
        ByteArrayInputStream inputStream = new ByteArrayInputStream(binaryData);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        Transaction restoredTransaction = (Transaction) objectInputStream.readObject();
        objectInputStream.close();
        inputStream.close();

        Enrollment enrollment = guardian
                .confirmEnroll(restoredTransaction, OTP_CODE);

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

        assertThat(enrollment.getRecoveryCode(), is(equalTo("THE_RECOVERY_CODE")));
    }

    @Test
    public void shouldFailEnrollWhenAlreadyConfirmed() throws Exception {
        exception.expect(GuardianException.class);
        exception.expectMessage("Account already has an enrollment");

        server.jsonResponse(MockServer.START_FLOW_CONFIRMED, 201);

        try {
            guardian
                    .requestEnroll(ENROLLMENT_TICKET, EnrollmentType.TOTP());
        } catch (GuardianException e) {
            assertThat(e.getErrorCode(), is(equalTo("already_enrolled")));
            assertThat(e.isAlreadyEnrolled(), is(equalTo(true)));
            throw e;
        }
    }

    @Test
    public void shouldFailConfirmationWhenTransationIsNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid enrollment transaction");

        server.jsonResponse(MockServer.VERIFY_OTP_VALID, 201);

        guardian
                .confirmEnroll(null, OTP_CODE);
    }

    @Test
    public void shouldFailConfirmationWhenTransationHasNoToken() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid enrollment transaction");

        server.jsonResponse(MockServer.VERIFY_OTP_VALID, 201);

        guardian
                .confirmEnroll(new Transaction(null, null, null), OTP_CODE);
    }

    @Test
    public void shouldFailConfirmationWhenOtpIsNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid OTP");

        server.jsonResponse(MockServer.VERIFY_OTP_VALID, 201);

        guardian
                .confirmEnroll(new Transaction("TRANSACTION_TOKEN", null, null), null);
    }
}