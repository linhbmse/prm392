package com.myfirstandroidjava.salesapp.models;

public class AddToCartRequest {
    private int productId;
    private int quantity;

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public AddToCartRequest(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
