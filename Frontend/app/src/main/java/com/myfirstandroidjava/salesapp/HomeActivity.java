package com.myfirstandroidjava.salesapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.myfirstandroidjava.salesapp.fragments.AlertFragment;
import com.myfirstandroidjava.salesapp.fragments.CartFragment;
import com.myfirstandroidjava.salesapp.fragments.ChatFragment;
import com.myfirstandroidjava.salesapp.fragments.ShopFragment;
import com.myfirstandroidjava.salesapp.fragments.StoreLocationFragment;
import com.myfirstandroidjava.salesapp.models.NotificationBadge;
import com.myfirstandroidjava.salesapp.network.NotificationAPIService;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.services.FloatingBubbleService;
import com.myfirstandroidjava.salesapp.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private NotificationAPIService notificationAPIService;

    private final BroadcastReceiver badgeUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBadges();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        
        if (token != null && !token.isEmpty()) {
            notificationAPIService = RetrofitClient.getClient(this, token).create(NotificationAPIService.class);
            updateBadges();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ShopFragment())
                .commit();

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0);
        } else {
            startService(new Intent(HomeActivity.this, FloatingBubbleService.class));
        }
    }

    private void updateBadges() {
        if (notificationAPIService == null) return;

        notificationAPIService.getBadge().enqueue(new Callback<NotificationBadge>() {
            @Override
            public void onResponse(Call<NotificationBadge> call, Response<NotificationBadge> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NotificationBadge badgeData = response.body();
                    
                    // Update Cart Badge
                    if (badgeData.getCartItemCount() > 0) {
                        BadgeDrawable cartBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_cart);
                        cartBadge.setVisible(true);
                        cartBadge.setNumber(badgeData.getCartItemCount());
                    } else {
                        bottomNavigationView.removeBadge(R.id.nav_cart);
                    }

                    // Update Alerts Badge
                    if (badgeData.getUnreadNotifications() > 0) {
                        BadgeDrawable alertBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_alerts);
                        alertBadge.setVisible(true);
                        alertBadge.setNumber(badgeData.getUnreadNotifications());
                    } else {
                        bottomNavigationView.removeBadge(R.id.nav_alerts);
                    }
                }
            }

            @Override
            public void onFailure(Call<NotificationBadge> call, Throwable t) {
                Log.e("HOME_BADGE", "Error fetching badges: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction("new-message");
        filter.addAction("notification-received");
        filter.addAction("cart-updated");
        LocalBroadcastManager.getInstance(this).registerReceiver(badgeUpdateReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(badgeUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBadges();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Clear badge when selecting the item
        bottomNavigationView.removeBadge(itemId);

        if (itemId == R.id.nav_shop) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ShopFragment())
                    .commit();
            return true;
        } else if (itemId == R.id.nav_cart) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CartFragment())
                    .commit();
            return true;
        } else if (itemId == R.id.nav_chat) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatFragment())
                    .commit();
            return true;
        } else if (itemId == R.id.nav_alerts) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AlertFragment())
                    .commit();
            return true;
        } else if (itemId == R.id.nav_map) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new StoreLocationFragment())
                    .commit();
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (Settings.canDrawOverlays(this)) {
                startService(new Intent(this, FloatingBubbleService.class));
            } else {
                Toast.makeText(this, "Permission denied for bubble", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
