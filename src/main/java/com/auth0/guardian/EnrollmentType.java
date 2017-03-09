package com.auth0.guardian;

/**
 * The type of multi-factor enrollment: TOTP or SMS
 */
public abstract class EnrollmentType {

    /**
     * Creates an enrollment type specific for TOTP-based multi-factor authentication
     *
     * @return a TOTP-based EnrollmentType
     */
    public static EnrollmentType TOTP() {
        return new TOTP();
    }

    /**
     * Creates an enrollment type specific for SMS-based multi-factor authentication
     *
     * @param phoneNumber the phone number to use for enrollment
     * @return a SMS-based EnrollmentType
     */
    public static EnrollmentType SMS(String phoneNumber) {
        return new SMS(phoneNumber);
    }

    static class TOTP extends EnrollmentType {

    }

    static class SMS extends EnrollmentType {

        private final String phoneNumber;

        SMS(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        String getPhoneNumber() {
            return phoneNumber;
        }
    }
}
