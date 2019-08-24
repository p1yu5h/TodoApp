package com.piyushsatija.todo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefUtils {
    private static final String SHARED_PREF_NAME = "user";
    private static SharedPrefUtils INSTANCE = null;
    private SharedPreferences preferences;

    public static SharedPrefUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SharedPrefUtils(context);
            return INSTANCE;
        }
        return INSTANCE;
    }

    private SharedPrefUtils(Context context) {
        preferences = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    }

    public void saveUserEmail(String email) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.apply();
    }

    public String getUserEmail() {
        return preferences.getString("email","");
    }

    public void saveUserName(String name) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name",name);
        editor.apply();
    }

    public String getUserName() {
        return preferences.getString("name","");
    }

    public void clearAllPrefs() {
        preferences.edit().clear().apply();
    }
}
