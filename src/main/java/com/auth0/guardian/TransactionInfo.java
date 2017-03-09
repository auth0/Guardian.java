package com.auth0.guardian;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class TransactionInfo {

    @JsonProperty("transaction_token")
    private String transactionToken;

    @JsonProperty("device_account")
    private DeviceAccount deviceAccount;

    String getTransactionToken() {
        return transactionToken;
    }

    DeviceAccount getDeviceAccount() {
        return deviceAccount;
    }
}
