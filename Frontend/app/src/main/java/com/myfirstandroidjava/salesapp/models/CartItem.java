package com.myfirstandroidjava.salesapp.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private int cartItemId;
    private int productId;
    private String productName;
    private String imageUrl;
    private double price;
    private int quantity;
    private double totalPrice;
    private String categoryName;

    public CartItem() {}

    public CartItem(int cartItemId, int productId, String productName, String imageUrl, double price, int quantity, double totalPrice) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public int getCartItemId() { return cartItemId; }
    public void setCartItemId(int cartItemId) { this.cartItemId = cartItemId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getMaxQuantity() {
        if (categoryName == null) return 2; // Default
        String lowerCategory = categoryName.toLowerCase();
        if (lowerCategory.contains("điện thoại") || lowerCategory.contains("phone")) {
            return 1;
        } else if (lowerCategory.contains("tai nghe") || lowerCategory.contains("headphone")) {
            return 3;
        } else {
            return 2;
        }
    }
}
