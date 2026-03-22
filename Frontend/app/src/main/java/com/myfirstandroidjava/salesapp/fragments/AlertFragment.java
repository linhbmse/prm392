package com.myfirstandroidjava.salesapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.adapters.NotificationAdapter;
import com.myfirstandroidjava.salesapp.models.GenericResponse;
import com.myfirstandroidjava.salesapp.models.NotificationItem;
import com.myfirstandroidjava.salesapp.models.NotificationListResponse;
import com.myfirstandroidjava.salesapp.network.NotificationAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private NotificationAdapter adapter;
    private NotificationAPIService notificationAPIService;
    private List<NotificationItem> notifications = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alert, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notifications, this);
        recyclerView.setAdapter(adapter);

        notificationAPIService = RetrofitClient.getNotificationAPIService(requireContext());

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);

        notificationAPIService.getNotifications(0, 50).enqueue(new Callback<NotificationListResponse>() {
            @Override
            public void onResponse(Call<NotificationListResponse> call, Response<NotificationListResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    notifications.clear();
                    notifications.addAll(response.body().getItems());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NotificationListResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("NOTIFICATION", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Error loading notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNotificationClick(NotificationItem item, int position) {
        if (!item.isRead()) {
            notificationAPIService.markRead(item.getNotificationId()).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (response.isSuccessful()) {
                        item.setRead(true);
                        adapter.notifyItemChanged(position);
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    Log.e("NOTIFICATION", "Error marking as read: " + t.getMessage());
                }
            });
        }
    }
}