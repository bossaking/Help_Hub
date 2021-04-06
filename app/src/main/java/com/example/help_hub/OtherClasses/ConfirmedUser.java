package com.example.help_hub.OtherClasses;

public class ConfirmedUser {

    private String UserId, Id;
    private boolean opinionSended;

    public ConfirmedUser(){}

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public void setOpinionSended(boolean opinionSended) {
        this.opinionSended = opinionSended;
    }

    public boolean isOpinionSended() {
        return opinionSended;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
}
