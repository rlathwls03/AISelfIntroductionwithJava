package com.quicinc.chatapp;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class EditListActivityFromHome extends AppCompatActivity {
    private final Map<String,String> sectionMap = new LinkedHashMap<>();
    private Button btnRole, btnGoal, btnReason, btnPersonality, btnEditAll, btnCheck;
    private ImageButton btnHome;
    private TextView resumeTitleText;

    public JsonObject aiJson;
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
            Log.d("AI_저장완료", "<><><>지정된 이름 : " + resumeTitle);
        } else {
            Toast.makeText(this, "현재 자기소개서: " + resumeTitle, Toast.LENGTH_SHORT).show();
        }

        // 모든 버튼 항상 활성화
        enableButton(btnRole);
        enableButton(btnGoal);
        enableButton(btnReason);
        enableButton(btnPersonality);
        //enableButton(btnEditAll);
        btnEditAll.setEnabled(true);
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
            // 디버그 로그 추가
            android.util.Log.d("EditListActivity", "확인 버튼 클릭: 제목 - " + resumeTitle);

            if (aiJson != null) {
                android.util.Log.d("EditListActivity", "전달할 JSON: " + aiJson.toString());
            } else {
                android.util.Log.e("EditListActivity", "aiJson이 null입니다!");

                // aiJson이 null인 경우 데이터를 다시 로드해본다
                loadSelfIntroData();

                if (aiJson == null) {
                    Toast.makeText(this, "자기소개서 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // DownloadActivity로 이동
            Intent intent = new Intent(EditListActivityFromHome.this, DownloadActivity.class);
            intent.putExtra("resumeTitle", resumeTitle); // 현재 자기소개서 제목 전달
            intent.putExtra("aiJson", aiJson.toString()); // JSON 문자열 전달
            startActivity(intent);
        });
    }

    private void loadSelfIntroData() {
        // SelfIntroStorage에서 데이터 로드 (resumeTitle이 있는 경우)
        if (resumeTitle != null) {
            try {
                SelfIntroStorage storage = new SelfIntroStorage(this);
                SelfIntroData introData = storage.loadSelfIntro(resumeTitle);

                if (introData != null) {
                    // 로드된 데이터를 JsonObject로 변환
                    aiJson = new JsonObject();
                    aiJson.addProperty("직무역량", introData.get직무역량());
                    aiJson.addProperty("입사후포부", introData.get입사후포부());
                    aiJson.addProperty("지원동기", introData.get지원동기());
                    aiJson.addProperty("성격장단점", introData.get성격장단점());

                    // 버튼 상태 업데이트
                    updateButtonState(btnRole, "직무역량");
                    updateButtonState(btnGoal, "입사후포부");
                    updateButtonState(btnReason, "지원동기");
                    updateButtonState(btnPersonality, "성격장단점");
                } else {
                    // XML 파일 확인
                    updateButtonsFromXmlFiles();
//                    Toast.makeText(this, "자기소개서 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
//                createSampleData();
                Toast.makeText(this, "데이터 로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 기본 샘플 데이터 생성
//            createSampleData();
            // XML 파일 기반으로 버튼 상태 업데이트
            updateButtonsFromXmlFiles();
        }

//        // 버튼 상태 업데이트
//        updateButtonState(btnRole, "직무역량");
//        updateButtonState(btnGoal, "입사후포부");
//        updateButtonState(btnReason, "지원동기");
//        updateButtonState(btnPersonality, "성격장단점");
    }
    private void updateButtonsFromXmlFiles() {
        // 각 섹션별 XML 파일 확인 및 버튼 업데이트
        updateButtonFromXmlFile(btnRole, "직무역량");
        updateButtonFromXmlFile(btnGoal, "입사후포부");
        updateButtonFromXmlFile(btnReason, "지원동기");
        updateButtonFromXmlFile(btnPersonality, "성격장단점");

        // 편집 버튼 항상 활성화
        btnEditAll.setEnabled(true);
        btnEditAll.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFD54F")));
        btnEditAll.setTextColor(Color.BLACK);

    }

    private void updateButtonFromXmlFile(Button btn, String section) {
        if (AIResponseFileManager.sectionFileExists(this, section)) {
            // XML 파일 존재 시 버튼 활성화
            enableButton(btn);

            // ① SharedPreferences(= SelfIntroPrefs.xml) 열기
            SharedPreferences prefs = getSharedPreferences("SelfIntroPrefs", MODE_PRIVATE);
            //    (파일명이 SelfIntroPrefs.xml 이므로, 첫 인자를 "SelfIntroPrefs" 로 사용)
            String outerJsonStr = prefs.getString(resumeTitle, null);
            //    ("1번" 은 <string name="1번"> ... </string> 의 키)

            // ② 꺼낸 문자열이 있으면 외부 JsonObject 로 파싱
            if (outerJsonStr != null) {
                JsonObject outer = new Gson().fromJson(outerJsonStr, JsonObject.class);

                // ③ 처리하고 싶은 섹션 키 목록
                String[] sections = {"직무역량","입사후포부","지원동기","성격장단점"};

                for (String key : sections) {
                    if (outer.has(key)) {
                        // ④ 외부 Json 에서 해당 섹션용 inner-JSON 문자열 추출
                        String innerJsonStr = outer.get(key).getAsString();

                        // ⑤ inner-JSON 파싱 → {"response":"..."} 객체
                        JsonObject inner = new Gson().fromJson(innerJsonStr, JsonObject.class);

                        // ⑥ 실제 텍스트 꺼내기
                        String text = inner.has("response")
                                ? inner.get("response").getAsString()
                                : "";

                        // ⑦ map 또는 리스트에 담기
                        sectionMap.put(key, text);
                    }
                }
            }

            // 클릭 리스너 설정
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(EditListActivityFromHome.this, EditSingleActivity.class);
                intent.putExtra("fieldKey", section);
                String content = sectionMap.getOrDefault(section, "");
                intent.putExtra("fieldContent", content);
                intent.putExtra("resumeTitle", resumeTitle);
                startActivity(intent);
            });
        } else {
            disableButton(btn);
        }
    }


    private void disableButton(Button btn) {
        btn.setEnabled(false);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
        btn.setTextColor(Color.parseColor("#888888"));
    }

    private void updateButtonState(Button btn, String key) {
        if (aiJson != null && aiJson.has(key)) {
            String content = aiJson.get(key).getAsString();
            enableButton(btn);
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(EditListActivityFromHome.this, EditSingleActivity.class);
                intent.putExtra("fieldKey", key);
                intent.putExtra("fieldContent", content);
                intent.putExtra("resumeTitle", resumeTitle); // 자기소개서 제목도 함께 전달
                startActivity(intent);
            });
        } else {
            disableButton(btn);
        }
    }

    private void startEditSingle(String fieldKey) {
        Intent intent = new Intent(EditListActivityFromHome.this, EditSingleActivityFromHome.class);
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