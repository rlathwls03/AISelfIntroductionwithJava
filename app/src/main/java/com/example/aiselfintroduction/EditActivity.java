package com.example.aiselfintroduction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewTreeObserver;
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
    private SelfIntroStorage selfIntroStorage; // 자기소개서 저장소 추가
    private boolean fromXmlFiles = false; // XML 파일에서 로드했는지 여부

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 키보드 감지를 위해 소프트 입력 모드 설정
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // 자기소개서 저장소 초기화
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

        // XML 파일에서 로드했는지 확인
        fromXmlFiles = getIntent().getBooleanExtra("fromXmlFiles", false);
        Log.d(TAG, "XML 파일에서 로드: " + fromXmlFiles);

        // 제목 초기화
        String resumeTitle = getIntent().getStringExtra("resumeTitle");
        if (resumeTitle != null && !resumeTitle.isEmpty()) {
            editTitle.setText(resumeTitle);
        } else {
            editTitle.setText("AI 자기소개서");
        }

        // JSON 데이터 로드 및 섹션 데이터 구성
        loadSectionsFromJson();


//        // AI JSON 응답 처리
//        String jsonString = getIntent().getStringExtra("aiJson");
//        if (jsonString != null) {
//            try {
//                JsonObject json = new Gson().fromJson(jsonString, JsonObject.class);
//                if (json.has("직무역량"))
//                    sections.add(new IntroSection("직무 역량", json.get("직무역량").getAsString()));
//                if (json.has("입사후포부"))
//                    sections.add(new IntroSection("입사 후 포부", json.get("입사후포부").getAsString()));
//                if (json.has("지원동기"))
//                    sections.add(new IntroSection("지원 동기", json.get("지원동기").getAsString()));
//                if (json.has("성격장단점"))
//                    sections.add(new IntroSection("성격의 장단점", json.get("성격장단점").getAsString()));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

//        if (sections.isEmpty()) {
//            sections.add(new IntroSection("직무 역량", "직무 역량 샘플 내용입니다."));
//            sections.add(new IntroSection("입사 후 포부", "입사 후 포부 내용입니다."));
//            sections.add(new IntroSection("지원 동기", "지원 동기 내용입니다."));
//            sections.add(new IntroSection("성격의 장단점", "성격의 장단점 내용입니다."));
//        }

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

        // 뒤로가기 버튼 클릭 리스너 추가
        btnBack.setOnClickListener(v -> {
            // 현재 입력된 제목 가져오기
            String currentTitle = editTitle.getText().toString().trim();

            // 원래 제목 가져오기
            String originalTitle = getIntent().getStringExtra("resumeTitle");

            // 전달할 제목 결정 (입력된 제목이 있으면 사용, 없으면 원래 제목 사용)
            String titleToPass = currentTitle.isEmpty() ?
                    (originalTitle != null ? originalTitle : "AI 자기소개서") :
                    currentTitle;

            Log.d(TAG, "뒤로가기: 제목 전달 - " + titleToPass);

            // EditListActivity로 이동하면서 제목 전달
            Intent intent = new Intent(EditActivity.this, EditListActivity.class);
            intent.putExtra("resumeTitle", titleToPass);
            startActivity(intent);
            finish();
        });

        // 저장 버튼 클릭 리스너
        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "자기소개서 제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 제목이 변경되었는지 확인
            String originalTitle = getIntent().getStringExtra("resumeTitle");
            boolean isTitleChanged = originalTitle != null && !originalTitle.isEmpty() && !originalTitle.equals(title);

            // 자기소개서 내용을 JSON으로 변환
            JsonObject resultJson = new JsonObject();
            for (IntroSection section : sections) {
                String key = convertToJsonKey(section.getTitle());
                resultJson.addProperty(key, section.getContent());
            }

            // SelfIntroData 객체로 변환
            SelfIntroData introData = new SelfIntroData(
                    getContentForKey(resultJson, "직무역량"),
                    getContentForKey(resultJson, "입사후포부"),
                    getContentForKey(resultJson, "지원동기"),
                    getContentForKey(resultJson, "성격장단점")
            );

            // 저장 로직 처리
            if (isTitleChanged) {
                // 이름이 변경된 경우 renameSelfIntro 메서드 사용
                Log.d("EditActivity", "자기소개서 이름 변경: " + originalTitle + " → " + title);
                boolean success = selfIntroStorage.renameSelfIntro(originalTitle, introData, title);

                if (!success) {
                    // 이름 변경 실패 시 단순 저장
                    selfIntroStorage.saveSelfIntro(title, introData);

                    // 기존 데이터 삭제 시도
                    try {
                        selfIntroStorage.deleteSelfIntro(originalTitle);
                    } catch (Exception e) {
                        Log.e("EditActivity", "기존 자기소개서 삭제 실패: " + originalTitle, e);
                    }
                }
            } else {
                // 이름 변경이 없는 경우 일반 저장
                selfIntroStorage.saveSelfIntro(title, introData);
            }

            // 최근 편집 자기소개서 기록
            SharedPreferences prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);
            prefs.edit().putString(HomeActivity.LAST_EDITED_KEY, title).apply();

            // XML 파일에서 로드한 경우, 저장 후 XML 파일 삭제
            // 확인용 로그 추가
            Log.d(TAG, "fromXmlFiles: " + fromXmlFiles);

            // 파일 존재 확인
            boolean roleExists = AIResponseFileManager.sectionFileExists(this, "직무역량");
            boolean goalExists = AIResponseFileManager.sectionFileExists(this, "입사후포부");
            boolean reasonExists = AIResponseFileManager.sectionFileExists(this, "지원동기");
            boolean personalityExists = AIResponseFileManager.sectionFileExists(this, "성격장단점");

            Log.d(TAG, "저장 전 XML 파일 존재 여부: 직무역량=" + roleExists +
                    ", 입사후포부=" + goalExists + ", 지원동기=" + reasonExists +
                    ", 성격장단점=" + personalityExists);

            // XML 파일 삭제 시도 (fromXmlFiles 플래그와 관계없이 항상 시도)
            // 이렇게 하면 임시 파일은 항상 삭제됨
            boolean filesDeleted = AIResponseFileManager.deleteAllResponseFiles(this);
            Log.d(TAG, "XML 파일 삭제 결과: " + filesDeleted);

            // 삭제 후 파일 존재 확인
            roleExists = AIResponseFileManager.sectionFileExists(this, "직무역량");
            goalExists = AIResponseFileManager.sectionFileExists(this, "입사후포부");
            reasonExists = AIResponseFileManager.sectionFileExists(this, "지원동기");
            personalityExists = AIResponseFileManager.sectionFileExists(this, "성격장단점");

            Log.d(TAG, "삭제 후 XML 파일 존재 여부: 직무역량=" + roleExists +
                    ", 입사후포부=" + goalExists + ", 지원동기=" + reasonExists +
                    ", 성격장단점=" + personalityExists);

            // 저장 완료 토스트 메시지
            showSafeToast("저장되었습니다.");

            // EditListActivity로 돌아가기
            Intent intent = new Intent(EditActivity.this, EditListActivity.class);
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

    /**
     * JSON 데이터에서 섹션 데이터 로드
     */
    private void loadSectionsFromJson() {
        // 기존 섹션 초기화
        sections.clear();

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
                Log.e(TAG, "JSON 파싱 오류", e);
            }
        }

        // 섹션이 비어있으면 기본값으로 채움
        if (sections.isEmpty()) {
            sections.add(new IntroSection("직무 역량", "직무 역량 샘플 내용입니다."));
            sections.add(new IntroSection("입사 후 포부", "입사 후 포부 내용입니다."));
            sections.add(new IntroSection("지원 동기", "지원 동기 내용입니다."));
            sections.add(new IntroSection("성격의 장단점", "성격의 장단점 내용입니다."));
        }
    }

    // JSON 키에서 내용 가져오기 (없으면 빈 문자열)
    private String getContentForKey(JsonObject json, String key) {
        return json.has(key) ? json.get(key).getAsString() : "";
    }

    private String convertToJsonKey(String title) {
        // 디버그 로그 추가
        Log.d("EditActivity", "변환 전 키: " + title);

        // 특수 케이스 처리: "성격의 장단점"은 "성격장단점"으로 변환
        if (title.equals("성격의 장단점")) {
            Log.d("EditActivity", "성격의 장단점을 성격장단점으로 변환");
            return "성격장단점";
        }

        // 일반적인 경우: 공백 제거
        String result = title.replaceAll("\\s", "");
        Log.d("EditActivity", "변환 후 키: " + result);
        return result;
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

    // 노란색 커스텀 Toast를 위한 안전한 메서드
    private void showYellowToast(String message) {
        try {
            // 커스텀 레이아웃 생성
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(40, 20, 40, 20);

            // 노란색 배경 설정
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(Color.parseColor("#FCD965")); // 시그니처 노란색
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
            // 실패 시 기본 토스트
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    // 기존 showSafeToast를 showYellowToast로 변경
    private void showSafeToast(String message) {
        showYellowToast(message);
    }
}