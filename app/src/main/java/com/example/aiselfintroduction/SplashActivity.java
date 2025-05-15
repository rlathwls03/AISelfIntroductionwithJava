package com.example.aiselfintroduction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2초

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 상태바 숨기기 (선택사항)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 뒤로가기 버튼 비활성화 (새로운 방식)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로가기 동작을 막음 (아무것도 하지 않음)
            }
        });

        // 지연 후 메인 액티비티로 이동
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // 메인 액티비티로 이동
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 스플래시 액티비티 종료
            }
        }, SPLASH_DELAY);
    }
}