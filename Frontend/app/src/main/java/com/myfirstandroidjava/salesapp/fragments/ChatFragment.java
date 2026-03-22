package com.myfirstandroidjava.salesapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.adapters.ChatAdapter;
import com.myfirstandroidjava.salesapp.models.ChatHistoryResponse;
import com.myfirstandroidjava.salesapp.models.ChatMessage;
import com.myfirstandroidjava.salesapp.network.ChatAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.services.ChatService;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends DialogFragment {

    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private Button buttonSend;
    private ChatAdapter chatAdapter;
    private ChatService chatService;
    private boolean isBound = false;

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String username = intent.getStringExtra("username");
            String message = intent.getStringExtra("message");
            int userId = intent.getIntExtra("userId", 0);
            String sentAt = intent.getStringExtra("sentAt");

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setUsername(username);
            chatMessage.setMessage(message);
            chatMessage.setUserId(userId);
            chatMessage.setSentAt(sentAt);

            chatAdapter.addMessage(chatMessage);
            recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
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
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), ChatService.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(messageReceiver, new IntentFilter("new-message"));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            getActivity().unbindService(connection);
            isBound = false;
        }
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(messageReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter(new ArrayList<>(), new TokenManager(getContext()).getUserId());
        recyclerViewChat.setAdapter(chatAdapter);

        buttonSend.setOnClickListener(v -> {
            if (isBound) {
                String message = editTextMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    chatService.sendMessage(message, null);
                    editTextMessage.setText("");
                }
            }
        });

        loadChatHistory();
    }

    private void loadChatHistory() {
        ChatAPIService chatAPIService = RetrofitClient.getChatAPIService(getContext());
        chatAPIService.getChatHistory(null, 0, 50).enqueue(new Callback<ChatHistoryResponse>() {
            @Override
            public void onResponse(Call<ChatHistoryResponse> call, Response<ChatHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatAdapter.setMessages(response.body().getMessages());
                    if (chatAdapter.getItemCount() > 0) {
                        recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatHistoryResponse> call, Throwable t) {
                // Handle failure
            }
        });
    }
}
