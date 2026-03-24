package com.myfirstandroidjava.salesapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    private static final long SEARCH_DEBOUNCE_MS = 350;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ProductAdapter adapter;
    private Button btnLoginRegister;
    private EditText etSearch;
    private ProductAPIService productAPIService;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Call<ProductListResponse> activeCall;
    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            if (getView() == null) {
                return;
            }
            loadProducts(etSearch != null ? etSearch.getText().toString() : null);
        }
    };

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
        etSearch = view.findViewById(R.id.etSearch);

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

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadProducts(null);

        return view;
    }

    private void loadProducts(String searchKeyword) {
        if (activeCall != null) {
            activeCall.cancel();
        }

        progressBar.setVisibility(View.VISIBLE);
        String normalizedSearch = TextUtils.isEmpty(searchKeyword) ? null : searchKeyword.trim();

        // Use skip/take pagination matching backend API
        activeCall = productAPIService.getProducts(normalizedSearch, null, null, null, null, 0, 20);
        activeCall.enqueue(new Callback<ProductListResponse>() {
            @Override
            public void onResponse(Call<ProductListResponse> call, Response<ProductListResponse> response) {
                if (!isAdded() || call.isCanceled()) {
                    return;
                }
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
                if (!isAdded() || call.isCanceled()) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                Log.e("SHOP_FRAGMENT", "Error loading data: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        searchHandler.removeCallbacks(searchRunnable);
        if (activeCall != null) {
            activeCall.cancel();
        }
        super.onDestroyView();
    }
}
