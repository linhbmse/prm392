package com.myfirstandroidjava.salesapp.network;

import android.content.Context;

import com.myfirstandroidjava.salesapp.utils.Constants;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import java.io.IOException;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit publicRetrofit = null;

    /**
     * Helper to safely format the base URL
     */
    private static String getSafeApiBaseUrl() {
        String baseUrl = Constants.BASE_URL.trim();
        // Remove trailing slash if present
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        // If the user already included 'api', don't add it again
        if (baseUrl.endsWith("api")) {
            return baseUrl + "/";
        } else {
            return baseUrl + "/api/";
        }
    }

    public static Retrofit getClient(Context context, String token) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);

        if (token != null && !token.isEmpty()) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .method(original.method(), original.body());
                    return chain.proceed(requestBuilder.build());
                }
            });
        }

        return new Retrofit.Builder()
                .baseUrl(getSafeApiBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
    }

    /**
     * Create a public Retrofit client (no authentication).
     */
    public static Retrofit getClientPublic(Context context) {
        if (publicRetrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS);

            publicRetrofit = new Retrofit.Builder()
                    .baseUrl(getSafeApiBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return publicRetrofit;
    }

    // ===== Convenience Factory Methods =====

    public static DeviceTokenAPIService getDeviceTokenAPIService(Context context) {
        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken();
        return getClient(context, token).create(DeviceTokenAPIService.class);
    }

    public static ChatAPIService getChatAPIService(Context context) {
        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken();
        return getClient(context, token).create(ChatAPIService.class);
    }

    public static NotificationAPIService getNotificationAPIService(Context context) {
        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken();
        return getClient(context, token).create(NotificationAPIService.class);
    }

    public static UserAPIService getUserAPIService(Context context) {
        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken();
        return getClient(context, token).create(UserAPIService.class);
    }

    public static PaymentAPIService getPaymentAPIService(Context context) {
        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken();
        return getClient(context, token).create(PaymentAPIService.class);
    }
}