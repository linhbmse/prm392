package com.myfirstandroidjava.salesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.myfirstandroidjava.salesapp.models.CheckoutRequest;
import com.myfirstandroidjava.salesapp.models.CheckoutResponse;
import com.myfirstandroidjava.salesapp.network.PaymentAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private PaymentAPIService paymentAPIService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        paymentAPIService = RetrofitClient.getClient(this, token).create(PaymentAPIService.class);

        int orderId = getIntent().getIntExtra("orderId", -1);

        Button payBtn = findViewById(R.id.btnPay);
        if (orderId != -1) {
            payBtn.setOnClickListener(v -> startPayOSCheckout(orderId));
        } else {
            payBtn.setEnabled(false);
            payBtn.setText("No order to pay");
        }
    }

    private void startPayOSCheckout(int orderId) {
        CheckoutRequest request = new CheckoutRequest(orderId);
        paymentAPIService.checkout(request).enqueue(new Callback<CheckoutResponse>() {
            @Override
            public void onResponse(Call<CheckoutResponse> call, Response<CheckoutResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CheckoutResponse checkout = response.body();
                    if (checkout.isSuccess() && checkout.getCheckoutUrl() != null) {
                        // Mở trình duyệt để thanh toán
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkout.getCheckoutUrl()));
                        startActivity(browserIntent);
                    } else {
                        String msg = checkout.getMessage() != null ? checkout.getMessage() : "Checkout failed";
                        Toast.makeText(PaymentActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PaymentActivity.this, "Failed to create checkout", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckoutResponse> call, Throwable t) {
                Log.e("PayOS", "Error: " + t.getMessage());
                Toast.makeText(PaymentActivity.this, "Payment error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
