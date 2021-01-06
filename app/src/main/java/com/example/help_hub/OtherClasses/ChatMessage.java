package com.example.help_hub.OtherClasses;

public class ChatMessage {

    private String message, userId;

    public ChatMessage(String message, String userId) {
        this.message = message;
        this.userId = userId;
    }

    public ChatMessage() {}

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }
}
