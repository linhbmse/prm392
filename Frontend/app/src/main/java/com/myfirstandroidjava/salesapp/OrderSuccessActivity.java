package com.myfirstandroidjava.salesapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class OrderSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        TextView tvOrderId = findViewById(R.id.tvOrderId);
        MaterialButton btnHome = findViewById(R.id.btnHome);
        MaterialButton btnViewOrders = findViewById(R.id.btnViewOrders);

        // Get order ID from intent (passed from PayOSRedirectActivity)
        String orderId = getIntent().getStringExtra("orderId");
        if (orderId != null) {
            tvOrderId.setText("#" + orderId);
        }

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnViewOrders.setOnClickListener(v -> {
            // Ideally navigate to an Order History screen if you have one
            // For now, let's just go back to Home
            Intent intent = new Intent(OrderSuccessActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
