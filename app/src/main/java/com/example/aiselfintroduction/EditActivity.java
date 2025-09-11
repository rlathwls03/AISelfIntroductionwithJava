package com.example.aiselfintroduction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {
    private static final String TAG = "EditActivity";
    private ViewPager2 viewPager;
    private IntroPagerAdapter pagerAdapter;
    private EditText editTitle;
    private TextView btnSave;
    private TextView sectionTitle;
    private ImageView dot1, dot2, dot3, dot4;
    private ImageButton btnBack;

    private List<IntroSection> sections = new ArrayList<>();
    private SelfIntroStorage selfIntroStorage;
    private boolean fromXmlFiles = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 키보드 감지를 위해 소프트 입력 모드 설정
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        selfIntroStorage = new SelfIntroStorage(this);

        viewPager = findViewById(R.id.viewPager);
        editTitle = findViewById(R.id.editTitle);
        btnSave = findViewById(R.id.btnSave);
        sectionTitle = findViewById(R.id.sectionTitle);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);
        btnBack = findViewById(R.id.btnBack);

        fromXmlFiles = getIntent().getBooleanExtra("fromXmlFiles", false);
        Log.d(TAG, "XML 파일에서 로드: " + fromXmlFiles);

        String resumeTitle = getIntent().getStringExtra("resumeTitle");
        if (resumeTitle != null && !resumeTitle.isEmpty()) {
            editTitle.setText(resumeTitle);
        } else {
            editTitle.setText("AI 자기소개서");
        }

        loadSectionsFromJson();

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

        btnBack.setOnClickListener(v -> {
            String currentTitle = editTitle.getText().toString().trim();
            String originalTitle = getIntent().getStringExtra("resumeTitle");
            String titleToPass = currentTitle.isEmpty() ?
                    (originalTitle != null ? originalTitle : "AI 자기소개서") :
                    currentTitle;

            Log.d(TAG, "뒤로가기: 제목 전달 - " + titleToPass);

            Intent intent = new Intent(EditActivity.this, EditListActivity.class);
            intent.putExtra("resumeTitle", titleToPass);
            startActivity(intent);
            finish();
        });

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "자기소개서 제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String originalTitle = getIntent().getStringExtra("resumeTitle");
            boolean isTitleChanged = originalTitle != null && !originalTitle.isEmpty() && !originalTitle.equals(title);

            // 🔥 모든 section을 반드시 {"response":"..."} 형태로 저장
            JsonObject resultJson = new JsonObject();
            for (IntroSection section : sections) {
                String key = convertToJsonKey(section.getTitle());
                String content = section.getContent();

                String toSave;
                try {
                    JsonObject tmp = new Gson().fromJson(content, JsonObject.class);
                    // 이미 객체(랩핑됨)
                    if (tmp.has("response")) {
                        toSave = content;
                    } else {
                        JsonObject wrapper = new JsonObject();
                        wrapper.addProperty("response", content);
                        toSave = wrapper.toString();
                    }
                } catch (Exception e) {
                    // 파싱 실패(plain text) → 무조건 래핑
                    JsonObject wrapper = new JsonObject();
                    wrapper.addProperty("response", content);
                    toSave = wrapper.toString();
                }

                resultJson.addProperty(key, toSave);
            }

            // SelfIntroData 객체로 변환
            SelfIntroData introData = new SelfIntroData(
                    getContentForKey(resultJson, "직무역량"),
                    getContentForKey(resultJson, "입사후포부"),
                    getContentForKey(resultJson, "지원동기"),
                    getContentForKey(resultJson, "성격장단점")
            );

            if (isTitleChanged) {
                Log.d("EditActivity", "자기소개서 이름 변경: " + originalTitle + " → " + title);
                boolean success = selfIntroStorage.renameSelfIntro(originalTitle, introData, title);

                if (!success) {
                    selfIntroStorage.saveSelfIntro(title, introData);
                    try {
                        selfIntroStorage.deleteSelfIntro(originalTitle);
                    } catch (Exception e) {
                        Log.e("EditActivity", "기존 자기소개서 삭제 실패: " + originalTitle, e);
                    }
                }
            } else {
                selfIntroStorage.saveSelfIntro(title, introData);
            }

            SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
            prefs.edit().putString(HomeActivity.LAST_EDITED_KEY, title).apply();

            boolean filesDeleted = AIResponseFileManager.deleteAllResponseFiles(this);
            Log.d(TAG, "XML 파일 삭제 결과: " + filesDeleted);

            showSafeToast("저장되었습니다.");

            Intent intent = new Intent(EditActivity.this, EditListActivityFromHome.class);
            intent.putExtra("resumeTitle", title);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
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

    private void loadSectionsFromJson() {
        sections.clear();

        // XML 파일 우선 로드
        String role = AIResponseFileManager.extractTextFromXml(AIResponseFileManager.readSectionFromFile(this, "직무역량"));
        String goal = AIResponseFileManager.extractTextFromXml(AIResponseFileManager.readSectionFromFile(this, "입사후포부"));
        String reason = AIResponseFileManager.extractTextFromXml(AIResponseFileManager.readSectionFromFile(this, "지원동기"));
        String personality = AIResponseFileManager.extractTextFromXml(AIResponseFileManager.readSectionFromFile(this, "성격장단점"));

        boolean hasAll = !role.isEmpty() || !goal.isEmpty() || !reason.isEmpty() || !personality.isEmpty();
        if (hasAll) {
            sections.add(new IntroSection("직무 역량", role));
            sections.add(new IntroSection("입사 후 포부", goal));
            sections.add(new IntroSection("지원 동기", reason));
            sections.add(new IntroSection("성격의 장단점", personality));
            return;
        }

        // XML이 없을 경우 aiJson 사용
        String jsonString = getIntent().getStringExtra("aiJson");
        if (jsonString != null) {
            try {
                JsonObject json = new Gson().fromJson(jsonString, JsonObject.class);
                String[] keys = {"직무역량", "입사후포부", "지원동기", "성격장단점"};
                String[] sectionTitles = {"직무 역량", "입사 후 포부", "지원 동기", "성격의 장단점"};
                for (int i = 0; i < keys.length; i++) {
                    if (json.has(keys[i])) {
                        String val = json.get(keys[i]).getAsString();
                        // 래핑 여부 판단해서 내용만 추출
                        String toShow;
                        try {
                            JsonObject obj = new Gson().fromJson(val, JsonObject.class);
                            if (obj.has("response")) {
                                toShow = obj.get("response").getAsString();
                            } else {
                                toShow = val;
                            }
                        } catch (Exception e) {
                            toShow = val;
                        }
                        sections.add(new IntroSection(sectionTitles[i], toShow));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "JSON 파싱 오류", e);
            }
        }

        // 둘 다 없을 경우 기본값 설정
        if (sections.isEmpty()) {
            sections.add(new IntroSection("직무 역량", "직무 역량 샘플 내용입니다."));
            sections.add(new IntroSection("입사 후 포부", "입사 후 포부 내용입니다."));
            sections.add(new IntroSection("지원 동기", "지원 동기 내용입니다."));
            sections.add(new IntroSection("성격의 장단점", "성격의 장단점 내용입니다."));
        }
    }

    // JSON 키에서 내용 꺼내기 (없으면 빈 문자열)
    private String getContentForKey(JsonObject json, String key) {
        if (!json.has(key)) return "";
        String val = json.get(key).getAsString();
        try {
            JsonObject obj = new Gson().fromJson(val, JsonObject.class);
            if (obj.has("response")) return obj.get("response").getAsString();
            else return val;
        } catch (Exception e) {
            return val;
        }
    }

    private String convertToJsonKey(String title) {
        if (title.equals("성격의 장단점")) {
            return "성격장단점";
        }
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

    private void showYellowToast(String message) {
        try {
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(40, 20, 40, 20);

            GradientDrawable shape = new GradientDrawable();
            shape.setColor(Color.parseColor("#FCD965"));
            shape.setCornerRadius(30);
            layout.setBackground(shape);

            TextView textView = new TextView(this);
            textView.setText(message);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(16);
            textView.setGravity(Gravity.CENTER);
            layout.addView(textView);

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

    private void showSafeToast(String message) {
        showYellowToast(message);
    }
}
