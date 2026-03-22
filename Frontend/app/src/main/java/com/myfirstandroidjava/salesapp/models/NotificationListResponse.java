package com.myfirstandroidjava.salesapp.models;

import java.util.List;

public class NotificationListResponse {
    private int total;
    private int unreadCount;
    private List<NotificationItem> items;

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public List<NotificationItem> getItems() { return items; }
    public void setItems(List<NotificationItem> items) { this.items = items; }
}
