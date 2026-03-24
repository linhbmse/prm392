package com.myfirstandroidjava.salesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.myfirstandroidjava.salesapp.models.CheckoutRequest;
import com.myfirstandroidjava.salesapp.models.CheckoutResponse;
import com.myfirstandroidjava.salesapp.models.OrderResponse;
import com.myfirstandroidjava.salesapp.network.PaymentAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderStatus, tvOrderDate, tvPaymentMethod, tvBillingAddress, tvTotalPrice;
    private Button btnPayNow;
    private PaymentAPIService paymentAPIService;
    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        paymentAPIService = RetrofitClient.getClient(this, token).create(PaymentAPIService.class);

        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvBillingAddress = findViewById(R.id.tvBillingAddress);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnPayNow = findViewById(R.id.btnPayNow);

        OrderResponse order = (OrderResponse) getIntent().getSerializableExtra("orderResponse");
        if (order != null) {
            orderId = order.getOrderId();
            displayOrder(order);

            // Logic for Pay Button based on Payment Method
            if ("PAYOS".equalsIgnoreCase(order.getPaymentMethod())) {
                btnPayNow.setText("Pay Now with PayOS");
                btnPayNow.setEnabled(true);
                btnPayNow.setOnClickListener(v -> startPayOSCheckout(orderId));
            } else {
                // For COD, the order is already placed. Let the user go home.
                btnPayNow.setText("Back to Home");
                btnPayNow.setEnabled(true);
                btnPayNow.setClickable(true);
                btnPayNow.setOnClickListener(v -> {
                    Toast.makeText(this, "Returning to Home", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OrderActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        } else {
            Toast.makeText(this, "Order data missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayOrder(OrderResponse order) {
        tvOrderId.setText("Order ID: " + order.getOrderId());
        tvOrderStatus.setText("Status: " + order.getOrderStatus());
        tvOrderDate.setText("Date: " + order.getOrderDate());
        tvPaymentMethod.setText("Payment: " + order.getPaymentMethod());
        tvBillingAddress.setText("Address: " + order.getBillingAddress());
        tvTotalPrice.setText(String.format("Total: $%.2f", order.getTotalAmount()));
    }

    private void startPayOSCheckout(int orderId) {
        btnPayNow.setEnabled(false);
        btnPayNow.setText("Processing...");

        CheckoutRequest request = new CheckoutRequest(orderId);
        request.setReturnUrl("myapp://payos-return");
        paymentAPIService.checkout(request).enqueue(new Callback<CheckoutResponse>() {
            @Override
            public void onResponse(Call<CheckoutResponse> call, Response<CheckoutResponse> response) {
                btnPayNow.setEnabled(true);
                btnPayNow.setText("Pay Now with PayOS");

                if (response.isSuccessful() && response.body() != null) {
                    CheckoutResponse checkout = response.body();
                    if (checkout.isSuccess() && checkout.getCheckoutUrl() != null) {
                        // Open PayOS checkout URL in in-app WebView
                        Intent intent = new Intent(OrderActivity.this, PayOSWebViewActivity.class);
                        intent.putExtra("checkoutUrl", checkout.getCheckoutUrl());
                        startActivity(intent);
                    } else {
                        String msg = checkout.getMessage() != null ? checkout.getMessage() : "Checkout failed";
                        Toast.makeText(OrderActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(OrderActivity.this, "Failed to create checkout", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckoutResponse> call, Throwable t) {
                btnPayNow.setEnabled(true);
                btnPayNow.setText("Pay Now with PayOS");
                Log.e("PayOS", "Error: " + t.getMessage());
                Toast.makeText(OrderActivity.this, "Payment error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
