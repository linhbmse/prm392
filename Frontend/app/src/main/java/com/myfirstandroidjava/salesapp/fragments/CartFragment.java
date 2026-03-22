package com.myfirstandroidjava.salesapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirstandroidjava.salesapp.OrderActivity;
import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.adapters.CartAdapter;
import com.myfirstandroidjava.salesapp.models.CartItem;
import com.myfirstandroidjava.salesapp.models.CartListResponse;
import com.myfirstandroidjava.salesapp.models.CreateOrderRequest;
import com.myfirstandroidjava.salesapp.models.OrderResponse;
import com.myfirstandroidjava.salesapp.network.CartAPIService;
import com.myfirstandroidjava.salesapp.network.OrderAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView tvTotal;
    private Button btnCheckout;
    private CartAPIService cartAPIService;
    private OrderAPIService orderAPIService;
    private ArrayList<CartItem> cartItems;
    private double totalCartPrice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.cartRecyclerView);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        TokenManager tokenManager = new TokenManager(getContext());
        String token = tokenManager.getToken();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAPIService = RetrofitClient.getClient(requireContext(), token).create(CartAPIService.class);
        orderAPIService = RetrofitClient.getClient(requireContext(), token).create(OrderAPIService.class);

        fetchCartData();

        btnCheckout.setOnClickListener(v -> {
            if (cartItems != null && !cartItems.isEmpty()) {
                showAddressInputDialog();
            } else {
                Toast.makeText(getContext(), "Your cart is empty.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void fetchCartData() {
        Call<CartListResponse> call = cartAPIService.getCart();

        call.enqueue(new Callback<CartListResponse>() {
            @Override
            public void onResponse(Call<CartListResponse> call, Response<CartListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CartListResponse cart = response.body();
                    cartItems = new ArrayList<>(cart.getItems());
                    totalCartPrice = cart.getTotalPrice();
                    recyclerView.setAdapter(new CartAdapter(cartItems));
                    tvTotal.setText(String.format("Total: $%.2f", totalCartPrice));
                } else {
                    Toast.makeText(getContext(), "Failed to load cart.", Toast.LENGTH_SHORT).show();
                    Log.e("CART_LIST_ERROR", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CartListResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddressInputDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Enter Delivery Address");

        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Enter your address");
        input.setPadding(32, 16, 32, 16);
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String address = input.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(getContext(), "Address is required", Toast.LENGTH_SHORT).show();
                return;
            }
            createOrder(address);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void createOrder(String address) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setPaymentMethod("COD");
        request.setBillingAddress(address);

        Call<OrderResponse> call = orderAPIService.createOrder(request);

        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse order = response.body();
                    // Navigate to OrderActivity with order info
                    Intent intent = new Intent(getActivity(), OrderActivity.class);
                    intent.putExtra("orderResponse", order);
                    startActivity(intent);
                } else {
                    String errorMsg = "Failed to create order";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e("ORDER_CREATE_ERROR", "Code: " + response.code() + " Error: " + errorMsg);
                    Toast.makeText(getContext(), "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
