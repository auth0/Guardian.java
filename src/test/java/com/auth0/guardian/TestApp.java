package com.auth0.guardian;

import java.io.IOException;

public class TestApp {

    public static void main(String[] args) {

        Guardian guardian = new Guardian("https://nikolaseu-test.guardian.eu.auth0.com");

        // obtain an enrollment ticket for the user
        String enrollmentTicket = "Ag1qX7vZVBvyTKhFwrkzaCH2M8vn5b6c";

        Transaction enrollmentTransaction;
        try {
            enrollmentTransaction = guardian
                    //.requestEnroll(enrollmentTicket, EnrollmentType.SMS("+5493424217158"));
                    .requestEnroll(enrollmentTicket, EnrollmentType.TOTP());

            // Only for TOTP: use the TOTP URI to create a QR and scan with an app
            String totpURI = enrollmentTransaction.totpURI("Username", "Issuer");
            System.out.println(totpURI);

        } catch (IOException e) {
            // connection issue, might be internet (or invalid certificates for example)
            return;
        } catch (GuardianException e) {
            if (e.isAlreadyEnrolled()) {
                // the user was already enrolled
            } else if (e.isInvalidToken()) {
                // the ticket is not valid anymore, or was already used
            } else {
                // some other guardian error, check the message
            }
            return;
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
            return;
        } catch (GuardianException e) {
            if (e.isInvalidToken()) {
                // the transaction is not valid anymore
            } else if (e.isInvalidOTP()) {
                // the OTP is not valid
            } else {
                // some other guardian error, check the message
            }
            return;
        }
    }
}
