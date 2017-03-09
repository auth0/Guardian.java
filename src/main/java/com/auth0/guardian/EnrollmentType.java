package com.auth0.guardian;

public abstract class EnrollmentType {

    public static EnrollmentType TOTP() {
        return new TOTP();
    }

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
