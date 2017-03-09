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

import java.util.HashMap;
import java.util.Map;

/**
 * Class that wraps server-side or app errors exclusively related to Guardian
 */
public class GuardianException extends RuntimeException {

    private static final String ERROR_INVALID_OTP = "invalid_otp";
    private static final String ERROR_INVALID_TOKEN = "invalid_token";
    private static final String ERROR_DEVICE_ACCOUNT_NOT_FOUND = "device_account_not_found";
    private static final String ERROR_ENROLLMENT_NOT_FOUND = "enrollment_not_found";
    private static final String ERROR_LOGIN_TRANSACTION_NOT_FOUND = "login_transaction_not_found";

    private static final String ERROR_ALREADY_ENROLLED = "already_enrolled";

    private final Map<String, Object> errorResponse;
    private final String errorCode;

    public GuardianException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.errorCode = null;
        this.errorResponse = null;
    }

    public GuardianException(Map<String, Object> errorResponse) {
        super((String) errorResponse.get("error"));
        this.errorCode = (String) errorResponse.get("errorCode");
        this.errorResponse = errorResponse;
    }

    static GuardianException alreadyEnrolled() {
        Map<String, Object> error = new HashMap<>(2);
        error.put("error", "Account already has an enrollment");
        error.put("errorCode", ERROR_ALREADY_ENROLLED);
        return new GuardianException(error);
    }

    /**
     * Returns the `errorCode` value, if available.
     *
     * @return the error code, or null
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Whether the error is caused by the use of an invalid OTP code
     *
     * @return true if the error is caused by the use of an invalid OTP code
     */
    public boolean isInvalidOTP() {
        return ERROR_INVALID_OTP.equals(errorCode);
    }

    /**
     * Whether the error is caused by the use of an invalid token
     *
     * @return true if the error is caused by the use of an invalid token
     */
    public boolean isInvalidToken() {
        return ERROR_INVALID_TOKEN.equals(errorCode);
    }

    /**
     * Whether the error is caused by the enrollment being invalid or not found on the server
     *
     * @return true if the error is caused by the enrollment being invalid or not found
     */
    public boolean isEnrollmentNotFound() {
        return ERROR_DEVICE_ACCOUNT_NOT_FOUND.equals(errorCode)
                || ERROR_ENROLLMENT_NOT_FOUND.equals(errorCode);
    }

    /**
     * Whether the error is caused by the login transaction being invalid, expired or not found
     *
     * @return true if error is caused by the login transaction being invalid, expired or not found
     */
    public boolean isLoginTransactionNotFound() {
        return ERROR_LOGIN_TRANSACTION_NOT_FOUND.equals(errorCode);
    }

    /**
     * Whether the error is caused by the user already having a confirmed enrollment
     *
     * @return true if error is caused by the user already having a confirmed enrollment
     */
    public boolean isAlreadyEnrolled() {
        return ERROR_ALREADY_ENROLLED.equals(errorCode);
    }

    @Override
    public String toString() {
        if (errorResponse != null) {
            return "GuardianException{" + errorResponse + '}';
        } else {
            return super.toString();
        }
    }
}
