package com.myfirstandroidjava.salesapp.models;

public class RegisterDeviceTokenRequest {
    private String deviceToken;

    public RegisterDeviceTokenRequest(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
