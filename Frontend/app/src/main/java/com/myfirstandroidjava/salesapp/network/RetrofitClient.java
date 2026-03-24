package com.myfirstandroidjava.salesapp.network;

import android.content.Context;

import com.myfirstandroidjava.salesapp.utils.Constants;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.dnsoverhttps.DnsOverHttps;
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
        return new Retrofit.Builder()
                .baseUrl(getSafeApiBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(buildHttpClient(token))
                .build();
    }

    /**
     * Create a public Retrofit client (no authentication).
     */
    public static Retrofit getClientPublic(Context context) {
        if (publicRetrofit == null) {
            publicRetrofit = new Retrofit.Builder()
                    .baseUrl(getSafeApiBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(buildHttpClient(null))
                    .build();
        }
        return publicRetrofit;
    }

    private static OkHttpClient buildHttpClient(String token) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .callTimeout(35, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        if (token != null && !token.isEmpty()) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .method(original.method(), original.body());
                    return chain.proceed(requestBuilder.build());
                }
            });
        }

        try {
            OkHttpClient bootstrapClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            DnsOverHttps dnsOverHttps = new DnsOverHttps.Builder()
                    .client(bootstrapClient)
                    .url(HttpUrl.get("https://dns.google/dns-query"))
                    .bootstrapDnsHosts(
                            InetAddress.getByName("8.8.8.8"),
                            InetAddress.getByName("8.8.4.4"))
                    .resolvePrivateAddresses(true)
                    .systemDns(Dns.SYSTEM)
                    .build();

            httpClient.dns(dnsOverHttps);
        } catch (Exception ignored) {
            // Fall back to the default resolver if DoH cannot be created.
        }

        return httpClient.build();
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
