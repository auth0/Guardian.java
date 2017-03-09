package com.auth0.guardian;

import okhttp3.HttpUrl;

import java.io.IOException;

public class Guardian {

    private final APIClient apiClient;

    public Guardian(String baseUrl) {
        HttpUrl url = HttpUrl.parse(baseUrl);
        if (url == null) {
            throw new IllegalArgumentException("Invalid base URL: " + baseUrl);
        }

        this.apiClient = new APIClient(url);
    }

    public Transaction requestEnroll(String ticket, EnrollmentType type)
            throws IOException, IllegalStateException, GuardianException {
        StartFlowResponse startFlowResponse = apiClient
                .startFlow(ticket)
                .execute();

        if (!"confirmation_pending".equals(startFlowResponse.getDeviceAccount().getStatus())) {
            throw GuardianException.alreadyEnrolled();
        }

        if (type instanceof EnrollmentType.SMS) {
            EnrollmentType.SMS smsEnrollmentRequest = (EnrollmentType.SMS) type;
            apiClient
                    .sendEnrollSMS(
                            startFlowResponse.getTransactionToken(),
                            startFlowResponse.getDeviceAccount().getId(),
                            smsEnrollmentRequest.getPhoneNumber())
                    .execute();
        }

        return new Transaction(
                startFlowResponse.getTransactionToken(),
                startFlowResponse.getDeviceAccount().getRecoveryCode(),
                startFlowResponse.getDeviceAccount().getOtpSecret());
    }

    public Enrollment confirmEnroll(Transaction transactionInfo, String otp)
            throws IOException, IllegalArgumentException, GuardianException {
        if (transactionInfo == null || transactionInfo.getTransactionToken() == null) {
            throw new IllegalArgumentException("Invalid enrollment transaction");
        }
        if (otp == null) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        apiClient
                .verifyOTP(transactionInfo.getTransactionToken(), otp)
                .execute();

        return new Enrollment(transactionInfo.getRecoveryCode());
    }
}
