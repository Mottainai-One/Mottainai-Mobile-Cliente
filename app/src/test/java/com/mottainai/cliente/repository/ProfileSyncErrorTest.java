package com.mottainai.cliente.repository;

import com.google.gson.JsonSyntaxException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLHandshakeException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProfileSyncErrorTest {
    @Test public void unavailableLocalApiExplainsEmulatorAddress() {
        String message = ProfileSyncError.message(new ConnectException("SECRET"), true);
        assertTrue(message.contains("conectar à API"));
        assertTrue(message.contains("só funciona no emulador"));
        assertFalse(message.contains("SECRET"));
    }
    @Test public void timeoutDoesNotReportWrongPassword() {
        String message = ProfileSyncError.message(new SocketTimeoutException(), false);
        assertTrue(message.contains("demorou"));
        assertFalse(message.contains("senha"));
    }
    @Test public void malformedResponseIsNotReportedAsInternetFailure() {
        String message = ProfileSyncError.message(new JsonSyntaxException("SECRET"), false);
        assertTrue(message.contains("incompatíveis"));
        assertFalse(message.contains("SECRET"));
    }
    @Test public void dnsAndTlsFailuresAreDistinct() {
        assertTrue(ProfileSyncError.message(new UnknownHostException(), false).contains("não foi encontrado"));
        assertTrue(ProfileSyncError.message(new SSLHandshakeException("SECRET"), false).contains("HTTPS segura"));
    }
    @Test public void wrappedNetworkErrorsKeepTheirClassification() {
        String message = ProfileSyncError.message(new java.io.IOException(new ConnectException()), false);
        assertTrue(message.contains("conectar à API"));
    }
    @Test public void malformedJsonIsNotAnInternetError() {
        String message = ProfileSyncError.message(new com.google.gson.stream.MalformedJsonException("SECRET"), true);
        assertTrue(message.contains("incompatíveis"));
        assertFalse(message.contains("SECRET"));
        assertFalse(message.contains("emulador"));
    }
}
