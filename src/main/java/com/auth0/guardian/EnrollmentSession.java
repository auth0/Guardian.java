package com.auth0.guardian;

import okhttp3.HttpUrl;

import java.io.IOException;

public class EnrollmentSession {

    private final HttpUrl baseUrl;
    private final String ticket;
    private final APIClient apiClient;

    private TransactionInfo transactionInfo;

    public EnrollmentSession(String baseUrl, String ticket) {
        this.baseUrl = HttpUrl.parse(baseUrl);
        this.ticket = ticket;

        this.apiClient = new APIClient(this.baseUrl);
    }

    public TOTPData requestEnroll(EnrollmentType type) throws IOException, IllegalStateException, GuardianException {
        if (transactionInfo == null) {
            transactionInfo = apiClient
                    .startFlow(ticket)
                    .execute();
        }

        ensureValidTransaction(transactionInfo);

        if (type instanceof EnrollmentType.SMS) {
            EnrollmentType.SMS smsEnrollmentRequest = (EnrollmentType.SMS) type;
            apiClient
                    .sendEnrollSMS(
                            transactionInfo.getTransactionToken(),
                            transactionInfo.getDeviceAccount().getId(),
                            smsEnrollmentRequest.getPhoneNumber())
                    .execute();
            return null;
        } else {
            return new TOTPData(transactionInfo.getDeviceAccount().getOtpSecret());
        }
    }

    public String confirmEnroll(String otp) throws IOException, IllegalStateException, GuardianException {
        ensureValidTransaction(transactionInfo);

        apiClient
                .verifyOTP(transactionInfo.getTransactionToken(), otp)
                .execute();

        return transactionInfo.getDeviceAccount().getRecoveryCode();
    }

    private static void ensureValidTransaction(TransactionInfo transactionInfo) throws IllegalStateException, GuardianException {
        if (transactionInfo == null || transactionInfo.getDeviceAccount() == null) {
            throw new IllegalStateException("Enrollment transaction has not been started");
        }

        if (!"confirmation_pending".equals(transactionInfo.getDeviceAccount().getStatus())) {
            throw GuardianException.alreadyEnrolled();
        }
    }
}
