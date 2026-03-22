package com.myfirstandroidjava.salesapp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.myfirstandroidjava.salesapp.fragments.ChatFragment;

public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.show(getSupportFragmentManager(), "ChatFragment");
    }
}
