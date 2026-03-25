package com.myfirstandroidjava.salesapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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
import com.myfirstandroidjava.salesapp.models.GenericResponse;
import com.myfirstandroidjava.salesapp.models.OrderResponse;
import com.myfirstandroidjava.salesapp.models.UpdateCartItemRequest;
import com.myfirstandroidjava.salesapp.network.CartAPIService;
import com.myfirstandroidjava.salesapp.network.OrderAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment implements CartAdapter.OnCartActionListener {
    private RecyclerView recyclerView;
    private TextView tvTotal;
    private Button btnCheckout;
    private CartAPIService cartAPIService;
    private OrderAPIService orderAPIService;
    private ArrayList<CartItem> cartItems;
    private double totalCartPrice;
    private CartAdapter adapter;

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
                showCheckoutDialog(null); // Checkout all
            } else {
                Toast.makeText(getContext(), "Giỏ hàng trống.", Toast.LENGTH_SHORT).show();
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
                    
                    if (adapter == null) {
                        adapter = new CartAdapter(cartItems, CartFragment.this);
                        recyclerView.setAdapter(adapter);
                    } else {
                        adapter.updateList(cartItems);
                    }
                    
                    tvTotal.setText(String.format("Tổng cộng: $%.2f", totalCartPrice));
                } else {
                    Toast.makeText(getContext(), "Không thể tải giỏ hàng.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartListResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUpdateQuantity(CartItem item, int newQuantity) {
        UpdateCartItemRequest request = new UpdateCartItemRequest(newQuantity);
        
        cartAPIService.updateCartItem(item.getCartItemId(), request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    fetchCartData();
                } else {
                    Toast.makeText(getContext(), "Không thể cập nhật số lượng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteItem(CartItem item) {
        cartAPIService.removeCartItem(item.getCartItemId()).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    fetchCartData();
                    Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBuyNow(CartItem item) {
        showCheckoutDialog(item);
    }

    private void showCheckoutDialog(@Nullable CartItem singleItem) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle(singleItem == null ? "Thanh toán tất cả" : "Mua ngay: " + singleItem.getProductName());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_checkout, null);
        builder.setView(dialogView);

        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        RadioGroup rgPayment = dialogView.findViewById(R.id.rgPayment);
        TextView tvDialogTotal = dialogView.findViewById(R.id.tvTotalAmount);
        
        double priceToShow;
        if (singleItem == null) {
            priceToShow = totalCartPrice;
        } else {
            // Tính toán giá tiền = giá sản phẩm * số lượng
            priceToShow = singleItem.getPrice() * singleItem.getQuantity();
        }

        if (tvDialogTotal != null) {
            tvDialogTotal.setText(String.format("Số tiền: $%.2f", priceToShow));
        }

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String address = etAddress.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(getContext(), "Địa chỉ là bắt buộc", Toast.LENGTH_SHORT).show();
                return;
            }

            String paymentMethod = "COD";
            int checkedId = rgPayment.getCheckedRadioButtonId();
            if (checkedId == R.id.rbPayOS) {
                paymentMethod = "PAYOS";
            }

            List<Integer> itemIds = null;
            if (singleItem != null) {
                itemIds = Collections.singletonList(singleItem.getCartItemId());
            }

            createOrder(address, paymentMethod, itemIds);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void createOrder(String address, String paymentMethod, @Nullable List<Integer> cartItemIds) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setPaymentMethod(paymentMethod);
        request.setBillingAddress(address);
        request.setCartItemIds(cartItemIds);

        orderAPIService.createOrder(request).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                handleOrderResponse(response);
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleOrderResponse(Response<OrderResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            OrderResponse order = response.body();
            Intent intent = new Intent(getActivity(), OrderActivity.class);
            intent.putExtra("orderResponse", order);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Đặt hàng thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}
