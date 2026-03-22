package com.myfirstandroidjava.salesapp.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;
import com.myfirstandroidjava.salesapp.models.RegisterDeviceTokenRequest;
import com.myfirstandroidjava.salesapp.network.DeviceTokenAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceTokenUtil {

    public static void sendDeviceToken(Context context) {
        FcmTokenManager fcmTokenManager = new FcmTokenManager(context);
        String savedToken = fcmTokenManager.getToken();

        if (savedToken != null) {
            sendTokenToServer(context, savedToken);
            fcmTokenManager.clearToken();
        } else {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("DEVICE_TOKEN_UTIL", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        String token = task.getResult();
                        sendTokenToServer(context, token);
                    });
        }
    }

    private static void sendTokenToServer(Context context, String token) {
        DeviceTokenAPIService service = RetrofitClient.getDeviceTokenAPIService(context);
        RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest(token);
        service.registerDeviceToken(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("DEVICE_TOKEN_UTIL", "Device token registered successfully");
                } else {
                    Log.e("DEVICE_TOKEN_UTIL", "Failed to register device token: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("DEVICE_TOKEN_UTIL", "Failed to register device token", t);
            }
        });
    }
}
