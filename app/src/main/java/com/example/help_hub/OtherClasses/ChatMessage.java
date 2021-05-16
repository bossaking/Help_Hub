package com.example.help_hub.OtherClasses;

public class ChatMessage {

    private String message, userId, time, type;

    public ChatMessage(String message, String userId, String time) {
        this.message = message;
        this.userId = userId;
        this.time = time;
    }

    public ChatMessage() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
