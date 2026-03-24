package com.myfirstandroidjava.salesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Handles redirect callbacks from PayOS checkout.
 * When the user completes or cancels payment in the browser, 
 * the PayOS checkout URL redirects back to the app via deep link.
 */
public class PayOSRedirectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            // Check status for PayOS: usually passed as query parameter 'status'
            // or by path if configured that way in the returnUrl.
            String status = uri.getQueryParameter("status");
            String path = uri.getPath();
            
            if ("PAID".equals(status) || (path != null && path.contains("success"))) {
                Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show();
                Intent successIntent = new Intent(this, OrderSuccessActivity.class);
                String orderId = uri.getQueryParameter("orderId");
                if (orderId == null) {
                    orderId = uri.getQueryParameter("orderCode"); // PayOS often uses orderCode
                }
                if (orderId != null) {
                    successIntent.putExtra("orderId", orderId);
                }
                startActivity(successIntent);
            } else if ("CANCELLED".equals(status) || (path != null && path.contains("cancel"))) {
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Payment status: " + (status != null ? status : "unknown"), Toast.LENGTH_SHORT).show();
            }
            finish();
        } else {
            finish();
        }
    }
}
