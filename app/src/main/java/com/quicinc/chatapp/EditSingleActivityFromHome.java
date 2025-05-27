package com.quicinc.chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class EditSingleActivityFromHome extends AppCompatActivity {

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
        Log.d("SINGLEACTIVITY","<><><><><><>전달 받은 키" + fieldKey);
        String fieldContent = getIntent().getStringExtra("fieldContent");
        Log.d("SINGLEACTIVITY","<><><><><><>전달 받은 내용" + fieldContent);
        String resumeTitle = getIntent().getStringExtra("resumeTitle");
        Log.d("SINGLEACTIVITY","<><><><><><>전달 받은 제목" + resumeTitle);
        if(fieldContent == null){
            // 1) SharedPreferences 열기 (SelfIntroPrefs.xml)
            SharedPreferences prefs = getSharedPreferences("SelfIntroPrefs", MODE_PRIVATE);
            // 2) resumeTitle 키로 outer JSON 문자열 꺼내기
            String outerJsonStr = prefs.getString(resumeTitle, null);
            Log.d("OUTERJSONSTR", "<><><><> OUTERJSONSTR : " + outerJsonStr);

            if (outerJsonStr != null) {
                try {
                    // 3) outer JSON 파싱
                    JsonObject outer = new Gson().fromJson(outerJsonStr, JsonObject.class);
                    // 4) fieldKey에 해당하는 inner JSON 문자열 추출
                    if (outer.has(fieldKey)) {
                        fieldContent = outer.get(fieldKey).getAsString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    fieldContent = outerJsonStr;
                }
            } else {
                fieldContent = "저장된 데이터가 없습니다.";
            }
        }
        else {
            tvFieldTitle.setText(fieldKey != null ? fieldKey : "제목 없음");
        }
        tvFieldContent.setText(fieldContent != null ? fieldContent : "내용 없음");
        tvFieldTitle.setText(fieldKey != null ? fieldKey : "제목 없음");
        // ✅ 텍스트뷰에 스크롤 기능 적용
        tvFieldContent.setMovementMethod(new ScrollingMovementMethod());

        // ✅ 터치 시 부모 스크롤 방지 → TextView만 스크롤되도록
        tvFieldContent.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        // X 버튼 누르면 화면 종료
        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(EditSingleActivityFromHome.this, EditListActivityFromHome.class);
            intent.putExtra("resumeTitle", resumeTitle);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 스택 초기화 (중복 방지)
            startActivity(intent);
            finish();
        });
    }
}