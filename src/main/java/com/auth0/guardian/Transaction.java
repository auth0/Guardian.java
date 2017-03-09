package com.auth0.guardian;

import java.io.Serializable;

public class Transaction implements Serializable {

    private String transactionToken;
    private String recoveryCode;
    private transient String otpSecret;

    Transaction(String transactionToken, String recoveryCode, String otpSecret) {
        this.transactionToken = transactionToken;
        this.recoveryCode = recoveryCode;
        this.otpSecret = otpSecret;
    }

    String getTransactionToken() {
        return transactionToken;
    }

    String getRecoveryCode() {
        return recoveryCode;
    }

    public String totpURI(String user, String issuer) {
        if (otpSecret == null) {
            return null;
        }

        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, user, otpSecret, issuer);
    }
}
