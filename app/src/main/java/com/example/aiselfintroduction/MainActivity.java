package com.example.aiselfintroduction;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // HomeActivity로 바로 이동
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);

        // MainActivity는 종료 (뒤로가기 시 안 돌아오게)
//        finish();
    }
}
