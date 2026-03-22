package com.myfirstandroidjava.salesapp.fragments;

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

import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.models.ChangePasswordRequest;
import com.myfirstandroidjava.salesapp.models.GenericResponse;
import com.myfirstandroidjava.salesapp.models.UpdateProfileRequest;
import com.myfirstandroidjava.salesapp.models.UserProfile;
import com.myfirstandroidjava.salesapp.network.UserAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvRole;
    private EditText etEmail, etPhone, etAddress;
    private Button btnSaveProfile;
    private ProgressBar progressBar;
    private UserAPIService userAPIService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvRole = view.findViewById(R.id.tvRole);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        progressBar = view.findViewById(R.id.progressBar);

        userAPIService = RetrofitClient.getUserAPIService(requireContext());

        loadProfile();

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        return view;
    }

    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);

        userAPIService.getProfile().enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();
                    tvUsername.setText(profile.getUsername());
                    tvRole.setText("Role: " + profile.getRole());
                    etEmail.setText(profile.getEmail());
                    etPhone.setText(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "");
                    etAddress.setText(profile.getAddress() != null ? profile.getAddress() : "");
                } else {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("PROFILE", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        UpdateProfileRequest request = new UpdateProfileRequest(
                email.isEmpty() ? null : email,
                phone.isEmpty() ? null : phone,
                address.isEmpty() ? null : address
        );

        progressBar.setVisibility(View.VISIBLE);
        btnSaveProfile.setEnabled(false);

        userAPIService.updateProfile(request).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                progressBar.setVisibility(View.GONE);
                btnSaveProfile.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSaveProfile.setEnabled(true);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}