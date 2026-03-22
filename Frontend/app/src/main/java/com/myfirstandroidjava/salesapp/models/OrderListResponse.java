package com.myfirstandroidjava.salesapp.models;

import java.util.List;

public class OrderListResponse {
    private int total;
    private List<Order> items;

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public List<Order> getItems() { return items; }
    public void setItems(List<Order> items) { this.items = items; }
}
