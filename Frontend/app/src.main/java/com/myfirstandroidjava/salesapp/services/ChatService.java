package com.myfirstandroidjava.salesapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.myfirstandroidjava.salesapp.models.ChatMessage;
import java.util.Date;

public class ChatService extends Service {

    private static final String HUB_URL = "http://172.20.10.3:7002/chathub";
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
        hubConnection = HubConnectionBuilder.create(HUB_URL).build();

        hubConnection.on("ReceiveMessage", (user, message) -> {
            // Handle received message
        }, String.class, String.class);

        hubConnection.start().blockingAwait();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hubConnection.stop();
    }

    public void sendMessage(String user, String message) {
        try {
            hubConnection.send("SendMessage", user, message);
        } catch (Exception e) {
            Log.e("ChatService", e.getMessage());
        }
    }
}
