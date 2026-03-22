package com.myfirstandroidjava.salesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.myfirstandroidjava.salesapp.models.AddToCartRequest;
import com.myfirstandroidjava.salesapp.models.CartListResponse;
import com.myfirstandroidjava.salesapp.models.ProductDetailResponse;
import com.myfirstandroidjava.salesapp.network.CartAPIService;
import com.myfirstandroidjava.salesapp.network.ProductAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.utils.Constants;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private TextView textName, textDescription, textPrice, textSpecs;
    private ImageView imageProduct;
    private ProgressBar progressBar;
    private Button btnAddToCart;
    private ProductAPIService productAPIService;
    private CartAPIService cartAPIService;
    private TokenManager tokenManager;
    private int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        textName = findViewById(R.id.textName);
        textDescription = findViewById(R.id.textDescription);
        textPrice = findViewById(R.id.textPrice);
        textSpecs = findViewById(R.id.textSpecs);
        imageProduct = findViewById(R.id.imageProduct);
        progressBar = findViewById(R.id.progressBar);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        productAPIService = RetrofitClient.getClient(this, token).create(ProductAPIService.class);
        cartAPIService = RetrofitClient.getClient(this, token).create(CartAPIService.class);

        productId = getIntent().getIntExtra("productId", -1);
        if (productId != -1) {
            loadProductDetail(productId);
        } else {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (token == null || token.isEmpty()) {
            btnAddToCart.setText("Login / Register");
            btnAddToCart.setOnClickListener(v -> {
                Intent intent = new Intent(ProductDetailActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        } else {
            btnAddToCart.setText("Add to Cart");
            btnAddToCart.setOnClickListener(v -> addToCart(productId));
        }
    }

    private void loadProductDetail(int productId) {
        progressBar.setVisibility(View.VISIBLE);

        productAPIService.getProductDetail(productId).enqueue(new Callback<ProductDetailResponse>() {
            @Override
            public void onResponse(Call<ProductDetailResponse> call, Response<ProductDetailResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ProductDetailResponse product = response.body();
                    textName.setText(product.getProductName());
                    textDescription.setText(product.getFullDescription());
                    textPrice.setText("$" + product.getPrice());
                    textSpecs.setText(product.getTechnicalSpecifications());

                    if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                        String imageUrl = product.getImageUrl();
                        // If the URL is relative, prepend base URL
                        if (!imageUrl.startsWith("http")) {
                            imageUrl = Constants.BASE_URL + imageUrl;
                        }
                        Glide.with(ProductDetailActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.image_error)
                                .into(imageProduct);
                    }
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Failed to load product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDetailResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("DETAIL_ERROR", "Error: " + t.getMessage());
                Toast.makeText(ProductDetailActivity.this, "Error loading detail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToCart(int productId) {
        progressBar.setVisibility(View.VISIBLE);
        btnAddToCart.setEnabled(false);

        AddToCartRequest request = new AddToCartRequest(productId, 1);
        cartAPIService.addToCart(request).enqueue(new Callback<CartListResponse>() {
            @Override
            public void onResponse(Call<CartListResponse> call, Response<CartListResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnAddToCart.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProductDetailActivity.this, "Added to cart successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                    Log.e("CART_ERROR", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CartListResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnAddToCart.setEnabled(true);
                Log.e("CART_ERROR", "Error: " + t.getMessage());
                Toast.makeText(ProductDetailActivity.this, "Error adding to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
