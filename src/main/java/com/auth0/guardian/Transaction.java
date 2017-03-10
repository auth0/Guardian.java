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

import okhttp3.HttpUrl;

import java.io.Serializable;

/**
 * An enrollment Transaction
 * <p>
 * A transaction is created when requesting to enroll. This transaction will be required to confirm the enrollment once
 * the user added his TOTP account or received the SMS with the code.
 * <p>
 * Implements {@code java.io.Serializable} to make it easy to save on the session.
 * The transaction contains sensitive information like the transaction token and the recovery code. Keep in mind this
 * when considering possible storage options.
 */
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

    /**
     * Returns the TOTP enrollment URI to be displayed in the QR code
     *
     * @param user   the user name or email of the account
     * @param issuer the issuer of the account, usually the company or service name
     * @return the TOTP URI
     * @throws IllegalStateException when there is no OTP secret
     */
    public String totpURI(String user, String issuer) throws IllegalStateException {
        if (otpSecret == null) {
            throw new IllegalStateException("There is no OTP Secret for this transaction");
        }

        // use HttpUrl to build the URI, java.net.URL really sucks
        return new HttpUrl.Builder()
                .scheme("https") // HttpUrl only allows http/https so I replace it afterwards
                .host("totp")
                .addPathSegment(String.format("%s:%s", issuer, user))
                .addQueryParameter("secret", otpSecret)
                .addQueryParameter("issuer", issuer)
                .build()
                .toString()
                .replaceFirst("https", "otpauth"); // replace scheme
    }
}
