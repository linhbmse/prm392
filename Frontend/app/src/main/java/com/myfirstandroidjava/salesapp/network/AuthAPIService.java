package com.myfirstandroidjava.salesapp.network;

import com.myfirstandroidjava.salesapp.models.RegisterRequest;
import com.myfirstandroidjava.salesapp.models.LoginRequest;
import com.myfirstandroidjava.salesapp.models.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthAPIService {

    @POST("Auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("Auth/register")
    Call<LoginResponse> registerUser(@Body RegisterRequest registerRequest); // Assuming register also returns a token in LoginResponse
}