package com.example.aiselfintroduction;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

public class EditListActivity extends AppCompatActivity {

    private Button btnRole, btnGoal, btnReason, btnPersonality, btnEditAll, btnCheck;
    private ImageButton btnHome;
    private TextView resumeTitleText;
    private JsonObject aiJson;
    private String resumeTitle = "AI 자기소개서"; // 기본 제목

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editlist);

        // 버튼 및 뷰 연결
        btnRole = findViewById(R.id.btnRole);
        btnGoal = findViewById(R.id.btnGoal);
        btnReason = findViewById(R.id.btnReason);
        btnPersonality = findViewById(R.id.btnPersonality);
        btnEditAll = findViewById(R.id.btnEditAll);
        btnCheck = findViewById(R.id.btnCheck);
        btnHome = findViewById(R.id.btnHome);

        // 자기소개서 제목 TextView - 이 부분은 레이아웃에 추가해야 합니다
        resumeTitleText = findViewById(R.id.titleText);

        // Intent에서 resumeTitle 가져오기
        String intentTitle = getIntent().getStringExtra("resumeTitle");
        if (intentTitle != null && !intentTitle.isEmpty()) {
            resumeTitle = intentTitle;
        }

        // 제목 표시
        if (resumeTitleText != null) {
            resumeTitleText.setText(resumeTitle);
        } else {
            // TextView가 없는 경우 토스트로 표시 (디버깅용)
            Toast.makeText(this, "현재 자기소개서: " + resumeTitle, Toast.LENGTH_SHORT).show();
        }

        // 기본 비활성화 상태 설정 (회색)
        disableButton(btnRole);
        disableButton(btnGoal);
        disableButton(btnReason);
        disableButton(btnPersonality);

        // 홈 버튼
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(EditListActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // 확인 버튼
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
            Intent intent = new Intent(EditListActivity.this, DownloadActivity.class);
            intent.putExtra("resumeTitle", resumeTitle); // 현재 자기소개서 제목 전달
            intent.putExtra("aiJson", aiJson.toString()); // JSON 문자열 전달
            startActivity(intent);
        });

        // 자기소개서 데이터 로드
        loadSelfIntroData();

        // 예시 AI 응답 - 실제 앱에서는 AI 서버에서 전달된 JSON 사용
        aiJson = new JsonObject();
        aiJson.addProperty("직무역량", "저는 Node.js와 React를 기반으로 한 웹 개발 경험이 있습니다.");
        aiJson.addProperty("입사후포부", "귀사에서 프론트엔드 고도화 프로젝트에 기여하고 싶습니다.");
        aiJson.addProperty("지원동기", "기술력과 팀워크를 중시하는 귀사의 가치관에 공감합니다.");
        aiJson.addProperty("성격장단점", "성실하고 끈기가 있으며, 가끔 완벽주의적인 면이 있습니다.");

        // AI 응답이 존재할 경우 버튼 활성화
        updateButtonState(btnRole, "직무역량");
        updateButtonState(btnGoal, "입사후포부");
        updateButtonState(btnReason, "지원동기");
        updateButtonState(btnPersonality, "성격장단점");

        // 전체 편집 화면 이동
        btnEditAll.setOnClickListener(v -> {
            Intent intent = new Intent(EditListActivity.this, EditActivity.class);
            intent.putExtra("aiJson", aiJson.toString());
            intent.putExtra("resumeTitle", resumeTitle); // 현재 제목 전달
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 화면이 다시 보일 때마다 자기소개서 데이터 새로 로드
        loadSelfIntroData();
    }

    // 자기소개서 데이터 로드 메서드
    private void loadSelfIntroData() {
        // SelfIntroStorage에서 데이터 로드 (resumeTitle이 있는 경우)
        if (resumeTitle != null && !resumeTitle.equals("AI 자기소개서")) {
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
                } else {
                    // 저장된 데이터가 없으면 샘플 데이터 사용
                    createSampleData();
                    Toast.makeText(this, "자기소개서 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                createSampleData();
                Toast.makeText(this, "데이터 로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 기본 샘플 데이터 생성
            createSampleData();
        }

        // 버튼 상태 업데이트
        updateButtonState(btnRole, "직무역량");
        updateButtonState(btnGoal, "입사후포부");
        updateButtonState(btnReason, "지원동기");
        updateButtonState(btnPersonality, "성격장단점");
    }

    // 샘플 데이터 생성
    private void createSampleData() {
        aiJson = new JsonObject();
        aiJson.addProperty("직무역량", "저는 Node.js와 React를 기반으로 한 웹 개발 경험이 있습니다.");
        aiJson.addProperty("입사후포부", "귀사에서 프론트엔드 고도화 프로젝트에 기여하고 싶습니다.");
        aiJson.addProperty("지원동기", "기술력과 팀워크를 중시하는 귀사의 가치관에 공감합니다.");
        aiJson.addProperty("성격장단점", "성실하고 끈기가 있으며, 가끔 완벽주의적인 면이 있습니다.");
    }

    private void disableButton(Button btn) {
        btn.setEnabled(false);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
        btn.setTextColor(Color.parseColor("#888888"));
    }

    private void enableButton(Button btn) {
        btn.setEnabled(true);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFD54F")));
        btn.setTextColor(Color.BLACK);
    }

    private void updateButtonState(Button btn, String key) {
        if (aiJson != null && aiJson.has(key)) {
            String content = aiJson.get(key).getAsString();
            enableButton(btn);
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(EditListActivity.this, EditSingleActivity.class);
                intent.putExtra("fieldKey", key);
                intent.putExtra("fieldContent", content);
                intent.putExtra("resumeTitle", resumeTitle); // 자기소개서 제목도 함께 전달
                startActivity(intent);
            });
        } else {
            disableButton(btn);
        }
    }
}