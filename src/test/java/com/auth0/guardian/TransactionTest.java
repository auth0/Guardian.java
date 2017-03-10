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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TransactionTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCreateCorrectly() throws Exception {
        Transaction transaction = new Transaction("TRANSACTION_TOKEN", "RECOVERY_CODE", "OTP_SECRET");

        assertThat(transaction.getTransactionToken(), is(equalTo("TRANSACTION_TOKEN")));
        assertThat(transaction.getRecoveryCode(), is(equalTo("RECOVERY_CODE")));
        assertThat(transaction.totpURI("username", "company"), is(equalTo("otpauth://totp/company:username?secret=OTP_SECRET&issuer=company")));
    }

    @Test
    public void shouldCreateCorrectlyWithSpaces() throws Exception {
        Transaction transaction = new Transaction("TRANSACTION_TOKEN", "RECOVERY_CODE", "OTP_SECRET");

        assertThat(transaction.getTransactionToken(), is(equalTo("TRANSACTION_TOKEN")));
        assertThat(transaction.getRecoveryCode(), is(equalTo("RECOVERY_CODE")));
        assertThat(transaction.totpURI("user name", "company name"), is(equalTo("otpauth://totp/company%20name:user%20name?secret=OTP_SECRET&issuer=company%20name")));
    }

    @Test
    public void shouldCreateCorrectlyWithWeirdCharacters() throws Exception {
        Transaction transaction = new Transaction("TRANSACTION_TOKEN", "RECOVERY_CODE", "OTP_SECRET");

        assertThat(transaction.getTransactionToken(), is(equalTo("TRANSACTION_TOKEN")));
        assertThat(transaction.getRecoveryCode(), is(equalTo("RECOVERY_CODE")));
        assertThat(transaction.totpURI("user%name", "compañy?!"), is(equalTo("otpauth://totp/compa%C3%B1y%3F!:user%25name?secret=OTP_SECRET&issuer=compa%C3%B1y?!")));
    }

    @Test
    public void shouldSerializeCorrectly() throws Exception {
        Transaction transaction = new Transaction("TRANSACTION_TOKEN", "RECOVERY_CODE", "OTP_SECRET");

        // save
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(transaction);
        objectOutputStream.close();
        outputStream.close();

        byte[] binaryData = outputStream.toByteArray();

        // restore
        ByteArrayInputStream inputStream = new ByteArrayInputStream(binaryData);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        Transaction restoredTransaction = (Transaction) objectInputStream.readObject();
        objectInputStream.close();
        inputStream.close();

        assertThat(restoredTransaction.getTransactionToken(), is(equalTo("TRANSACTION_TOKEN")));
        assertThat(restoredTransaction.getRecoveryCode(), is(equalTo("RECOVERY_CODE")));
    }

    @Test
    public void shouldThrowWhenRequestingTotpUriAfterSerialization() throws Exception {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("There is no OTP Secret for this transaction");

        Transaction transaction = new Transaction("TRANSACTION_TOKEN", "RECOVERY_CODE", "OTP_SECRET");

        // save
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(transaction);
        objectOutputStream.close();
        outputStream.close();

        byte[] binaryData = outputStream.toByteArray();

        // restore
        ByteArrayInputStream inputStream = new ByteArrayInputStream(binaryData);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        Transaction restoredTransaction = (Transaction) objectInputStream.readObject();
        objectInputStream.close();
        inputStream.close();

        restoredTransaction.totpURI("user name", "company name");
    }
}