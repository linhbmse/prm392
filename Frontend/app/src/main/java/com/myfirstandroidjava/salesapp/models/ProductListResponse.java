package com.myfirstandroidjava.salesapp.models;

import java.util.List;

public class ProductListResponse {
    private int total;
    private List<ProductItem> items;

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public List<ProductItem> getItems() { return items; }
    public void setItems(List<ProductItem> items) { this.items = items; }
}
