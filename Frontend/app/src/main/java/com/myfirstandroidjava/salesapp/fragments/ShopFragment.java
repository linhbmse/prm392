package com.myfirstandroidjava.salesapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.myfirstandroidjava.salesapp.LoginActivity;
import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.adapters.ProductAdapter;
import com.myfirstandroidjava.salesapp.models.ProductItem;
import com.myfirstandroidjava.salesapp.models.ProductListResponse;
import com.myfirstandroidjava.salesapp.network.ProductAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ProductAdapter adapter;
    private Button btnLoginRegister;
    private EditText etSearch;
    private Chip chipSortPrice, chipFilterCategory, chipReset;
    private ProductAPIService productAPIService;
    
    private List<ProductItem> originalList = new ArrayList<>();
    private boolean isAscending = true;
    private String selectedCategory = null;

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
        chipSortPrice = view.findViewById(R.id.chipSortPrice);
        chipFilterCategory = view.findViewById(R.id.chipFilterCategory);
        chipReset = view.findViewById(R.id.chipReset);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        setupAuthButton(tokenManager, token);
        setupFilters();
        loadProducts();

        return view;
    }

    private void setupAuthButton(TokenManager tokenManager, String token) {
        if (token == null) {
            btnLoginRegister.setText("Login / Register");
            btnLoginRegister.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            });
        } else {
            btnLoginRegister.setText("Logout");
            btnLoginRegister.setOnClickListener(v -> {
                tokenManager.clearToken();
                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                requireActivity().recreate();
            });
        }
    }

    private void setupFilters() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyAllFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipSortPrice.setOnClickListener(v -> {
            isAscending = !isAscending;
            chipSortPrice.setText(isAscending ? "Giá: Thấp -> Cao" : "Giá: Cao -> Thấp");
            applyAllFilters();
        });

        chipFilterCategory.setOnClickListener(v -> showCategoryFilterDialog());

        chipReset.setOnClickListener(v -> {
            etSearch.setText("");
            isAscending = true;
            selectedCategory = null;
            chipSortPrice.setText("Giá: Thấp -> Cao");
            chipFilterCategory.setText("Danh mục");
            applyAllFilters();
        });
    }

    private void showCategoryFilterDialog() {
        if (originalList.isEmpty()) return;

        Set<String> categoriesSet = new HashSet<>();
        for (ProductItem item : originalList) {
            if (item.getCategoryName() != null && !item.getCategoryName().isEmpty()) {
                categoriesSet.add(item.getCategoryName());
            }
        }
        
        if (categoriesSet.isEmpty()) {
            Toast.makeText(getContext(), "Không có danh mục nào", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] categories = categoriesSet.toArray(new String[0]);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn danh mục");
        builder.setItems(categories, (dialog, which) -> {
            selectedCategory = categories[which];
            chipFilterCategory.setText(selectedCategory);
            applyAllFilters();
        });
        builder.show();
    }

    private void applyAllFilters() {
        String searchText = etSearch.getText().toString().toLowerCase().trim();
        List<ProductItem> filteredList = new ArrayList<>();

        for (ProductItem item : originalList) {
            boolean matchesSearch = item.getProductName().toLowerCase().contains(searchText);
            boolean matchesCategory = (selectedCategory == null || 
                                      (item.getCategoryName() != null && item.getCategoryName().equals(selectedCategory)));
            
            if (matchesSearch && matchesCategory) {
                filteredList.add(item);
            }
        }

        // Apply Sorting
        Collections.sort(filteredList, (p1, p2) -> {
            if (isAscending) return Double.compare(p1.getPrice(), p2.getPrice());
            else return Double.compare(p2.getPrice(), p1.getPrice());
        });

        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        Call<ProductListResponse> call = productAPIService.getProducts(null, null, null, null, null, 0, 100);
        call.enqueue(new Callback<ProductListResponse>() {
            @Override
            public void onResponse(Call<ProductListResponse> call, Response<ProductListResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    originalList = response.body().getItems();
                    adapter = new ProductAdapter(new ArrayList<>(originalList));
                    recyclerView.setAdapter(adapter);
                    // Initial sort
                    applyAllFilters();
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
