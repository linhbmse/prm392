package com.myfirstandroidjava.salesapp.network;

import com.myfirstandroidjava.salesapp.models.CheckoutRequest;
import com.myfirstandroidjava.salesapp.models.CheckoutResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PaymentAPIService {
    @POST("Payment/checkout")
    Call<CheckoutResponse> checkout(@Body CheckoutRequest request);
}
