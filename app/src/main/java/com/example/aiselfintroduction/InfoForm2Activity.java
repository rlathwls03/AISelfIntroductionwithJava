package com.example.aiselfintroduction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.flexbox.FlexboxLayoutManager;
import java.util.ArrayList;
import java.util.List;

public class InfoForm2Activity extends AppCompatActivity {
    private EditText jobInput;
    private EditText keywordInput;
    private EditText personalityInput;
    private EditText experienceInput;
    private EditText extraSentenceInput;
    private RecyclerView jobChipsRecyclerView;
    private RecyclerView keywordChipsRecyclerView;
    private ChipsAdapter jobChipsAdapter;
    private ChipsAdapter keywordChipsAdapter;
    private List<String> jobChips = new ArrayList<>();
    private List<String> keywordChips = new ArrayList<>();
    private Button saveButton;
    private ImageButton backButton;
    private Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_form2);

        initializeViews();
        setupRecyclerViews();
        setupListeners();

        // 초기 데이터 설정 (테스트용)
        jobChips.add("삼성 개발자 1년 근무");
        jobChips.add("사무직 2년 근무");
        keywordChips.add("데이터 분석");
        keywordChips.add("MySQL");

        jobChipsAdapter.notifyDataSetChanged();
        keywordChipsAdapter.notifyDataSetChanged();
    }

    private void initializeViews() {
        jobInput = findViewById(R.id.jobInput);
        keywordInput = findViewById(R.id.keywordInput);
        personalityInput = findViewById(R.id.personalityInput);
        experienceInput = findViewById(R.id.experienceInput);
        extraSentenceInput = findViewById(R.id.extraSentenceInput);
        jobChipsRecyclerView = findViewById(R.id.jobChipsRecyclerView);
        keywordChipsRecyclerView = findViewById(R.id.keywordChipsRecyclerView);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        uploadButton = findViewById(R.id.uploadButton);
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (ev.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int[] scrcoords = new int[2];
                v.getLocationOnScreen(scrcoords);
                float x = ev.getRawX() + v.getLeft() - scrcoords[0];
                float y = ev.getRawY() + v.getTop() - scrcoords[1];

                if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                    v.clearFocus();
                    hideKeyboard(v);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void setupRecyclerViews() {
        // 경력사항 RecyclerView 설정
        FlexboxLayoutManager jobLayoutManager = new FlexboxLayoutManager(this);
        jobChipsRecyclerView.setLayoutManager(jobLayoutManager);
        jobChipsAdapter = new ChipsAdapter(jobChips, this::removeJobChip);
        jobChipsRecyclerView.setAdapter(jobChipsAdapter);

        // 키워드 RecyclerView 설정
        FlexboxLayoutManager keywordLayoutManager = new FlexboxLayoutManager(this);
        keywordChipsRecyclerView.setLayoutManager(keywordLayoutManager);
        keywordChipsAdapter = new ChipsAdapter(keywordChips, this::removeKeywordChip);
        keywordChipsRecyclerView.setAdapter(keywordChipsAdapter);
    }

    private void setupListeners() {
        // 경력사항 추가 버튼
        findViewById(R.id.addJobButton).setOnClickListener(v -> addJobChip());
        jobInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addJobChip();
                return true;
            }
            return false;
        });

        // 키워드 추가 버튼
        findViewById(R.id.addKeywordButton).setOnClickListener(v -> addKeywordChip());
        keywordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addKeywordChip();
                return true;
            }
            return false;
        });

        // 저장 버튼
        saveButton.setOnClickListener(v -> saveUserInfo());

        // 뒤로가기 버튼
        backButton.setOnClickListener(v -> finish());

        // 파일 업로드 버튼
        uploadButton.setOnClickListener(v -> handleFileUpload());
    }

    private void addJobChip() {
        String job = jobInput.getText().toString().trim();
        if (!job.isEmpty() && !jobChips.contains(job)) {
            jobChips.add(job);
            jobChipsAdapter.notifyDataSetChanged();
            jobInput.setText("");
        }
    }

    private void addKeywordChip() {
        String keyword = keywordInput.getText().toString().trim();
        if (!keyword.isEmpty() && !keywordChips.contains(keyword)) {
            keywordChips.add(keyword);
            keywordChipsAdapter.notifyDataSetChanged();
            keywordInput.setText("");
        }
    }

    private void removeJobChip(int position) {
        jobChips.remove(position);
        jobChipsAdapter.notifyItemRemoved(position);
    }

    private void removeKeywordChip(int position) {
        keywordChips.remove(position);
        keywordChipsAdapter.notifyItemRemoved(position);
    }

    private void saveUserInfo() {
        // TODO: 사용자 정보 저장 로직 작성

        Intent intent = new Intent(InfoForm2Activity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }


    private void handleFileUpload() {
        // TODO: Implement file upload
    }
}