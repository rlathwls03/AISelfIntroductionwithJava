package com.example.aiselfintroduction;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// SelfIntroStorage 클래스 (자기소개서 데이터 관리)
class SelfIntroStorage {
    private static final String PREF_NAME = "SelfIntroPrefs";
    private SharedPreferences preferences;
    private Gson gson;
    private Context context;
    public SelfIntroStorage(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
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

    public boolean renameSelfIntro(String oldName, SelfIntroData data, String newName) {
        try {
            // 1. 먼저 새 이름으로 데이터 저장
            saveSelfIntro(newName, data);

            // 2. 기존 이름의 데이터 삭제
            deleteSelfIntro(oldName);

            // 3. 최근 편집 항목도 업데이트
            SharedPreferences prefs = context.getSharedPreferences("IntroPrefs", MODE_PRIVATE);
            String recentIntro = prefs.getString(HomeActivity.LAST_EDITED_KEY, null);
            if (oldName.equals(recentIntro)) {
                prefs.edit().putString(HomeActivity.LAST_EDITED_KEY, newName).apply();
            }

            // 4. 즐겨찾기 목록에서도 이름 업데이트
            Set<String> favorites = prefs.getStringSet("FAVORITE_INTROS", new HashSet<>());
            if (favorites.contains(oldName)) {
                Set<String> updatedFavorites = new HashSet<>(favorites);
                updatedFavorites.remove(oldName);
                updatedFavorites.add(newName);
                prefs.edit().putStringSet("FAVORITE_INTROS", updatedFavorites).apply();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}