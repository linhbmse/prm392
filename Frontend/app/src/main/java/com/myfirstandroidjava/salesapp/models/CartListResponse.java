package com.myfirstandroidjava.salesapp.models;

import java.util.List;

public class CartListResponse {
    private int cartId;
    private double totalPrice;
    private String status;
    private List<CartItem> items;

    public CartListResponse() {}

    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}
