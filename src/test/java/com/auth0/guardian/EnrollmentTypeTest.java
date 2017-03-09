package com.auth0.guardian;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class EnrollmentTypeTest {

    @Test
    public void shouldReturnTOTP() throws Exception {
        EnrollmentType enrollmentType = EnrollmentType.TOTP();
        assertThat(enrollmentType, is(instanceOf(EnrollmentType.TOTP.class)));
    }

    @Test
    public void shouldReturnSMS() throws Exception {
        EnrollmentType enrollmentType = EnrollmentType.SMS("+549342123456");
        assertThat(enrollmentType, is(instanceOf(EnrollmentType.SMS.class)));
        assertThat(((EnrollmentType.SMS) enrollmentType).getPhoneNumber(), is(equalTo("+549342123456")));
    }
}