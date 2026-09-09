package com.mottainai.cliente.models;

public class ChatMessage {

    public enum Sender {
        BOT, USER
    }

    private final Sender sender;
    private final String text;
    private final Offer attachedOffer;

    public ChatMessage(Sender sender, String text) {
        this(sender, text, null);
    }

    public ChatMessage(Sender sender, String text, Offer attachedOffer) {
        this.sender = sender;
        this.text = text;
        this.attachedOffer = attachedOffer;
    }

    public Sender getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public Offer getAttachedOffer() {
        return attachedOffer;
    }

    public boolean hasAttachedOffer() {
        return attachedOffer != null;
    }
}
