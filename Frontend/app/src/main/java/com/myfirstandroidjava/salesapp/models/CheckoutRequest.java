package com.myfirstandroidjava.salesapp.models;

public class CheckoutRequest {
    private int orderId;
    private String returnUrl;

    public CheckoutRequest(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
}
