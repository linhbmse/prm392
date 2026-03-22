package com.myfirstandroidjava.salesapp.adapters;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages;
    private int currentUserId;

    public ChatAdapter(List<ChatMessage> chatMessages, int currentUserId) {
        this.chatMessages = chatMessages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        holder.bind(chatMessage);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyItemInserted(chatMessages.size() - 1);
    }

    public void setMessages(List<ChatMessage> messages) {
        this.chatMessages = messages;
        notifyDataSetChanged();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewUser;
        private TextView textViewMessage;
        private TextView textViewTimestamp;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }

        public void bind(ChatMessage chatMessage) {
            String message = chatMessage.getMessage();

            textViewUser.setVisibility(View.VISIBLE);
            textViewUser.setText(chatMessage.getUsername() != null ? chatMessage.getUsername() : "Unknown");
            textViewMessage.setText(message);

            // Display sentAt timestamp
            String sentAt = chatMessage.getSentAt();
            if (sentAt != null && sentAt.length() >= 16) {
                // Parse ISO date string, show just time part
                textViewTimestamp.setText(sentAt.substring(11, 16));
            } else {
                textViewTimestamp.setText("");
            }
        }
    }
}
