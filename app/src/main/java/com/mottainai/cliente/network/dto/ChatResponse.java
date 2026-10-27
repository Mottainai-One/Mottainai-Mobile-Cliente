package com.mottainai.cliente.network.dto;

import com.google.gson.annotations.SerializedName;

public final class ChatResponse {
    @SerializedName("session_id") public String sessionId;
    public String response;

    public boolean isValidFor(String expectedSession) {
        return expectedSession != null && expectedSession.equals(sessionId)
                && response != null && !response.trim().isEmpty();
    }
}
