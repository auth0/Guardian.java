package com.auth0.guardian;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class EnrollmentTest {

    @Test
    public void shouldCreateCorrectly() throws Exception {
        Enrollment enrollment = new Enrollment("RECOVERY_CODE");

        assertThat(enrollment.getRecoveryCode(), is(equalTo("RECOVERY_CODE")));
    }
}