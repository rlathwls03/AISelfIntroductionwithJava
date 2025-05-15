package com.example.aiselfintroduction;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private IntroPagerAdapter pagerAdapter;
    private EditText editTitle;
    private TextView btnSave;
    private TextView sectionTitle;
    private ImageView dot1, dot2, dot3, dot4;

    private List<IntroSection> sections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 키보드 감지를 위해 소프트 입력 모드 설정
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        viewPager = findViewById(R.id.viewPager);
        editTitle = findViewById(R.id.editTitle);
        btnSave = findViewById(R.id.btnSave);
        sectionTitle = findViewById(R.id.sectionTitle);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);

        // 제목 초기화
        String resumeTitle = getIntent().getStringExtra("resumeTitle");
        if (resumeTitle != null && !resumeTitle.isEmpty()) {
            editTitle.setText(resumeTitle);
        } else {
            editTitle.setText("AI 자기소개서");
        }

        // AI JSON 응답 처리
        String jsonString = getIntent().getStringExtra("aiJson");
        if (jsonString != null) {
            try {
                JsonObject json = new Gson().fromJson(jsonString, JsonObject.class);
                if (json.has("직무역량"))
                    sections.add(new IntroSection("직무 역량", json.get("직무역량").getAsString()));
                if (json.has("입사후포부"))
                    sections.add(new IntroSection("입사 후 포부", json.get("입사후포부").getAsString()));
                if (json.has("지원동기"))
                    sections.add(new IntroSection("지원 동기", json.get("지원동기").getAsString()));
                if (json.has("성격장단점"))
                    sections.add(new IntroSection("성격의 장단점", json.get("성격장단점").getAsString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (sections.isEmpty()) {
            sections.add(new IntroSection("직무 역량", "직무 역량 샘플 내용입니다."));
            sections.add(new IntroSection("입사 후 포부", "입사 후 포부 내용입니다."));
            sections.add(new IntroSection("지원 동기", "지원 동기 내용입니다."));
            sections.add(new IntroSection("성격의 장단점", "성격의 장단점 내용입니다."));
        }

        pagerAdapter = new IntroPagerAdapter(sections);
        viewPager.setAdapter(pagerAdapter);

        updateTitleAndDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTitleAndDots(position);
            }
        });

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "자기소개서 제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObject resultJson = new JsonObject();
            for (IntroSection section : sections) {
                String key = convertToJsonKey(section.getTitle());
                resultJson.addProperty(key, section.getContent());
            }

            Intent intent = new Intent(EditActivity.this, DownloadActivity.class);
            intent.putExtra("resumeTitle", title);
            intent.putExtra("aiJson", resultJson.toString());
            startActivity(intent);
        });

        // 키보드 올라올 때 ViewPager 안 가려지도록 자동 스크롤
        findViewById(R.id.viewPager).getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
            int screenHeight = getWindow().getDecorView().getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            if (keypadHeight > screenHeight * 0.15) {
                viewPager.post(() -> viewPager.setTranslationY(-keypadHeight / 3f));
            } else {
                viewPager.post(() -> viewPager.setTranslationY(0));
            }
        });
    }

    private String convertToJsonKey(String title) {
        return title.replaceAll("\\s", "");
    }

    private void updateTitleAndDots(int position) {
        if (sectionTitle != null && position < sections.size()) {
            sectionTitle.setText(sections.get(position).getTitle());
        }

        dot1.setImageResource(position == 0 ? R.drawable.dot_selected : R.drawable.dot_unselected);
        dot2.setImageResource(position == 1 ? R.drawable.dot_selected : R.drawable.dot_unselected);
        dot3.setImageResource(position == 2 ? R.drawable.dot_selected : R.drawable.dot_unselected);
        dot4.setImageResource(position == 3 ? R.drawable.dot_selected : R.drawable.dot_unselected);
    }
}