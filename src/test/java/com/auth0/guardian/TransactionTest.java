package com.auth0.guardian;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class TransactionTest {

    @Test
    public void shouldCreateCorrectly() throws Exception {
        Transaction transaction = new Transaction("TRANSACTION_TOKEN", "RECOVERY_CODE", "OTP_SECRET");

        assertThat(transaction.getTransactionToken(), is(equalTo("TRANSACTION_TOKEN")));
        assertThat(transaction.getRecoveryCode(), is(equalTo("RECOVERY_CODE")));
        assertThat(transaction.totpURI("username", "company"), is(equalTo("otpauth://totp/company:username?secret=OTP_SECRET&issuer=company")));
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
        assertThat(restoredTransaction.totpURI("username", "company"), is(nullValue()));
    }
}