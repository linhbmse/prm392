package com.myfirstandroidjava.salesapp.models;

public class CreateOrderRequest {
    private String paymentMethod;
    private String billingAddress;

    public CreateOrderRequest(String paymentMethod, String billingAddress) {
        this.paymentMethod = paymentMethod;
        this.billingAddress = billingAddress;
    }

    public CreateOrderRequest() {

    }

    // Getters and setters
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
}
