package com.example.aiselfintroduction;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

// SelfIntroStorage 클래스 (자기소개서 데이터 관리)
class SelfIntroStorage {
    private static final String PREF_NAME = "SelfIntroPrefs";
    private SharedPreferences preferences;
    private Gson gson;

    public SelfIntroStorage(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // 자기소개서 저장
    public void saveSelfIntro(String title, SelfIntroData data) {
        String json = gson.toJson(data);
        preferences.edit().putString(title, json).apply();
    }

    // 자기소개서 로드
    public SelfIntroData loadSelfIntro(String title) {
        String json = preferences.getString(title, null);
        if (json == null) return null;
        return gson.fromJson(json, SelfIntroData.class);
    }

    // 자기소개서 삭제
    public void deleteSelfIntro(String title) {
        preferences.edit().remove(title).apply();
    }

    // 모든 자기소개서 이름 가져오기
    public List<String> getAllSelfIntroNames() {
        return new ArrayList<>(preferences.getAll().keySet());
    }
}