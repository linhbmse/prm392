package com.myfirstandroidjava.salesapp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.myfirstandroidjava.salesapp.fragments.AlertFragment;
import com.myfirstandroidjava.salesapp.fragments.CartFragment;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import com.myfirstandroidjava.salesapp.fragments.ChatFragment;
import com.myfirstandroidjava.salesapp.fragments.StoreLocationFragment;
import com.myfirstandroidjava.salesapp.fragments.ShopFragment;
import com.myfirstandroidjava.salesapp.services.FloatingBubbleService;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

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

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

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
}