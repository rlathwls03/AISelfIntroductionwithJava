package com.example.aiselfintroduction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
//        getSupportActionBar().hide();

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

        // 플로팅 버튼
        fabAddIntro.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            int lastAction;

            @Override
            public boolean onTouch(View view, android.view.MotionEvent event) {
                switch (event.getActionMasked()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        lastAction = android.view.MotionEvent.ACTION_DOWN;
                        return true;

                    case android.view.MotionEvent.ACTION_MOVE:
                        view.setX(event.getRawX() + dX);
                        view.setY(event.getRawY() + dY);
                        lastAction = android.view.MotionEvent.ACTION_MOVE;
                        return true;

                    case android.view.MotionEvent.ACTION_UP:
                        if (lastAction == android.view.MotionEvent.ACTION_DOWN) {
                            // 클릭으로 간주
                            view.performClick();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });

        // RecyclerView 초기화
        introList = new ArrayList<>();
        introList.add(new SelfIntro("테스트 자기소개서1", true));
        introList.add(new SelfIntro("백엔드 개발자 지원서", false));
        introList.add(new SelfIntro("프론트엔드 인턴 지원", false));
        introList.add(new SelfIntro("백엔드 개발자 지원서2", false));
        introList.add(new SelfIntro("프론트엔드 인턴 지원2", false));
        introList.add(new SelfIntro("테스트 자기소개서2", true));
        introList.add(new SelfIntro("백엔드 개발자 지원서3", false));
        introList.add(new SelfIntro("프론트엔드 인턴 지원3", false));

        adapter = new SelfIntroAdapter(this, introList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 플로팅 버튼 이벤트리스너
        fabAddIntro.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, InputActivity.class);
            startActivity(intent);
        });

        // 즐겨찾기 항목
        SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet("FAVORITE_INTROS", new HashSet<>());
        introList.add(new SelfIntro("테스트 자기소개서", favorites.contains("테스트 자기소개서")));

        // 최근 항목 불러오기
        String recentIntroName = prefs.getString(LAST_EDITED_KEY, null);

        // 항상 recentContainer 보이도록 설정
        recentContainer.setVisibility(View.VISIBLE);

        if (recentIntroName != null && !recentIntroName.isEmpty()) {
            recentText.setText(recentIntroName);
            recentContainer.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SelfIntroDetailActivity.class);
                intent.putExtra("introName", recentIntroName);
                startActivity(intent);
            });
        } else {
            recentText.setText("수정한 자기소개서가 없습니다");
            // 클릭 비활성화
            recentContainer.setOnClickListener(null);
        }
    }
}

