package com.myfirstandroidjava.salesapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.models.ConversationItem;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<ConversationItem> conversations;
    private final OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(ConversationItem conversation);
    }

    public ConversationAdapter(List<ConversationItem> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ConversationItem item = conversations.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void setConversations(List<ConversationItem> conversations) {
        this.conversations = conversations;
        notifyDataSetChanged();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatar;
        private final TextView tvUsername;
        private final TextView tvLastMessage;
        private final TextView tvTime;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(ConversationItem item) {
            String username = item.getUsername() != null ? item.getUsername() : "User";
            tvUsername.setText(username);
            tvAvatar.setText(username.substring(0, 1).toUpperCase());

            String lastMsg = item.getLastMessage();
            tvLastMessage.setText(lastMsg != null ? lastMsg : "");

            String time = item.getLastMessageAt();
            if (time != null && time.length() >= 16) {
                tvTime.setText(time.substring(11, 16));
            } else {
                tvTime.setText("");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(item);
                }
            });
        }
    }
}
