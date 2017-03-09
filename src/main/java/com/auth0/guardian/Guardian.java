package com.auth0.guardian;

import okhttp3.HttpUrl;

import java.io.IOException;

public class Guardian {

    private final APIClient apiClient;

    /**
     * Creates an instance for a specific Guardian server URL
     *
     * @param baseUrl the Guardian server URL
     */
    public Guardian(String baseUrl) {
        HttpUrl url = HttpUrl.parse(baseUrl);
        if (url == null) {
            throw new IllegalArgumentException("Invalid base URL: " + baseUrl);
        }

        this.apiClient = new APIClient(url);
    }

    /**
     * Request to create an enrollment
     *
     * @param ticket the enrollment ticket
     * @param type   the enrollment type to request
     * @return an enrollment Transaction
     * @throws IOException       when there's a connection issue
     * @throws GuardianException when there's a Guardian specific issue
     */
    public Transaction requestEnroll(String ticket, EnrollmentType type)
            throws IOException, GuardianException {
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

    /**
     * Confirms an enrollment started with {@link Guardian#requestEnroll(String, EnrollmentType)}.
     * <p>
     * Use this method to confirm an enrollment transaction once the user scanned the QR code with a TOTP app (for a
     * transaction initiated with {@link EnrollmentType#TOTP()}) or when the user received the OTP code delivered to his
     * phone number by SMS (for a transaction initiated with {@link EnrollmentType#SMS(String)}).
     *
     * @param transaction the enrollment transaction
     * @param otp         the code obtained from the TOTP app or delivered to the phone number by SMS
     * @return extra information about the enrollment, like the recovery code
     * @throws IOException              when there's a connection issue
     * @throws IllegalArgumentException when the transaction is not valid
     * @throws GuardianException        when there's a Guardian specific issue (invalid otp for example)
     */
    public Enrollment confirmEnroll(Transaction transaction, String otp)
            throws IOException, IllegalArgumentException, GuardianException {
        if (transaction == null || transaction.getTransactionToken() == null) {
            throw new IllegalArgumentException("Invalid enrollment transaction");
        }
        if (otp == null) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        apiClient
                .verifyOTP(transaction.getTransactionToken(), otp)
                .execute();

        return new Enrollment(transaction.getRecoveryCode());
    }
}
