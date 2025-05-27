package com.quicinc.chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quicinc.chatapp.HomeActivity;
import com.quicinc.chatapp.InfoFormActivity;
import com.quicinc.chatapp.R;
import com.quicinc.chatapp.SelfIntro;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SelfIntroAdapter adapter;
    private List<SelfIntro> favoriteIntros;
    private SharedPreferences prefs;
    private static final String FAVORITES_KEY = "FAVORITE_INTROS";
    private boolean isManuallyUpdating = false;
    private LinearLayout emptyState;

    // SelfIntroStorage 추가
    private SelfIntroStorage selfIntroStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);

        // SelfIntroStorage 초기화
        selfIntroStorage = new SelfIntroStorage(this);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_favorite);

        recyclerView = findViewById(R.id.recyclerview_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        emptyState = findViewById(R.id.emptyState);

        favoriteIntros = new ArrayList<>();
        adapter = new SelfIntroAdapter(this, favoriteIntros);

        // 즐겨찾기 제거 리스너 설정
        adapter.setOnFavoriteChangedListener(new SelfIntroAdapter.OnFavoriteChangedListener() {
            @Override
            public void onFavoriteRemoved(SelfIntro item, int position) {
                isManuallyUpdating = true;
                adapter.removeItem(position);
                updateEmptyState();
                isManuallyUpdating = false;
            }
        });

        // 이름 변경 리스너 설정 추가
        adapter.setOnItemRenameListener(new SelfIntroAdapter.OnItemRenameListener() {
            @Override
            public void onItemRename(String oldName, String newName) {
                handleItemRename(oldName, newName);
            }
        });

        adapter.setOnDownloadClickListener(new SelfIntroAdapter.OnDownloadClickListener() {
            @Override
            public void onDownloadClicked(String introTitle) {
                navigateToDownload(introTitle); // 이 메서드 호출
            }
        });

        recyclerView.setAdapter(adapter);

        loadFavorites();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(FavoritesActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(FavoritesActivity.this, InfoFormActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isManuallyUpdating) {
            loadFavorites(); // 수동 업데이트 중이 아닐 때만 즐겨찾기 목록 갱신
        }
    }

    // 이름 변경 처리 함수 (HomeActivity와 동일한 로직)
    private void handleItemRename(String oldName, String newName) {
        try {
            // 로그 추가
            Log.d("FavoritesActivity", "이름 변경: " + oldName + " → " + newName);

            // 1. 기존 자기소개서 데이터 로드
            SelfIntroData oldData = selfIntroStorage.loadSelfIntro(oldName);

            if (oldData == null) {
                Log.e("FavoritesActivity", "기존 데이터를 찾을 수 없음: " + oldName);
                Log.d("FavoritesActivity", "기존 데이터가 없어서 빈 데이터를 생성합니다: " + oldName);
                oldData = new SelfIntroData(
                        "직무역량 내용을 입력하세요.",
                        "입사후포부 내용을 입력하세요.",
                        "지원동기 내용을 입력하세요.",
                        "성격장단점 내용을 입력하세요."
                );
            }

            // 2. 새 이름으로 데이터 저장
            selfIntroStorage.saveSelfIntro(newName, oldData);
            Log.d("FavoritesActivity", "새 이름으로 저장 완료: " + newName);

            // 3. 기존 이름의 데이터 삭제
            selfIntroStorage.deleteSelfIntro(oldName);
            Log.d("FavoritesActivity", "기존 데이터 삭제 완료: " + oldName);

            // 4. UI 리스트에서 이름 업데이트
            for (SelfIntro intro : favoriteIntros) {
                if (intro.getTitle().equals(oldName)) {
                    intro.setTitle(newName);
                    break;
                }
            }

            // 5. 어댑터에 변경 알림
            adapter.notifyDataSetChanged();

            // 6. 즐겨찾기 목록에서도 이름 업데이트
            Set<String> favorites = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());
            if (favorites.contains(oldName)) {
                Set<String> updatedFavorites = new HashSet<>(favorites);
                updatedFavorites.remove(oldName);
                updatedFavorites.add(newName);
                prefs.edit().putStringSet(FAVORITES_KEY, updatedFavorites).apply();
                Log.d("FavoritesActivity", "즐겨찾기 목록에서 이름 업데이트 완료");
            }

            // 7. 최근 편집 항목도 업데이트
            String recentIntro = prefs.getString(HomeActivity.LAST_EDITED_KEY, null);
            if (oldName.equals(recentIntro)) {
                prefs.edit().putString(HomeActivity.LAST_EDITED_KEY, newName).apply();
                Log.d("FavoritesActivity", "최근 편집 항목 업데이트 완료");
            }

            // 8. 성공 토스트 표시
            showYellowToast("이름이 변경되었습니다: " + newName);

        } catch (Exception e) {
            Log.e("FavoritesActivity", "이름 변경 실패", e);
            e.printStackTrace();
            showYellowToast("이름 변경 중 오류가 발생했습니다.");
        }
    }

    // 노란색 토스트 메서드 (HomeActivity와 동일)
    private void showYellowToast(String message) {
        try {
            // 커스텀 레이아웃 생성
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(40, 20, 40, 20);

            // 노란색 배경 설정
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(Color.parseColor("#FCD965"));
            shape.setCornerRadius(30);
            layout.setBackground(shape);

            // 텍스트 뷰 생성
            TextView textView = new TextView(this);
            textView.setText(message);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(16);
            textView.setGravity(Gravity.CENTER);
            layout.addView(textView);

            // 토스트 생성
            Toast toast = new Toast(this);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 150);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFavorites() {
        Set<String> favorites = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());

        favoriteIntros.clear();
        for (String title : favorites) {
            favoriteIntros.add(new SelfIntro(title, true));
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (favoriteIntros.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    // 저장소에서 자기소개서 삭제하는 메서드
    public void deleteSelfIntroFromStorage(String title) {
        try {
            // 1. 저장소에서 삭제
            selfIntroStorage.deleteSelfIntro(title);

            // 2. 최근 편집 항목이 삭제된 자기소개서면 제거
            SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
            String recentIntro = prefs.getString(HomeActivity.LAST_EDITED_KEY, null);
            if (title.equals(recentIntro)) {
                prefs.edit().remove(HomeActivity.LAST_EDITED_KEY).apply();
            }

            Log.d("FavoritesActivity", "저장소에서 자기소개서 삭제 완료: " + title);
            showYellowToast("자기소개서가 삭제되었습니다.");

        } catch (Exception e) {
            Log.e("FavoritesActivity", "저장소 삭제 실패", e);
            showYellowToast("삭제 중 오류가 발생했습니다.");
        }
    }

    public void navigateToDownload(String introTitle) {
        try {
            // 선택한 자기소개서 데이터 로드
            SelfIntroData introData = selfIntroStorage.loadSelfIntro(introTitle);

            if (introData == null) {
                showYellowToast("자기소개서 데이터를 찾을 수 없습니다.");
                return;
            }

            // JSON 형태로 변환
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("직무역량", introData.get직무역량());
            jsonObject.addProperty("입사후포부", introData.get입사후포부());
            jsonObject.addProperty("지원동기", introData.get지원동기());
            jsonObject.addProperty("성격장단점", introData.get성격장단점());

            String jsonData = new Gson().toJson(jsonObject);

            // 다운로드 액티비티로 이동
            Intent intent = new Intent(FavoritesActivity.this, DownloadActivity.class);
            intent.putExtra("resumeTitle", introTitle);
            intent.putExtra("aiJson", jsonData);
            startActivity(intent);

            Log.d("HomeActivity", "다운로드 화면으로 이동: " + introTitle);
        } catch (Exception e) {
            Log.e("HomeActivity", "다운로드 화면 이동 중 오류", e);
            showYellowToast("다운로드 준비 중 오류가 발생했습니다.");
        }
    }
}