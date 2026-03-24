package com.myfirstandroidjava.salesapp.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
        private LinearLayout messageRoot;
        private LinearLayout messageBubble;
        private TextView textViewUser;
        private TextView textViewMessage;
        private TextView textViewTimestamp;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageRoot = itemView.findViewById(R.id.messageRoot);
            messageBubble = itemView.findViewById(R.id.messageBubble);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }

        public void bind(ChatMessage chatMessage) {
            boolean isCurrentUser = chatMessage.getUserId() == currentUserId;
            String message = chatMessage.getMessage() != null ? chatMessage.getMessage() : "";

            messageRoot.setGravity(isCurrentUser ? Gravity.END : Gravity.START);
            messageBubble.setBackgroundResource(isCurrentUser
                    ? R.drawable.bg_chat_bubble_outgoing
                    : R.drawable.bg_chat_bubble_incoming);

            textViewUser.setVisibility(isCurrentUser ? View.GONE : View.VISIBLE);
            textViewUser.setText(chatMessage.getUsername() != null ? chatMessage.getUsername() : "Support");
            textViewMessage.setText(message);
            textViewMessage.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    isCurrentUser ? android.R.color.white : R.color.text_primary));

            String sentAt = chatMessage.getSentAt();
            if (sentAt != null && sentAt.length() >= 16) {
                textViewTimestamp.setText(sentAt.substring(11, 16));
            } else {
                textViewTimestamp.setText("");
            }

            textViewTimestamp.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    isCurrentUser ? android.R.color.white : R.color.text_muted));
        }
    }
}
