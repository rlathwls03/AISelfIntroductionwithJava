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
        String fieldContent = getIntent().getStringExtra("fieldContent");

        tvFieldTitle.setText(fieldKey != null ? fieldKey : "제목 없음");
        tvFieldContent.setText(fieldContent != null ? fieldContent : "내용 없음");

        // ✅ 텍스트뷰에 스크롤 기능 적용
        tvFieldContent.setMovementMethod(new ScrollingMovementMethod());

        // ✅ 터치 시 부모 스크롤 방지 → TextView만 스크롤되도록
        tvFieldContent.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        // X 버튼 누르면 화면 종료
        btnClose.setOnClickListener(v -> {
            // 원래 자기소개서 제목을 다시 EditListActivity로 전달
            String resumeTitle = getIntent().getStringExtra("resumeTitle");

            // 로그 추가
            Log.d("EditSingleActivity", "닫기 버튼: 원래 제목으로 돌아가기 - " + resumeTitle);

            Intent intent = new Intent(EditSingleActivity.this, EditListActivity.class);
            if (resumeTitle != null && !resumeTitle.isEmpty()) {
                intent.putExtra("resumeTitle", resumeTitle);
            }startActivity(intent);
            finish();
        });
    }
}