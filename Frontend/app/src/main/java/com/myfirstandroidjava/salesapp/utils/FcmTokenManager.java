package com.myfirstandroidjava.salesapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class FcmTokenManager {
    private static final String SHARED_PREF_NAME = "fcm_token_pref";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public FcmTokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveToken(String token) {
        editor.putString(KEY_FCM_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null);
    }

    public void clearToken() {
        editor.remove(KEY_FCM_TOKEN);
        editor.apply();
    }
}
