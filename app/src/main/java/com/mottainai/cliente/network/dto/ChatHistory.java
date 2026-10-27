package com.mottainai.cliente.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public final class ChatHistory {
    @SerializedName("session_id") public String sessionId;
    public String status;
    public List<Message> messages;

    public static final class Message {
        public String role;
        public String content;
    }

    public boolean isValidFor(String expectedSession) {
        return expectedSession != null && expectedSession.equals(sessionId) && messages != null;
    }
}
