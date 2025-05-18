package com.example.aiselfintroduction;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditListActivityFromHome extends AppCompatActivity {

    private Button btnRole, btnGoal, btnReason, btnPersonality, btnEditAll, btnCheck;
    private ImageButton btnHome;
    private TextView resumeTitleText;
    private String resumeTitle = "AI 자기소개서"; // 기본 제목

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editlistfromhome);

        // 버튼 및 뷰 연결
        btnRole = findViewById(R.id.btnRole);
        btnGoal = findViewById(R.id.btnGoal);
        btnReason = findViewById(R.id.btnReason);
        btnPersonality = findViewById(R.id.btnPersonality);
        btnEditAll = findViewById(R.id.btnEditAll);
        btnCheck = findViewById(R.id.btnCheck);
        btnHome = findViewById(R.id.btnHome);

        resumeTitleText = findViewById(R.id.titleText);

        String intentTitle = getIntent().getStringExtra("resumeTitle");
        if (intentTitle != null && !intentTitle.isEmpty()) {
            resumeTitle = intentTitle;
        }

        if (resumeTitleText != null) {
            resumeTitleText.setText(resumeTitle);
        } else {
            Toast.makeText(this, "현재 자기소개서: " + resumeTitle, Toast.LENGTH_SHORT).show();
        }

        // 모든 버튼 항상 활성화
        enableButton(btnRole);
        enableButton(btnGoal);
        enableButton(btnReason);
        enableButton(btnPersonality);
        enableButton(btnEditAll);
        enableButton(btnCheck);

        // 홈 버튼
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(EditListActivityFromHome.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // 각 버튼 클릭 시 이동
        btnRole.setOnClickListener(v -> startEditSingle("직무역량"));
        btnGoal.setOnClickListener(v -> startEditSingle("입사후포부"));
        btnReason.setOnClickListener(v -> startEditSingle("지원동기"));
        btnPersonality.setOnClickListener(v -> startEditSingle("성격장단점"));

        btnEditAll.setOnClickListener(v -> {
            Intent intent = new Intent(EditListActivityFromHome.this, EditActivityFromHome.class);
            intent.putExtra("resumeTitle", resumeTitle);
            startActivity(intent);
        });

        btnCheck.setOnClickListener(v -> {
            Intent intent = new Intent(EditListActivityFromHome.this, DownloadActivity.class);
            intent.putExtra("resumeTitle", resumeTitle);
            startActivity(intent);
        });
    }

    private void startEditSingle(String fieldKey) {
        Intent intent = new Intent(EditListActivityFromHome.this, EditSingleActivity.class);
        intent.putExtra("fieldKey", fieldKey);
        intent.putExtra("resumeTitle", resumeTitle);
        startActivity(intent);
    }

    private void enableButton(Button btn) {
        btn.setEnabled(true);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFD54F")));
        btn.setTextColor(Color.BLACK);
    }
}
