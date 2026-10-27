package com.mottainai.cliente.network.dto;

import com.google.gson.annotations.SerializedName;

public final class ChatRequest {
    public final String message;
    @SerializedName("session_id") public final String sessionId;

    public ChatRequest(String message, String sessionId) {
        if (message == null || message.trim().isEmpty() || message.trim().length() > 4096) {
            throw new IllegalArgumentException("Escreva uma mensagem com até 4096 caracteres.");
        }
        if (!isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Conversa inválida. Inicie uma nova conversa.");
        }
        this.message = message.trim();
        this.sessionId = sessionId;
    }

    public static boolean isValidSessionId(String sessionId) {
        return sessionId != null && sessionId.length() <= 128
                && sessionId.matches("[A-Za-z0-9][A-Za-z0-9._:-]*");
    }
}
