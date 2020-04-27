package com.ixxhar.covid19tracker.modelclass;

public class UserModel {
    private String userPhoneNumber, userID, sendDataPermission, notificationToken;

    public String getSendDataPermission() {
        return sendDataPermission;
    }

    public void setSendDataPermission(String sendDataPermission) {
        this.sendDataPermission = sendDataPermission;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = notificationToken;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "userPhoneNumber='" + userPhoneNumber + '\'' +
                ", userID='" + userID + '\'' +
                ", sendDataPermission='" + sendDataPermission + '\'' +
                ", notificationToken='" + notificationToken + '\'' +
                '}';
    }
}
