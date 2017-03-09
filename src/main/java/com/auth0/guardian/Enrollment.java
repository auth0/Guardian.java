package com.auth0.guardian;

public class Enrollment {

    private final String recoveryCode;

    Enrollment(String recoveryCode) {
        this.recoveryCode = recoveryCode;
    }

    public String getRecoveryCode() {
        return recoveryCode;
    }
}
