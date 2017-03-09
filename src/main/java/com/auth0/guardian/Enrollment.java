package com.auth0.guardian;

/**
 * Enrollment data obtained after confirmation
 */
public class Enrollment {

    private final String recoveryCode;

    Enrollment(String recoveryCode) {
        this.recoveryCode = recoveryCode;
    }

    /**
     * Returns the recovery code that can be used by the user to authenticate when the main method (TOTP/SMS) is not
     * available. For example when the user lost his phone.
     *
     * @return the recovery code
     */
    public String getRecoveryCode() {
        return recoveryCode;
    }
}
