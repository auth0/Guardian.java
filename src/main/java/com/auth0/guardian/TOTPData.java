package com.auth0.guardian;

public class TOTPData {

    private String totpSecret;

    TOTPData(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public String getSecret() {
        return totpSecret;
    }

    public String getAlgorithm() {
        return "sha1";
    }

    public Integer getDigits() {
        return 6;
    }

    public Integer getPeriod() {
        return 30;
    }
}
