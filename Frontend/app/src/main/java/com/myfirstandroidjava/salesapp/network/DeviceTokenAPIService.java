package com.myfirstandroidjava.salesapp.network;

import com.myfirstandroidjava.salesapp.models.RegisterDeviceTokenRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DeviceTokenAPIService {
    @POST("api/devices/register")
    Call<Void> registerDeviceToken(@Body RegisterDeviceTokenRequest request);
}
