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

        // 전체 편집 화면 이동
        btnEditAll.setOnClickListener(v -> {
            // 1. JSON 먼저 시도
            loadSelfIntroData();  // JSON → aiJson

            // 2. JSON이 비어 있거나 일부 항목이 누락되었으면 XML 보완
            if (aiJson == null || aiJson.entrySet().isEmpty()) {
                loadDataFromXmlFiles(); // 전체를 XML로 대체
            } else {
                // 개별 필드 보완
                String[] fields = {"직무역량", "입사후포부", "지원동기", "성격장단점"};
                for (String field : fields) {
                    if (!aiJson.has(field) || aiJson.get(field).getAsString().isEmpty()) {
                        if (AIResponseFileManager.sectionFileExists(this, field)) {
                            String xmlContent = AIResponseFileManager.readResponseFile(this, field);
                            String fallbackText = AIResponseFileManager.extractTextFromXml(xmlContent);
                            aiJson.addProperty(field, fallbackText);
                        }
                    }
                }
            }

            if (aiJson == null || aiJson.entrySet().isEmpty()) {
                Toast.makeText(this, "자기소개서 내용을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(EditListActivity.this, EditActivity.class);
            intent.putExtra("aiJson", aiJson.toString());
            intent.putExtra("resumeTitle", resumeTitle); // 현재 제목 전달
            intent.putExtra("fromXmlFiles", true); // XML 파일에서 로드했음을 표시
            startActivity(intent);
        });

        // 테스트용: 샘플 XML 파일 생성 (실제 앱에서는 AI 팀에서 생성)
        // 주석 해제하여 테스트
        // AIResponseFileManager.createSampleFiles(this);


        // XML 파일 기반으로 버튼 상태 업데이트
        updateButtonsFromXmlFiles();

        // 자기소개서 데이터 로드
//        loadSelfIntroData();


//        // 예시 AI 응답 - 실제 앱에서는 AI 서버에서 전달된 JSON 사용
//        aiJson = new JsonObject();
//        aiJson.addProperty("직무역량", "저는 Node.js와 React를 기반으로 한 웹 개발 경험이 있습니다.");
//        aiJson.addProperty("입사후포부", "귀사에서 프론트엔드 고도화 프로젝트에 기여하고 싶습니다.");
//        aiJson.addProperty("지원동기", "기술력과 팀워크를 중시하는 귀사의 가치관에 공감합니다.");
//        aiJson.addProperty("성격장단점", "성실하고 끈기가 있으며, 가끔 완벽주의적인 면이 있습니다.");
//
//        // AI 응답이 존재할 경우 버튼 활성화
//        updateButtonState(btnRole, "직무역량");
//        updateButtonState(btnGoal, "입사후포부");
//        updateButtonState(btnReason, "지원동기");
//        updateButtonState(btnPersonality, "성격장단점");


    }

    @Override
    protected void onResume() {
        super.onResume();

        // XML 파일 디버깅 (필요시에만 사용)
        debugXmlFiles();

        // XML 파일 체크 후 버튼 상태 업데이트
        updateButtonsFromXmlFiles();

        // 저장된 자기소개서가 있으면 로드
        if (resumeTitle != null && !resumeTitle.equals("AI 자기소개서")) {
            loadSelfIntroData();
        }
    }

    /**
     * XML 파일에서 데이터를 로드하여 aiJson 업데이트 (개선된 버전)
     */
    private void loadDataFromXmlFiles() {
        Log.d(TAG, "XML 파일에서 데이터 로드 시작");

        // 새 JsonObject 생성
        aiJson = new JsonObject();

        // 각 섹션별 XML 파일 로드
        String[] sections = {"직무역량", "입사후포부", "지원동기", "성격장단점"};

        for (String section : sections) {
            if (AIResponseFileManager.sectionFileExists(this, section)) {
                // XML 파일 읽기
                String xmlContent = AIResponseFileManager.readResponseFile(this, section);
                Log.d(TAG, section + " XML 파일 읽기 성공, 길이: " + xmlContent.length());

                // XML에서 텍스트 추출
                String content = AIResponseFileManager.extractTextFromXml(xmlContent);
                Log.d(TAG, section + " 추출된 텍스트 길이: " + content.length());

                if (content.isEmpty()) {
                    Log.e(TAG, section + " 텍스트 추출 실패");
                    // 실패 시 더미 데이터 사용
                    content = "AI가 생성한 " + section + " 내용을 불러오지 못했습니다.";
                }

                // JSON에 추가
                aiJson.addProperty(section, content);
                Log.d(TAG, section + " JSON에 추가 완료");
            } else {
                Log.d(TAG, section + " XML 파일 없음");
            }
        }

        // aiJson 확인
        Log.d(TAG, "최종 JSON: " + aiJson.toString());
    }

    /**
     * XML 파일 존재 여부에 따라 버튼 상태 업데이트
     */
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

    /**
     * 특정 섹션의 XML 파일 존재 여부에 따라 버튼 상태 업데이트
     */
    private void updateButtonFromXmlFile(Button btn, String section) {
        if (AIResponseFileManager.sectionFileExists(this, section)) {
            // XML 파일 존재 시 버튼 활성화
            enableButton(btn);

            // XML 내용 읽기
            String xmlContent = AIResponseFileManager.readResponseFile(this, section);
            String content = AIResponseFileManager.extractTextFromXml(xmlContent);

            // 클릭 리스너 설정
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(EditListActivity.this, EditSingleActivity.class);
                intent.putExtra("fieldKey", section);
                intent.putExtra("fieldContent", content);
                intent.putExtra("resumeTitle", resumeTitle);
                startActivity(intent);
            });
        } else {
            disableButton(btn);
        }
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

    // 샘플 데이터 생성
//    private void createSampleData() {
//        aiJson = new JsonObject();
//        aiJson.addProperty("직무역량", "저는 Node.js와 React를 기반으로 한 웹 개발 경험이 있습니다.");
//        aiJson.addProperty("입사후포부", "귀사에서 프론트엔드 고도화 프로젝트에 기여하고 싶습니다.");
//        aiJson.addProperty("지원동기", "기술력과 팀워크를 중시하는 귀사의 가치관에 공감합니다.");
//        aiJson.addProperty("성격장단점", "성실하고 끈기가 있으며, 가끔 완벽주의적인 면이 있습니다.");
//    }

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

    /**
     * XML 파일 디버깅을 위한 메서드
     */
    private void debugXmlFiles() {
        Log.d(TAG, "=== XML 파일 디버그 시작 ===");

        // 파일 목록 조회
        String[] fileList = fileList();
        Log.d(TAG, "총 " + fileList.length + "개 파일 존재:");
        for (String fileName : fileList) {
            Log.d(TAG, "- " + fileName);
        }

        // 각 XML 파일 확인
        Log.d(TAG, "각 섹션 파일 존재 여부:");
        Log.d(TAG, "- 직무역량: " + AIResponseFileManager.sectionFileExists(this, "직무역량"));
        Log.d(TAG, "- 입사후포부: " + AIResponseFileManager.sectionFileExists(this, "입사후포부"));
        Log.d(TAG, "- 지원동기: " + AIResponseFileManager.sectionFileExists(this, "지원동기"));
        Log.d(TAG, "- 성격장단점: " + AIResponseFileManager.sectionFileExists(this, "성격장단점"));

        // 파일 내용 확인
        if (AIResponseFileManager.sectionFileExists(this, "직무역량")) {
            String content = AIResponseFileManager.readResponseFile(this, "직무역량");
            Log.d(TAG, "직무역량 XML 원본 길이: " + content.length());
            Log.d(TAG, "직무역량 XML 내용 일부: " + content.substring(0, Math.min(100, content.length())));

            String extractedText = AIResponseFileManager.extractTextFromXml(content);
            Log.d(TAG, "직무역량 추출 텍스트 길이: " + extractedText.length());
            Log.d(TAG, "직무역량 추출 텍스트 일부: " + extractedText.substring(0, Math.min(100, extractedText.length())));
        }

        // XML에서 JSON 생성 테스트
        if (AIResponseFileManager.allFilesExist(this)) {
            Log.d(TAG, "모든 XML 파일 존재, JSON 변환 테스트");
            loadDataFromXmlFiles();
        }

        Log.d(TAG, "=== XML 파일 디버그 종료 ===");
    }
}