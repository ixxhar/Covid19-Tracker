package com.ixxhar.covid19tracker.modelclass;

public class DeviceModel {
    private String deviceID, loggedTime;

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getLoggedTime() {
        return loggedTime;
    }

    public void setLoggedTime(String loggedTime) {
        this.loggedTime = loggedTime;
    }

    @Override
    public String toString() {
        return "DeviceModel{" +
                "deviceID='" + deviceID + '\'' +
                ", loggedDate='" + loggedTime + '\'' +
                '}';
    }
}
