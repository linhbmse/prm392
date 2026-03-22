package com.myfirstandroidjava.salesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.myfirstandroidjava.salesapp.models.LoginResponse;
import com.myfirstandroidjava.salesapp.models.RegisterRequest;
import com.myfirstandroidjava.salesapp.network.AuthAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword, editTextPhone;
    private Button buttonRegister;
    private TextView textViewLogin;
    private AuthAPIService authAPIService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);

        authAPIService = RetrofitClient.getClient(this, null).create(AuthAPIService.class);

        buttonRegister.setOnClickListener(v -> registerUser());

        textViewLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void registerUser() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest registerRequest = new RegisterRequest(username, password, email, phone.isEmpty() ? null : phone);
        authAPIService.registerUser(registerRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.isSuccess()) {
                        // Save token and role
                        TokenManager tokenManager = new TokenManager(RegisterActivity.this);
                        if (loginResponse.getToken() != null) {
                            tokenManager.saveToken(loginResponse.getToken());
                        }
                        if (loginResponse.getRole() != null) {
                            tokenManager.saveRole(loginResponse.getRole());
                        }

                        Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                        // Navigate to HomeActivity
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String msg = loginResponse.getMessage() != null ? loginResponse.getMessage() : "Registration failed";
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Registration failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("REGISTER_FAILURE", t.getMessage(), t);
            }
        });
    }
}