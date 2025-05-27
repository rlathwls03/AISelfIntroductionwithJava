package com.quicinc.chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class UserInfoStorage {
    private static final String PREF_NAME = "UserInfoPrefs";
    private static final String KEY_USER_INFO = "user_info";
    private final SharedPreferences preferences;
    private final Gson gson;

    public UserInfoStorage(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveUserInfo(UserInfo userInfo) {
        String json = gson.toJson(userInfo);
        preferences.edit().putString(KEY_USER_INFO, json).commit();
    }

    public UserInfo loadUserInfo() {
        String json = preferences.getString(KEY_USER_INFO, null);
        if (json == null) {
            return new UserInfo();
        }
        Type type = new TypeToken<UserInfo>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void clearUserInfo() {
        preferences.edit().remove(KEY_USER_INFO).commit();
    }
}