package com.auth0.guardian;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class TOTPDataTest {

    private static final String TOTP_SECRET = "TOTP_SECRET";

    private TOTPData data;

    @Before
    public void setUp() throws Exception {
        data = new TOTPData(TOTP_SECRET);
    }

    @Test
    public void shouldReturnSecret() throws Exception {
        assertThat(data.getSecret(), is(equalTo(TOTP_SECRET)));
    }

    @Test
    public void shouldReturnAlgorithm() throws Exception {
        assertThat(data.getAlgorithm(), is(equalTo("sha1")));
    }

    @Test
    public void shouldReturnDigits() throws Exception {
        assertThat(data.getDigits(), is(equalTo(6)));
    }

    @Test
    public void shouldReturnPeriod() throws Exception {
        assertThat(data.getPeriod(), is(equalTo(30)));
    }

}