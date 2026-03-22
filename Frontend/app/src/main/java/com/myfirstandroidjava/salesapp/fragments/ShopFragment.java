package com.myfirstandroidjava.salesapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirstandroidjava.salesapp.LoginActivity;
import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.adapters.ProductAdapter;
import com.myfirstandroidjava.salesapp.models.ProductItem;
import com.myfirstandroidjava.salesapp.models.ProductListResponse;
import com.myfirstandroidjava.salesapp.network.ProductAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ProductAdapter adapter;
    private Button btnLoginRegister;
    private ProductAPIService productAPIService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        TokenManager tokenManager = new TokenManager(getContext());
        String token = tokenManager.getToken();
        productAPIService = RetrofitClient.getClient(requireContext(), token).create(ProductAPIService.class);

        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        progressBar = view.findViewById(R.id.progressBar);
        btnLoginRegister = view.findViewById(R.id.btnLoginRegister);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        if (token == null) {
            btnLoginRegister.setText("Login / Register");
            btnLoginRegister.setVisibility(View.VISIBLE);
            btnLoginRegister.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            });
        } else {
            btnLoginRegister.setText("Logout");
            btnLoginRegister.setVisibility(View.VISIBLE);
            btnLoginRegister.setOnClickListener(v -> {
                tokenManager.clearToken();
                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                requireActivity().recreate();
            });
        }

        loadProducts();

        return view;
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);

        // Use skip/take pagination matching backend API
        Call<ProductListResponse> call = productAPIService.getProducts(null, null, null, null, null, 0, 20);
        call.enqueue(new Callback<ProductListResponse>() {
            @Override
            public void onResponse(Call<ProductListResponse> call, Response<ProductListResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductItem> products = response.body().getItems();
                    adapter = new ProductAdapter(products);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductListResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("SHOP_FRAGMENT", "Error loading data: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
