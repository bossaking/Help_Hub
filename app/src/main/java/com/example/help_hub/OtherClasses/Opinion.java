package com.example.help_hub.OtherClasses;

public class Opinion {

    private String OpinionText, UserNickname;
    private float Rating;

    public Opinion(){

    }

    public void setOpinionText(String opinionText) {
        OpinionText = opinionText;
    }

    public void setRating(float rating) {
        Rating = rating;
    }

    public void setUserNickname(String userNickname) {
        UserNickname = userNickname;
    }

    public float getRating() {
        return Rating;
    }

    public String getOpinionText() {
        return OpinionText;
    }

    public String getUserNickname() {
        return UserNickname;
    }
}
