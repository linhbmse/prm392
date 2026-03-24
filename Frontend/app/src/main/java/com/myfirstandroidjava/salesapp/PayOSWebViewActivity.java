package com.myfirstandroidjava.salesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PayOSWebViewActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payos_webview);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        String checkoutUrl = getIntent().getStringExtra("checkoutUrl");
        if (checkoutUrl == null || checkoutUrl.isEmpty()) {
            Toast.makeText(this, "Checkout URL is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        // Important for some payment gateways
        webSettings.setSupportMultipleWindows(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }

            private boolean handleUrl(String url) {
                // Manually intercept any myapp:// link and send it directly!
                if (url.startsWith("myapp://")) {
                    Intent intent = new Intent(PayOSWebViewActivity.this, PayOSRedirectActivity.class);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    finish(); // Close WebView
                    return true;
                }
                
                // Allow fallback for other generic intents if needed
                if (url.startsWith("intent://")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            startActivity(intent);
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        webView.loadUrl(checkoutUrl);
    }
}
