package com.myfirstandroidjava.salesapp.models;

import java.util.List;

public class CreateOrderRequest {
    private String paymentMethod;
    private String billingAddress;
    private List<Integer> cartItemIds;

    public CreateOrderRequest(String paymentMethod, String billingAddress) {
        this.paymentMethod = paymentMethod;
        this.billingAddress = billingAddress;
    }

    public CreateOrderRequest() {
    }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

    public List<Integer> getCartItemIds() { return cartItemIds; }
    public void setCartItemIds(List<Integer> cartItemIds) { this.cartItemIds = cartItemIds; }
}
