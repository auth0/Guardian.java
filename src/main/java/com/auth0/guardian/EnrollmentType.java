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
