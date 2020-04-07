package com.ixxhar.covid19tracker.modelclass;

public class UserModel {
    private String userPhoneNumber, userID;

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

    @Override
    public String toString() {
        return "UserModel{" +
                "userPhoneNumber='" + userPhoneNumber + '\'' +
                ", userID='" + userID + '\'' +
                '}';
    }
}
