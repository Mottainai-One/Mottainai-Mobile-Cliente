package com.mottainai.cliente.repository;

import com.google.gson.JsonSyntaxException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLHandshakeException;
import org.junit.Test;
import static org.junit.Assert.*;

/** Reúne os cenários de teste de ProfileSyncError. */
public class ProfileSyncErrorTest {
    /** Verifica a mensagem produzida quando o serviço local está indisponível no emulador. */
    @Test public void unavailableLocalApiExplainsEmulatorAddress() {
        String message = ProfileSyncError.message(new ConnectException("SECRET"), true);
        assertTrue(message.contains("conectar à API"));
        assertTrue(message.contains("só funciona no emulador"));
        assertFalse(message.contains("SECRET"));
    }
    /** Verifica se o tempo esgotado não é apresentado como senha incorreta. */
    @Test public void timeoutDoesNotReportWrongPassword() {
        String message = ProfileSyncError.message(new SocketTimeoutException(), false);
        assertTrue(message.contains("demorou"));
        assertFalse(message.contains("senha"));
    }
    /** Verifica se uma resposta inválida não é tratada como ausência de internet. */
    @Test public void malformedResponseIsNotReportedAsInternetFailure() {
        String message = ProfileSyncError.message(new JsonSyntaxException("SECRET"), false);
        assertTrue(message.contains("incompatíveis"));
        assertFalse(message.contains("SECRET"));
    }
    /** Verifica a distinção entre falhas de resolução de endereço e de conexão segura. */
    @Test public void dnsAndTlsFailuresAreDistinct() {
        assertTrue(ProfileSyncError.message(new UnknownHostException(), false).contains("não foi encontrado"));
        assertTrue(ProfileSyncError.message(new SSLHandshakeException("SECRET"), false).contains("HTTPS segura"));
    }
    /** Verifica a classificação de falhas de rede encapsuladas em outras exceções. */
    @Test public void wrappedNetworkErrorsKeepTheirClassification() {
        String message = ProfileSyncError.message(new java.io.IOException(new ConnectException()), false);
        assertTrue(message.contains("conectar à API"));
    }
    /** Verifica se o JSON inválido é distinguido das falhas de internet. */
    @Test public void malformedJsonIsNotAnInternetError() {
        String message = ProfileSyncError.message(new com.google.gson.stream.MalformedJsonException("SECRET"), true);
        assertTrue(message.contains("incompatíveis"));
        assertFalse(message.contains("SECRET"));
        assertFalse(message.contains("emulador"));
    }
}
