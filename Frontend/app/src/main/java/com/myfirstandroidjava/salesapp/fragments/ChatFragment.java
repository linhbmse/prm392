package com.myfirstandroidjava.salesapp.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirstandroidjava.salesapp.ChatDetailActivity;
import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.adapters.ChatAdapter;
import com.myfirstandroidjava.salesapp.adapters.ConversationAdapter;
import com.myfirstandroidjava.salesapp.models.ChatHistoryResponse;
import com.myfirstandroidjava.salesapp.models.ChatMessage;
import com.myfirstandroidjava.salesapp.models.ConversationItem;
import com.myfirstandroidjava.salesapp.models.SendMessageRequest;
import com.myfirstandroidjava.salesapp.network.ChatAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.services.ChatService;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    // Common
    private ChatAPIService chatAPIService;
    private TokenManager tokenManager;
    private boolean isAdmin = false;

    // Admin views
    private LinearLayout layoutAdminConversations;
    private RecyclerView recyclerViewConversations;
    private ProgressBar progressBarConversations;
    private TextView tvEmptyConversations;
    private ConversationAdapter conversationAdapter;

    // User views
    private LinearLayout layoutUserChat;
    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private Button buttonSend;
    private ChatAdapter chatAdapter;

    // SignalR
    private ChatService chatService;
    private boolean isBound = false;

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isAdmin && chatAdapter != null) {
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
    public void onStart() {
        super.onStart();
        if (!isAdmin) {
            Intent intent = new Intent(getActivity(), ChatService.class);
            getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                    messageReceiver, new IntentFilter("new-message"));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!isAdmin) {
            if (isBound) {
                getActivity().unbindService(connection);
                isBound = false;
            }
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh conversation list when returning from ChatDetailActivity
        if (isAdmin) {
            loadConversations();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        chatAPIService = RetrofitClient.getChatAPIService(requireContext());
        String role = tokenManager.getRole();
        isAdmin = "Admin".equalsIgnoreCase(role);

        // Admin views
        layoutAdminConversations = view.findViewById(R.id.layoutAdminConversations);
        recyclerViewConversations = view.findViewById(R.id.recyclerViewConversations);
        progressBarConversations = view.findViewById(R.id.progressBarConversations);
        tvEmptyConversations = view.findViewById(R.id.tvEmptyConversations);

        // User views
        layoutUserChat = view.findViewById(R.id.layoutUserChat);
        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        // Header texts
        TextView tvChatTitle = view.findViewById(R.id.tvChatTitle);
        TextView tvChatSubtitle = view.findViewById(R.id.tvChatSubtitle);

        if (isAdmin) {
            setupAdminView(tvChatTitle, tvChatSubtitle);
        } else {
            setupUserView();
        }
    }

    // ======================== ADMIN ========================

    private void setupAdminView(TextView tvTitle, TextView tvSubtitle) {
        layoutAdminConversations.setVisibility(View.VISIBLE);
        layoutUserChat.setVisibility(View.GONE);

        tvTitle.setText("Manage chats");
        tvSubtitle.setText("View and reply to customer conversations.");

        recyclerViewConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        conversationAdapter = new ConversationAdapter(new ArrayList<>(), conversation -> {
            Intent intent = new Intent(getActivity(), ChatDetailActivity.class);
            intent.putExtra("otherUserId", conversation.getUserId());
            intent.putExtra("username", conversation.getUsername());
            startActivity(intent);
        });
        recyclerViewConversations.setAdapter(conversationAdapter);

        loadConversations();
    }

    private void loadConversations() {
        progressBarConversations.setVisibility(View.VISIBLE);
        tvEmptyConversations.setVisibility(View.GONE);

        chatAPIService.getConversations().enqueue(new Callback<List<ConversationItem>>() {
            @Override
            public void onResponse(Call<List<ConversationItem>> call, Response<List<ConversationItem>> response) {
                if (!isAdded()) return;
                progressBarConversations.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<ConversationItem> conversations = response.body();
                    conversationAdapter.setConversations(conversations);
                    tvEmptyConversations.setVisibility(conversations.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvEmptyConversations.setVisibility(View.VISIBLE);
                    tvEmptyConversations.setText("Failed to load conversations");
                }
            }

            @Override
            public void onFailure(Call<List<ConversationItem>> call, Throwable t) {
                if (!isAdded()) return;
                progressBarConversations.setVisibility(View.GONE);
                tvEmptyConversations.setVisibility(View.VISIBLE);
                tvEmptyConversations.setText("Error: " + t.getMessage());
                Log.e(TAG, "Error loading conversations", t);
            }
        });
    }

    // ======================== USER ========================

    private void setupUserView() {
        layoutAdminConversations.setVisibility(View.GONE);
        layoutUserChat.setVisibility(View.VISIBLE);

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter(new ArrayList<>(), tokenManager.getUserId());
        recyclerViewChat.setAdapter(chatAdapter);

        buttonSend.setOnClickListener(v -> sendMessageAsUser());

        loadChatHistory();
    }

    private void loadChatHistory() {
        chatAPIService.getChatHistory(null, 0, 50).enqueue(new Callback<ChatHistoryResponse>() {
            @Override
            public void onResponse(Call<ChatHistoryResponse> call, Response<ChatHistoryResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    chatAdapter.setMessages(response.body().getMessages());
                    if (chatAdapter.getItemCount() > 0) {
                        recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatHistoryResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Error loading chat history", t);
            }
        });
    }

    private void sendMessageAsUser() {
        String messageText = editTextMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        editTextMessage.setText("");
        SendMessageRequest request = new SendMessageRequest(messageText, null);

        chatAPIService.sendMessage(request).enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    chatAdapter.addMessage(response.body());
                    recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
                } else {
                    Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error sending message", t);
            }
        });
    }
}
