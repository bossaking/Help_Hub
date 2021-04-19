package com.example.help_hub.OtherClasses;

public class Report {

    private String ReportID, Cause, UserID, Type;

    public Report(){

    }

    public String getReportID() {
        return ReportID;
    }

    public void setReportID(String ReportID) {
        this.ReportID = ReportID;
    }

    public String getCause() {
        return Cause;
    }

    public void setCause(String cause) {
        Cause = cause;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}
