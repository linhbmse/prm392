package com.myfirstandroidjava.salesapp.models;

public class NotificationBadge {
    private int unreadNotifications;
    private int cartItemCount;

    public int getUnreadNotifications() { return unreadNotifications; }
    public void setUnreadNotifications(int unreadNotifications) { this.unreadNotifications = unreadNotifications; }

    public int getCartItemCount() { return cartItemCount; }
    public void setCartItemCount(int cartItemCount) { this.cartItemCount = cartItemCount; }
}
