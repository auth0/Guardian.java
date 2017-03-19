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

    /**
     * Confirms an enrollment started with {@link Guardian#requestEnroll(String, EnrollmentType)}.
     * <p>
     * Use this method to confirm an enrollment transaction once the user scanned the QR code with a TOTP app (for a
     * transaction initiated with {@link EnrollmentType#TOTP()}) or when the user received the OTP code delivered to his
     * phone number by SMS (for a transaction initiated with {@link EnrollmentType#SMS(String)}).
     *
     * This overload is intended for stateless applications where {@link java.io.Serializable} is not acceptable,
     * avoiding the necessity of utilising poor practises to preserve {@link Transaction} between user actions.
     *
     * @param transactionToken the token associated with the transaction to confirm.
     * @param otp              the code obtained from the TOTP app or delivered to the phone number by SMS
     * @throws IOException              when there's a connection issue
     * @throws IllegalArgumentException when the transaction is not valid
     * @throws GuardianException        when there's a Guardian specific issue (invalid otp for example)
     */
    public void confirmEnroll(String transactionToken, String otp)
            throws IOException, IllegalArgumentException, GuardianException {
        if (transactionToken == null) {
            throw new IllegalArgumentException("Invalid enrollment transaction");
        }
        if (otp == null) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        apiClient
                .verifyOTP(transactionToken, otp)
                .execute();
    }
}
