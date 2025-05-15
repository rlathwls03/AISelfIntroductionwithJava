package com.example.aiselfintroduction;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
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
            Intent intent = new Intent(EditSingleActivity.this, EditListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 스택 초기화 (중복 방지)
            startActivity(intent);
            finish();
        });
    }
}