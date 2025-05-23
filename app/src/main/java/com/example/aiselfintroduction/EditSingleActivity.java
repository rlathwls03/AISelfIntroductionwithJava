package com.example.aiselfintroduction;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EditSingleActivity extends AppCompatActivity {

    private TextView tvFieldTitle, tvFieldContent;
    private ImageButton btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_single);

        tvFieldTitle = findViewById(R.id.tvFieldTitle);
        tvFieldContent = findViewById(R.id.tvFieldContent);
        btnClose = findViewById(R.id.btnClose);

        // Intent로부터 데이터 받기
        String fieldKey = getIntent().getStringExtra("fieldKey");
        String resumeTitle = getIntent().getStringExtra("resumeTitle");

        tvFieldTitle.setText(fieldKey != null ? fieldKey : "제목 없음");

        // ✅ 저장소에서 데이터 로드
        String fieldContent = "";
        if (resumeTitle != null && fieldKey != null) {
            SelfIntroStorage storage = new SelfIntroStorage(this);
            SelfIntroData data = storage.loadSelfIntro(resumeTitle);

            if (data != null) {
                switch (fieldKey) {
                    case "직무역량":
                        fieldContent = data.get직무역량();
                        break;
                    case "입사후포부":
                        fieldContent = data.get입사후포부();
                        break;
                    case "지원동기":
                        fieldContent = data.get지원동기();
                        break;
                    case "성격장단점":
                        fieldContent = data.get성격장단점();
                        break;
                    default:
                        fieldContent = "지원 항목이 올바르지 않습니다.";
                }
            } else {
                fieldContent = "저장된 데이터가 없습니다.";
            }
        } else {
            fieldContent = "필수 정보 누락";
        }

        tvFieldContent.setText(fieldContent);
        tvFieldContent.setMovementMethod(new ScrollingMovementMethod());

        // 스크롤 충돌 방지
        tvFieldContent.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        // X 버튼: EditListActivity로 돌아감
        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(EditSingleActivity.this, EditListActivity.class);
            if (resumeTitle != null && !resumeTitle.isEmpty()) {
                intent.putExtra("resumeTitle", resumeTitle);
            }
            startActivity(intent);
            finish();
        });
    }

}