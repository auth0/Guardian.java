package com.auth0.guardian;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class DeviceAccount {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("otp_secret")
    private String otpSecret;

    @JsonProperty("recovery_code")
    private String recoveryCode;

    String getId() {
        return id;
    }

    String getStatus() {
        return status;
    }

    String getOtpSecret() {
        return otpSecret;
    }

    String getRecoveryCode() {
        return recoveryCode;
    }
}
