package com.myfirstandroidjava.salesapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirstandroidjava.salesapp.adapters.ChatAdapter;
import com.myfirstandroidjava.salesapp.models.ChatHistoryResponse;
import com.myfirstandroidjava.salesapp.models.ChatMessage;
import com.myfirstandroidjava.salesapp.models.SendMessageRequest;
import com.myfirstandroidjava.salesapp.network.ChatAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.services.ChatService;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Chat detail screen for Admin to chat with a specific user.
 * Receives otherUserId and username via Intent extras.
 */
public class ChatDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChatDetailActivity";

    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private Button buttonSend;
    private ChatAdapter chatAdapter;
    private ChatAPIService chatAPIService;

    private int otherUserId;
    private String otherUsername;

    // SignalR
    private ChatService chatService;
    private boolean isBound = false;

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int senderId = intent.getIntExtra("userId", 0);
            // Only show messages from the user we're chatting with,
            // or messages from ourselves (admin)
            int myUserId = new TokenManager(ChatDetailActivity.this).getUserId();
            if (senderId == otherUserId || senderId == myUserId) {
                String username = intent.getStringExtra("username");
                String message = intent.getStringExtra("message");
                String sentAt = intent.getStringExtra("sentAt");

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setUsername(username);
                chatMessage.setMessage(message);
                chatMessage.setUserId(senderId);
                chatMessage.setSentAt(sentAt);

                chatAdapter.addMessage(chatMessage);
                recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ChatService.ChatBinder binder = (ChatService.ChatBinder) service;
            chatService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        otherUserId = getIntent().getIntExtra("otherUserId", -1);
        otherUsername = getIntent().getStringExtra("username");

        if (otherUserId == -1) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatAPIService = RetrofitClient.getChatAPIService(this);
        TokenManager tokenManager = new TokenManager(this);

        // Setup views
        TextView tvChatUsername = findViewById(R.id.tvChatUsername);
        tvChatUsername.setText(otherUsername != null ? otherUsername : "User #" + otherUserId);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(new ArrayList<>(), tokenManager.getUserId());
        recyclerViewChat.setAdapter(chatAdapter);

        buttonSend.setOnClickListener(v -> sendMessage());

        loadChatHistory();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageReceiver, new IntentFilter("new-message"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }

    private void loadChatHistory() {
        chatAPIService.getChatHistory(otherUserId, 0, 50).enqueue(new Callback<ChatHistoryResponse>() {
            @Override
            public void onResponse(Call<ChatHistoryResponse> call, Response<ChatHistoryResponse> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null) {
                    chatAdapter.setMessages(response.body().getMessages());
                    if (chatAdapter.getItemCount() > 0) {
                        recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                } else {
                    Toast.makeText(ChatDetailActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatHistoryResponse> call, Throwable t) {
                if (isFinishing()) return;
                Toast.makeText(ChatDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading chat history", t);
            }
        });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        editTextMessage.setText("");

        // Admin must send receiverUserId
        SendMessageRequest request = new SendMessageRequest(messageText, otherUserId);

        chatAPIService.sendMessage(request).enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null) {
                    chatAdapter.addMessage(response.body());
                    recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
                } else {
                    Toast.makeText(ChatDetailActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                if (isFinishing()) return;
                Toast.makeText(ChatDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error sending message", t);
            }
        });
    }
}
