package com.myfirstandroidjava.salesapp.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.models.NotificationItem;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem item, int position);
    }

    private List<NotificationItem> notifications;
    private OnNotificationClickListener listener;

    public NotificationAdapter(List<NotificationItem> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notifications.get(position);
        holder.tvMessage.setText(item.getMessage());

        // Format the createdAt date
        String createdAt = item.getCreatedAt();
        if (createdAt != null && createdAt.length() >= 16) {
            holder.tvTime.setText(createdAt.substring(0, 16).replace("T", " "));
        } else {
            holder.tvTime.setText("");
        }

        // Style based on read status
        if (item.isRead()) {
            holder.tvMessage.setTypeface(null, Typeface.NORMAL);
            holder.tvMessage.setTextColor(Color.GRAY);
            holder.itemView.setBackgroundColor(Color.WHITE);
        } else {
            holder.tvMessage.setTypeface(null, Typeface.BOLD);
            holder.tvMessage.setTextColor(Color.BLACK);
            holder.itemView.setBackgroundColor(Color.parseColor("#F0F8FF"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
        }
    }
}
