package com.myfirstandroidjava.salesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.myfirstandroidjava.salesapp.models.LoginRequest;
import com.myfirstandroidjava.salesapp.models.LoginResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.myfirstandroidjava.salesapp.models.RegisterDeviceTokenRequest;
import com.myfirstandroidjava.salesapp.network.AuthAPIService;
import com.myfirstandroidjava.salesapp.network.DeviceTokenAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.utils.FcmTokenManager;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private AuthAPIService authAPIService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewRegister = findViewById(R.id.textViewRegister);

        authAPIService = RetrofitClient.getClient(this, null).create(AuthAPIService.class);

        buttonLogin.setOnClickListener(v -> loginUser());

        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest loginRequest = new LoginRequest(username, password);
        authAPIService.loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.isSuccess() && loginResponse.getToken() != null) {
                        // Save token and role
                        TokenManager tokenManager = new TokenManager(LoginActivity.this);
                        tokenManager.saveToken(loginResponse.getToken());
                        if (loginResponse.getRole() != null) {
                            tokenManager.saveRole(loginResponse.getRole());
                        }

                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                        // Send device token to the backend
                        sendDeviceToken();

                        // Navigate to HomeActivity
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String msg = loginResponse.getMessage() != null ? loginResponse.getMessage() : "Login failed";
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("LOGIN_FAILURE", t.getMessage(), t);
            }
        });
    }

    private void sendDeviceToken() {
        FcmTokenManager fcmTokenManager = new FcmTokenManager(this);
        String savedToken = fcmTokenManager.getToken();

        if (savedToken != null) {
            sendTokenToServer(savedToken);
            fcmTokenManager.clearToken();
        } else {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("LOGIN_ACTIVITY", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        String token = task.getResult();
                        sendTokenToServer(token);
                    });
        }
    }

    private void sendTokenToServer(String token) {
        DeviceTokenAPIService service = RetrofitClient.getDeviceTokenAPIService(LoginActivity.this);
        RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest(token);
        service.registerDeviceToken(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("LOGIN_ACTIVITY", "Device token registered successfully");
                } else {
                    Log.e("LOGIN_ACTIVITY", "Failed to register device token: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("LOGIN_ACTIVITY", "Failed to register device token", t);
            }
        });
    }
}