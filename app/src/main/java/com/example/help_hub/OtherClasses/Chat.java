package com.example.help_hub.OtherClasses;

public class Chat {

    private String chatId, otherUserId, offerId, offerTitle, otherUserName, chatType;

    public Chat() {
    }

    public Chat(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getOfferTitle() {
        return offerTitle;
    }

    public void setOfferTitle(String offerTitle) {
        this.offerTitle = offerTitle;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getChatType() {
        return chatType;
    }
}
