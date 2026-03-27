package com.myfirstandroidjava.salesapp.services;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.myfirstandroidjava.salesapp.models.RegisterDeviceTokenRequest;
import com.myfirstandroidjava.salesapp.network.DeviceTokenAPIService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.utils.FcmTokenManager;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        
        // Save the new token locally
        FcmTokenManager fcmTokenManager = new FcmTokenManager(this);
        fcmTokenManager.saveToken(token);

        // Also try to send it to the server immediately if user is logged in
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        TokenManager tokenManager = new TokenManager(this);
        String jwtToken = tokenManager.getToken();
        
        if (jwtToken != null && !jwtToken.isEmpty()) {
            DeviceTokenAPIService service = RetrofitClient.getClient(this, jwtToken).create(DeviceTokenAPIService.class);
            RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest(token);
            service.registerDeviceToken(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Token updated on server successfully");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Failed to update token on server", t);
                }
            });
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        String title = null;
        String body = null;
        int cartCount = 0;

        // 1. Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        // 2. Check if message contains a data payload (for cart count).
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            if (data.containsKey("cartCount")) {
                try {
                    cartCount = Integer.parseInt(data.get("cartCount"));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing cartCount", e);
                }
            }
            // If notification payload was empty, try to get from data
            if (title == null) title = data.get("title");
            if (body == null) body = data.get("body");
        }

        if (title != null && body != null) {
            sendNotification(title, body, cartCount);
        }

        // Notify activity to update badges in-app
        Intent intent = new Intent("notification-received");
        intent.putExtra("cartCount", cartCount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendNotification(String title, String messageBody, int cartCount) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "cart_notifications";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Cart Updates", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications about items in your cart");
            channel.setShowBadge(true); 
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_cart)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setNumber(cartCount)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, notificationBuilder.build());
    }
}
