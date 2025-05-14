package com.example.aiselfintroduction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 홈 탭으로 왔을 때 상태 수동 갱신
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private TextView userName, userPhone, userEmail, recentText;
    private ListView selfIntroListView;
    private FloatingActionButton fabAddIntro;
    private LinearLayout recentContainer;

    private ArrayList<String> introTitles;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);
        userEmail = findViewById(R.id.userEmail);
        recentText = findViewById(R.id.recentText);
        recentContainer = findViewById(R.id.recentContainer);
        fabAddIntro = findViewById(R.id.fab);
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

        // 예시 데이터
        introTitles = new ArrayList<>();
        introTitles.add("백엔드 개발자 지원서");
        introTitles.add("프론트엔드 인턴 지원");
        introTitles.add("백엔드 개발자 지원서");
        introTitles.add("프론트엔드 인턴 지원");
        introTitles.add("백엔드 개발자 지원서");
        introTitles.add("프론트엔드 인턴 지원");

        // ✅ ListView 초기화 및 연결
        selfIntroListView = findViewById(R.id.selfIntroListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, introTitles);
        selfIntroListView.setAdapter(adapter);

        RecyclerView recyclerView;

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, introTitles);


        fabAddIntro.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, InfoFormActivity.class);
            startActivity(intent);
        });

        recentContainer.setVisibility(View.VISIBLE);
        recentText.setText("프론트엔드 인턴 지원");
        recentContainer.setOnClickListener(v -> {
            // 최근 자기소개서 보기
        });
    }
}

