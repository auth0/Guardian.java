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

import java.io.IOException;

public class TestApp {

    public static void main(String[] args) {

        Guardian guardian = new Guardian("https://<tenant>.guardian.auth0.com");

        // obtain an enrollment ticket for the user
        String enrollmentTicket = "Ag1qX7vZVBvyTKhFwrkzaCH2M8vn5b6c";

        Transaction enrollmentTransaction = null;
        try {
            enrollmentTransaction = guardian
                    //.requestEnroll(enrollmentTicket, EnrollmentType.SMS("+5493424217158"));
                    .requestEnroll(enrollmentTicket, EnrollmentType.TOTP());

            // Only for TOTP: use the TOTP URI to create a QR and scan with an app
            String totpURI = enrollmentTransaction.totpURI("Username", "Issuer");
            System.out.println(totpURI);

        } catch (IOException e) {
            // connection issue, might be internet (or invalid certificates for example)
        } catch (GuardianException e) {
            if (e.isAlreadyEnrolled()) {
                // the user was already enrolled
            } else if (e.isInvalidToken()) {
                // the ticket is not valid anymore, or was already used
            } else {
                // some other guardian error, check the message
            }
        }

        // get the OTP from SMS or TOTP app
        String code = "123456";

        try {
            Enrollment enrollment = guardian.confirmEnroll(enrollmentTransaction, code);

            // Get the recovery code and show to the user
            String recoveryCode = enrollment.getRecoveryCode();
            System.out.println(recoveryCode);

        } catch (IOException e) {
            // connection issue, might be internet (or invalid certificates for example)
        } catch (GuardianException e) {
            if (e.isInvalidToken()) {
                // the transaction is not valid anymore
            } else if (e.isInvalidOTP()) {
                // the OTP is not valid
            } else {
                // some other guardian error, check the message
            }
        }
    }
}
