package com.myfirstandroidjava.salesapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrderSuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        TextView tvMessage = findViewById(R.id.tvMessage);
        Button btnHome = findViewById(R.id.btnHome);

        String orderId = getIntent().getStringExtra("orderId");
        tvMessage.setText("Your order #" + orderId + " was placed successfully!");

        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }
}