package com.example.help_hub.OtherClasses;

public class ConfirmedUser {

    private String UserId, Id;
    private boolean opinionSent;

    public ConfirmedUser() {
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public void setOpinionSended(boolean opinionSent) {
        this.opinionSent = opinionSent;
    }

    public boolean isOpinionSended() {
        return opinionSent;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
}
