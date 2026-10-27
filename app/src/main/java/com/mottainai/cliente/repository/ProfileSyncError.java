package com.mottainai.cliente.repository;

import com.google.gson.JsonParseException;
import com.google.gson.stream.MalformedJsonException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import javax.net.ssl.SSLException;

/** No exception text, token, server response or personal data is exposed to the user. */
public final class ProfileSyncError {
    private ProfileSyncError() {}

    public static String message(Throwable failure, boolean emulatorAddress) {
        Throwable cause = failure;
        for (int depth = 0; cause != null && depth < 8; depth++, cause = cause.getCause()) {
            if (cause instanceof JsonParseException || cause instanceof MalformedJsonException
                    || cause instanceof InterruptedIOException || cause instanceof UnknownHostException
                    || cause instanceof ConnectException || cause instanceof SSLException
                    || cause instanceof UnknownServiceException) {
                failure = cause;
                break;
            }
        }
        String message;
        boolean invalidResponse = failure instanceof JsonParseException || failure instanceof MalformedJsonException;
        if (invalidResponse) {
            message = "A API retornou dados incompatíveis com o aplicativo. Confira a versão do serviço.";
        } else if (failure instanceof InterruptedIOException) {
            message = "A API demorou para responder. Tente sincronizar novamente.";
        } else if (failure instanceof UnknownHostException) {
            message = "O endereço da API não foi encontrado. Confira a conexão e a configuração do servidor.";
        } else if (failure instanceof ConnectException) {
            message = "Não foi possível conectar à API. Confira se o servidor está disponível.";
        } else if (failure instanceof SSLException) {
            message = "Não foi possível estabelecer uma conexão HTTPS segura com a API.";
        } else if (failure instanceof UnknownServiceException) {
            message = "A conexão configurada não é permitida. Confira o endereço HTTPS da API.";
        } else {
            message = "Não foi possível confirmar a sincronização com a API. Tente novamente.";
        }
        if (emulatorAddress && !invalidResponse) {
            message += " O endereço 10.0.2.2 exige a API no computador e só funciona no emulador Android.";
        }
        return message + " Não é necessário criar outra conta Firebase.";
    }
}
