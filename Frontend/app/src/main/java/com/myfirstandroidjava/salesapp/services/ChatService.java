package com.myfirstandroidjava.salesapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.myfirstandroidjava.salesapp.utils.Constants;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

public class ChatService extends Service {

    private HubConnection hubConnection;
    private final IBinder binder = new ChatBinder();

    public class ChatBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();

        // Clean up BASE_URL in case user added '/api' or trailing slashes
        String baseUrl = Constants.BASE_URL.trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (baseUrl.endsWith("/api") || baseUrl.endsWith("api")) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("api")).trim();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
        }
        
        // Build hub connection with correct path and JWT token
        String hubUrl = baseUrl + "/hubs/chat";
        if (token != null && !token.isEmpty()) {
            hubUrl += "?access_token=" + token;
        }

        hubConnection = HubConnectionBuilder.create(hubUrl).build();

        // Listen for ReceiveMessage events from the hub
        hubConnection.on("ReceiveMessage", (chatMessageJson) -> {
            try {
                // The hub sends ChatMessageDto as a single object
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.myfirstandroidjava.salesapp.models.ChatMessage chatMessage =
                        gson.fromJson(chatMessageJson.toString(), com.myfirstandroidjava.salesapp.models.ChatMessage.class);

                Intent broadcastIntent = new Intent("new-message");
                broadcastIntent.putExtra("username", chatMessage.getUsername());
                broadcastIntent.putExtra("message", chatMessage.getMessage());
                broadcastIntent.putExtra("userId", chatMessage.getUserId());
                broadcastIntent.putExtra("sentAt", chatMessage.getSentAt());
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
            } catch (Exception e) {
                Log.e("ChatService", "Error parsing message: " + e.getMessage());
            }
        }, Object.class);

        new Thread(() -> {
            try {
                hubConnection.start().blockingAwait();
                Log.d("ChatService", "Connected to SignalR hub");
            } catch (Exception e) {
                Log.e("ChatService", "Failed to connect: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hubConnection != null) {
            hubConnection.stop();
        }
    }

    /**
     * Send a message via SignalR hub.
     * Backend ChatHub.SendMessage expects (string message, int? receiverUserId)
     */
    public void sendMessage(String message, Integer receiverUserId) {
        try {
            if (receiverUserId != null) {
                hubConnection.send("SendMessage", message, receiverUserId);
            } else {
                hubConnection.send("SendMessage", message);
            }
        } catch (Exception e) {
            Log.e("ChatService", "Error sending message: " + e.getMessage());
        }
    }
}
