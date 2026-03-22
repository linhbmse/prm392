package com.myfirstandroidjava.salesapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class OrderManager {
    private static final String PREF_NAME = "sales_app_prefs";
    private static final String KEY_ORDER_ID = "current_order_id";
    private final SharedPreferences prefs;

    public OrderManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveOrderId(String orderId) {
        prefs.edit().putString(KEY_ORDER_ID, orderId).apply();
    }

    public String getOrderId() {
        return prefs.getString(KEY_ORDER_ID, null);
    }

    public void clearOrderId() {
        prefs.edit().remove(KEY_ORDER_ID).apply();
    }
}
