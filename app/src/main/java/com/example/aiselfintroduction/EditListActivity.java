package com.example.aiselfintroduction;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

public class EditListActivity extends AppCompatActivity {

    private Button btnRole, btnGoal, btnReason, btnPersonality, btnEditAll;
    private ImageButton btnClose, btnHome;
    private JsonObject aiJson;

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
        btnClose = findViewById(R.id.btnClose);
        btnHome = findViewById(R.id.btnHome);

        // 기본 비활성화 상태 설정 (회색)
        disableButton(btnRole);
        disableButton(btnGoal);
        disableButton(btnReason);
        disableButton(btnPersonality);

        // 닫기 버튼
        btnClose.setOnClickListener(v -> {
            finish(); // 현재 화면 종료
        });

        // 홈 버튼
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(EditListActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // 예시 AI 응답 - 실제 앱에서는 AI 서버에서 전달된 JSON 사용
        aiJson = new JsonObject();
        aiJson.addProperty("직무역량", "저는 Node.js와 React를 기반으로 한 웹 개발 경험이 있습니다.");
        aiJson.addProperty("입사후포부", "귀사에서 프론트엔드 고도화 프로젝트에 기여하고 싶습니다.");
        aiJson.addProperty("지원동기", "기술력과 팀워크를 중시하는 귀사의 가치관에 공감합니다.");
//        aiJson.addProperty("성격장단점", "성실하고 끈기가 있으며, 가끔 완벽주의적인 면이 있습니다.");

        // AI 응답이 존재할 경우 버튼 활성화
        updateButtonState(btnRole, "직무역량");
        updateButtonState(btnGoal, "입사후포부");
        updateButtonState(btnReason, "지원동기");
        updateButtonState(btnPersonality, "성격장단점");

        // 전체 편집 화면 이동
        btnEditAll.setOnClickListener(v -> {
            Intent intent = new Intent(EditListActivity.this, EditActivity.class);
            intent.putExtra("aiJson", aiJson.toString());
            startActivity(intent);
        });
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
        if (aiJson.has(key)) {
            String content = aiJson.get(key).getAsString();
            enableButton(btn);
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(EditListActivity.this, EditSingleActivity.class);
                intent.putExtra("fieldKey", key);
                intent.putExtra("fieldContent", content);
                startActivity(intent);
            });
        }
    }
}