package com.myfirstandroidjava.salesapp.models;

public class CheckoutRequest {
    private int orderId;

    public CheckoutRequest(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
}
