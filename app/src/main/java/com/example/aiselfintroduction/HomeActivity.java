package com.example.aiselfintroduction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {
    private TextView userName, userPhone, userEmail, recentText;
    private ListView selfIntroListView;
    private FloatingActionButton fabAddIntro;
    private LinearLayout recentContainer;
    private RecyclerView recyclerView;
    private SelfIntroAdapter adapter;
    private List<SelfIntro> introList;
    private ArrayList<String> introTitles;
    public static final String LAST_EDITED_KEY = "LAST_EDITED_INTRO_NAME";
    // SelfIntroStorage 클래스 추가 (자기소개서 저장/불러오기 관리)
    private SelfIntroStorage selfIntroStorage;


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 화면이 다시 보일 때마다 즐겨찾기 상태 갱신
        SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet("FAVORITE_INTROS", new HashSet<>());

        for (SelfIntro intro : introList) {
            intro.setFavorite(favorites.contains(intro.getTitle()));
        }
        adapter.notifyDataSetChanged();

        // 저장된 자기소개서 목록을 다시 불러와서 전체 리스트 갱신
        refreshIntroList();

        // 최근 편집 항목도 다시 불러와서 업데이트
        updateRecentIntroDisplay();
    }

    // 최근 수정된 자기소개서 정보를 불러와 UI에 표시하는 메서드
    private void updateRecentIntroDisplay() {
        // SharedPreferences에서 최근 편집 항목 불러오기
        SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
        String recentIntroName = prefs.getString(LAST_EDITED_KEY, null);

        // 항상 recentContainer 보이도록 설정
        recentContainer.setVisibility(View.VISIBLE);

        // 로그 추가
        Log.d("HomeActivity", "최근 수정된 자기소개서 업데이트: " + recentIntroName);

        if (recentIntroName != null && !recentIntroName.isEmpty()) {
            // 최근 편집한 자기소개서가 있는 경우
            recentText.setText(recentIntroName);

            // 클릭 리스너 설정
            recentContainer.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, EditListActivityFromHome.class);
                intent.putExtra("resumeTitle", recentIntroName);
                startActivity(intent);
                Log.d("HomeActivity", "최근 자기소개서 클릭: " + recentIntroName);
            });
        } else {
            // 최근 편집한 자기소개서가 없는 경우
            recentText.setText("수정한 자기소개서가 없음");
            // 클릭 비활성화
            recentContainer.setOnClickListener(null);
        }
    }

    // 자기소개서 리스트 전체 갱신 메서드
    private void refreshIntroList() {
        // 현재 저장된 모든 자기소개서 이름 가져오기
        List<String> savedNames = selfIntroStorage.getAllSelfIntroNames();
        SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet("FAVORITE_INTROS", new HashSet<>());

        // 기존 리스트 클리어 후 최신 데이터로 재구성
        introList.clear();

        // 저장된 이름들로 리스트 재구성
        for (String title : savedNames) {
            boolean isFavorite = favorites.contains(title);
            introList.add(new SelfIntro(title, isFavorite));
        }

        // UI 업데이트
        adapter.notifyDataSetChanged();

        Log.d("HomeActivity", "자기소개서 리스트 갱신 완료: " + savedNames.size() + "개");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
//        getSupportActionBar().hide();

        // SelfIntroStorage 초기화
        selfIntroStorage = new SelfIntroStorage(this);

        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);
        userEmail = findViewById(R.id.userEmail);
        recentText = findViewById(R.id.recentText);
        recentContainer = findViewById(R.id.recentContainer);
        fabAddIntro = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerView);

        // 사용자 정보 불러오기
        UserInfoStorage userInfoStorage = new UserInfoStorage(this);
        UserInfo userInfo = userInfoStorage.loadUserInfo();
        if (userInfo != null) {
            userName.setText(userInfo.getName());
            userPhone.setText(userInfo.getPhone());
            userEmail.setText(userInfo.getEmail());
        }

        // 하단 네비게이션 바 처리
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // 홈 탭: 현재 화면이므로 아무 동작 안 해도 OK
                return true;
            } else if (id == R.id.nav_favorite) {
                // 즐겨찾기 탭 누르면 이동
                Intent intent = new Intent(HomeActivity.this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(HomeActivity.this, InfoFormActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });

        // 플로팅 버튼 터치 리스너 - 개선된 버전
        fabAddIntro.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            float startX, startY;
            long startClickTime;
            final int CLICK_TIME_THRESHOLD = 200;
            final float CLICK_MOVE_THRESHOLD = 20;

            @Override
            public boolean onTouch(View view, android.view.MotionEvent event) {
                switch (event.getActionMasked()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        startX = event.getRawX();
                        startY = event.getRawY();
                        startClickTime = System.currentTimeMillis();
                        view.setAlpha(0.9f);
                        return false;

                    case android.view.MotionEvent.ACTION_MOVE:
                        view.setX(event.getRawX() + dX);
                        view.setY(event.getRawY() + dY);
                        float distanceMoved = distance(startX, startY, event.getRawX(), event.getRawY());
                        if (distanceMoved > CLICK_MOVE_THRESHOLD) {
                            return true;
                        }
                        return false;

                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL: // 취소 이벤트도 동일하게 처리
                        // 항상 알파값을 원래대로 복원
                        view.setAlpha(1.0f);

                        // ACTION_UP일 때만 클릭 처리
                        if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP) {
                            long clickDuration = System.currentTimeMillis() - startClickTime;
                            float distance = distance(startX, startY, event.getRawX(), event.getRawY());

                            if (clickDuration < CLICK_TIME_THRESHOLD && distance < CLICK_MOVE_THRESHOLD) {
                                view.performClick();
                                return false;
                            }
                        }

                        // 항상 true 반환하여 이벤트 소비
                        return true;

                    default:
                        // 알파값 복원 추가
                        view.setAlpha(1.0f);
                        return false;
                }
            }

            private float distance(float x1, float y1, float x2, float y2) {
                float dx = x2 - x1;
                float dy = y2 - y1;
                return (float) Math.sqrt(dx * dx + dy * dy);
            }
        });

        // RecyclerView 초기화 - 저장된 데이터 우선 로드
        introList = new ArrayList<>();

        // 1. 먼저 저장된 데이터가 있는지 확인
        List<String> savedNames = selfIntroStorage.getAllSelfIntroNames();
        SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet("FAVORITE_INTROS", new HashSet<>());

        if (!savedNames.isEmpty()) {
            // 저장된 데이터가 있으면 그것을 사용
            Log.d("HomeActivity", "저장된 자기소개서 목록 로드: " + savedNames.size() + "개");
            for (String title : savedNames) {
                boolean isFavorite = favorites.contains(title);
                introList.add(new SelfIntro(title, isFavorite));
            }
        } else {
            // 저장된 데이터가 없으면 더미 데이터 생성 및 저장
            Log.d("HomeActivity", "저장된 데이터가 없어 더미 데이터 생성");
            // 더미 데이터 생성 대신 안내 메시지 추가 (아래에 코드 추가)
//            showEmptyStateUI();
        }


        adapter = new SelfIntroAdapter(this, introList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 플로팅 버튼 이벤트리스너
        fabAddIntro.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, InputActivity.class);
            startActivity(intent);
        });

        // 최근 수정 항목 표시 메서드 호출 (기존 코드를 이 한 줄로 대체)
        updateRecentIntroDisplay();

        // 최근 항목 불러오기
        String recentIntroName = prefs.getString(LAST_EDITED_KEY, null);

        // 항상 recentContainer 보이도록 설정
        recentContainer.setVisibility(View.VISIBLE);

        if (recentIntroName != null && !recentIntroName.isEmpty()) {
            recentText.setText(recentIntroName);

            recentContainer.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, EditListActivityFromHome.class);
                intent.putExtra("resumeTitle", recentIntroName);
                startActivity(intent);

                // 로그 추가
                Log.d("HomeActivity", "최근 자기소개서 클릭: " + recentIntroName);
            });
        } else {
            recentText.setText("수정한 자기소개서가 없습니다");
            // 클릭 비활성화
            recentContainer.setOnClickListener(null);
        }

        // 어댑터에 이름 변경 리스너 설정
        adapter.setOnItemRenameListener(new SelfIntroAdapter.OnItemRenameListener() {
            @Override
            public void onItemRename(String oldName, String newName) {
                handleItemRename(oldName, newName);
            }
        });

        // 처음 진입시 안내 애니메이션 호출 추가
        fabAddIntro.postDelayed(() -> {
            showCreateIntroGuide();
        }, 1000);
    }

    // 3. 처음 진입시 안내 애니메이션 메서드 추가
    private void showCreateIntroGuide() {
        // SharedPreferences 체크를 제거하고 항상 가이드를 보여주도록 수정

        // 1. 반투명 텍스트 뷰 생성
        TextView guideText = new TextView(this);
        guideText.setText("자기소개서 생성");
        guideText.setTextColor(Color.WHITE);
        guideText.setBackgroundResource(R.drawable.rounded_toast_bg);
        guideText.setPadding(30, 10, 100, 10);
        guideText.setTextSize(16);

        // 2. 최상위 레이아웃(FrameLayout)에 추가
        FrameLayout rootLayout = (FrameLayout) findViewById(android.R.id.content);

        // 상대적 위치 파라미터 설정
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        rootLayout.addView(guideText, params);

        // 3. 플로팅 버튼 위치 기준으로 텍스트 위치 조정
        fabAddIntro.post(() -> {
            int[] location = new int[2];
            fabAddIntro.getLocationInWindow(location);

            guideText.setX(location[0] - guideText.getWidth() / 2 + fabAddIntro.getWidth() / 2);
            guideText.setY(location[1] - guideText.getHeight() - 20); // 버튼 위 20px

            // 4. 텍스트 페이드인 애니메이션
            guideText.setAlpha(0f);
            guideText.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start();

            // 5. 버튼 애니메이션
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(fabAddIntro, "scaleX", 1f, 1.2f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(fabAddIntro, "scaleY", 1f, 1.2f, 1f);

            scaleX.setDuration(500);
            scaleY.setDuration(500);
            scaleX.setRepeatCount(2); // 총 3회 반복
            scaleY.setRepeatCount(2);
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

            scaleX.start();
            scaleY.start();

            // 6. 3초 후 가이드 제거
            rootLayout.postDelayed(() -> {
                guideText.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction(() -> rootLayout.removeView(guideText))
                        .start();

                // SharedPreferences 저장 코드 제거
            }, 3000);
        });

        // 노란색 토스트도 함께 표시 (선택적)
        // showYellowToast("플로팅 버튼을 눌러 자기소개서를 추가해보세요!");
    }

    // 더미 데이터 생성 및 저장 메서드
    private void createAndSaveDummyData(Set<String> favorites) {
//        String[] dummyTitles = {
//                "테스트 자기소개서1",
//                "백엔드 개발자 지원서",
//                "프론트엔드 인턴 지원",
//                "백엔드 개발자 지원서2",
//                "프론트엔드 인턴 지원2",
//                "테스트 자기소개서2",
//                "백엔드 개발자 지원서3",
//                "프론트엔드 인턴 지원3"
//        };

//        boolean[] dummyFavorites = {true, false, false, false, false, true, false, false};

//        for (int i = 0; i < dummyTitles.length; i++) {
//            // SelfIntroData 생성 및 저장
//            SelfIntroData dummyData = new SelfIntroData(
//                    "직무역량 내용을 입력하세요.",
//                    "입사후포부 내용을 입력하세요.",
//                    "지원동기 내용을 입력하세요.",
//                    "성격장단점 내용을 입력하세요."
//            );
//
//            // 저장소에 저장
//            selfIntroStorage.saveSelfIntro(dummyTitles[i], dummyData);
//
//            // UI 리스트에 추가
//            introList.add(new SelfIntro(dummyTitles[i], dummyFavorites[i]));
//
//            // 즐겨찾기 항목이면 SharedPreferences에도 저장
//            if (dummyFavorites[i]) {
//                favorites.add(dummyTitles[i]);
//            }
//        }

//        // 즐겨찾기 목록 저장
//        SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
//        prefs.edit().putStringSet("FAVORITE_INTROS", favorites).apply();
        // 더미 데이터를 생성하지 않고 빈 구현으로 변경
        Log.d("HomeActivity", "더미 데이터 생성 비활성화됨");

        // 안내 메시지 표시
        showYellowToast("플로팅 버튼을 눌러 자기소개서를 추가해보세요!");
    }

    // 이름 변경 처리 함수
    private void handleItemRename(String oldName, String newName) {
        try {

            // 로그 추가
            Log.d("HomeActivity", "이름 변경: " + oldName + " → " + newName);

            // 1. 기존 자기소개서 데이터 로드
            SelfIntroData oldData = selfIntroStorage.loadSelfIntro(oldName);

            if (oldData == null) {
                Log.e("HomeActivity", "기존 데이터를 찾을 수 없음: " + oldName);
                showYellowToast("변경하려는 자기소개서를 찾을 수 없습니다.");
                Log.d("HomeActivity", "기존 데이터가 없어서 빈 데이터를 생성합니다: " + oldName);
                oldData = new SelfIntroData(
                        "직무역량 내용을 입력하세요.",
                        "입사후포부 내용을 입력하세요.",
                        "지원동기 내용을 입력하세요.",
                        "성격장단점 내용을 입력하세요."
                );
//                return;
            }

            // 2. 새 이름으로 데이터 저장
            selfIntroStorage.saveSelfIntro(newName, oldData);
            Log.d("HomeActivity", "새 이름으로 저장 완료: " + newName);

            // 3. 기존 이름의 데이터 삭제
            selfIntroStorage.deleteSelfIntro(oldName);
            Log.d("HomeActivity", "기존 데이터 삭제 완료: " + oldName);

            // 4. 리스트에서 이름 업데이트
            for (SelfIntro intro : introList) {
                if (intro.getTitle().equals(oldName)) {
                    intro.setTitle(newName);
                    break;
                }
            }

            // 5. 어댑터에 변경 알림
            adapter.notifyDataSetChanged();

            // 6. 즐겨찾기 목록에서도 이름 업데이트
            SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
            Set<String> favorites = prefs.getStringSet("FAVORITE_INTROS", new HashSet<>());
            if (favorites.contains(oldName)) {
                Set<String> updatedFavorites = new HashSet<>(favorites);
                updatedFavorites.remove(oldName);
                updatedFavorites.add(newName);
                prefs.edit().putStringSet("FAVORITE_INTROS", updatedFavorites).apply();
            }

            // 7. 최근 편집 항목도 업데이트
            String recentIntro = prefs.getString(LAST_EDITED_KEY, null);
            if (oldName.equals(recentIntro)) {
                prefs.edit().putString(LAST_EDITED_KEY, newName).apply();
                recentText.setText(newName);
            }

            // 8. 성공 토스트 표시
            showYellowToast("이름이 변경되었습니다: " + newName);

        } catch (Exception e) {
            Log.e("HomeActivity", "이름 변경 실패", e);
            e.printStackTrace();
            showYellowToast("이름 변경 중 오류가 발생했습니다.");
        }
    }

    // 노란색 토스트 메서드 추가
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

    // 저장소에서 자기소개서 삭제하는 메서드 (클래스 내부에 추가)
    public void deleteSelfIntroFromStorage(String title) {
        try {
            // 1. 저장소에서 삭제
            selfIntroStorage.deleteSelfIntro(title);

            // 2. 최근 편집 항목이 삭제된 자기소개서면 제거
            SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
            String recentIntro = prefs.getString(HomeActivity.LAST_EDITED_KEY, null);
            if (title.equals(recentIntro)) {
                prefs.edit().remove(HomeActivity.LAST_EDITED_KEY).apply();
                recentText.setText("수정한 자기소개서가 없습니다");
                recentContainer.setOnClickListener(null);
            }

            Log.d("HomeActivity", "저장소에서 자기소개서 삭제 완료: " + title);
            showYellowToast("자기소개서가 삭제되었습니다.");

        } catch (Exception e) {
            Log.e("HomeActivity", "저장소 삭제 실패", e);
            showYellowToast("삭제 중 오류가 발생했습니다.");
        }
    }

    // HomeActivity.java에 추가
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
            Intent intent = new Intent(HomeActivity.this, DownloadActivity.class);
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

