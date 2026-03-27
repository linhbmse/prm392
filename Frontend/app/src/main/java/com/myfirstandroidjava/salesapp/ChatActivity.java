package com.myfirstandroidjava.salesapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Legacy ChatActivity - redirects to HomeActivity chat tab.
 * Previously used DialogFragment.show(), now ChatFragment is a regular Fragment.
 */
public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Redirect to HomeActivity - the chat is now a regular fragment in bottom nav
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("navigate_to", "chat");
        startActivity(intent);
        finish();
    }
}
