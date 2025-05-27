package com.quicinc.chatapp;

import static android.content.Context.MODE_PRIVATE;
import static com.quicinc.chatapp.HomeActivity.LAST_EDITED_KEY;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SelfIntroDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Intent로부터 intro 이름 받아오기
        String introName = getIntent().getStringExtra("introName");

        // 최근 수정한 자기소개서 이름 저장
        SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
        prefs.edit().putString(LAST_EDITED_KEY, introName).apply();
    }
}
