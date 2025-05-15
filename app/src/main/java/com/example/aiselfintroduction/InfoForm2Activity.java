package com.example.aiselfintroduction;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private static final int PICK_PDF_FILE = 1;
    private List<UserInfo.FileInfo> selectedFiles = new ArrayList<>();
    private UserInfoStorage userInfoStorage;
    private LinearLayout fileListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_form2);

        userInfoStorage = new UserInfoStorage(this);
        initializeViews();
        setupRecyclerViews();
        setupListeners();
        loadSavedData();

        jobChipsAdapter.notifyDataSetChanged();
        keywordChipsAdapter.notifyDataSetChanged();
    }

    // 저장된 데이터 불러오는 함수
    private void loadSavedData() {
        UserInfo savedInfo = userInfoStorage.loadUserInfo();
        if (savedInfo != null) {
            // Load saved data
            jobChips.clear();
            jobChips.addAll(savedInfo.getExperience());
            jobChipsAdapter.notifyDataSetChanged();

            keywordChips.clear();
            keywordChips.addAll(savedInfo.getKeywords());
            keywordChipsAdapter.notifyDataSetChanged();

            personalityInput.setText(savedInfo.getPersonality());
            experienceInput.setText(savedInfo.getProjects());
            extraSentenceInput.setText(savedInfo.getExtraSentence());

            selectedFiles.clear();
            selectedFiles.addAll(savedInfo.getFiles());
            updateFileList();
        }
    }

    // 파일 업로드 함수
    private void handleFileUpload() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        // 파일 선택 후 다른 액티비티로 이동하지 않도록 플래그 추가
        // 더 안전한 Intent 플래그 설정
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, PICK_PDF_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 파일 선택 결과 처리
        if (requestCode == PICK_PDF_FILE) {
            try {
                if (resultCode == RESULT_OK && data != null) {
                    if (data.getClipData() != null) {
                        // Multiple files selected
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            processSelectedFile(data.getClipData().getItemAt(i).getUri());
                        }
                    } else if (data.getData() != null) {
                        // Single file selected
                        processSelectedFile(data.getData());
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // 사용자가 파일 선택을 취소한 경우
                    // 안전한 토스트 메시지 표시
                    showSafeToast("파일 선택이 취소되었습니다.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showSafeToast("파일 처리 중 오류가 발생했습니다.");
            }
            // 중요: return 문 추가로 의도치 않은 액티비티 이동 방지
            return;
        }
    }

    // onResume 메서드 추가 - 액티비티가 다시 활성화될 때 현재 상태 유지
    @Override
    protected void onResume() {
        super.onResume();
        // 파일 선택 후 돌아왔을 때 상태 유지
        if (jobChipsAdapter != null) {
            jobChipsAdapter.notifyDataSetChanged();
        }
        if (keywordChipsAdapter != null) {
            keywordChipsAdapter.notifyDataSetChanged();
        }
        updateFileList();
    }

    // onPause 메서드 추가 - 액티비티가 일시 정지될 때 상태 저장
    @Override
    protected void onPause() {
        super.onPause();
        // 현재 상태를 임시 저장 (파일 선택 중에 데이터가 손실되지 않도록)
        saveCurrentState();
    }

    // 현재 상태 저장하는 메서드
    private void saveCurrentState() {
        // 현재 입력된 데이터를 임시로 저장
        UserInfo currentInfo = userInfoStorage.loadUserInfo();
        if (currentInfo == null) {
            currentInfo = new UserInfo();
        }

        // 현재 화면의 데이터로 업데이트 (저장하지는 않고 메모리에만)
        currentInfo.setExperience(new ArrayList<>(jobChips));
        currentInfo.setKeywords(new ArrayList<>(keywordChips));
        currentInfo.setPersonality(personalityInput.getText().toString().trim());
        currentInfo.setProjects(experienceInput.getText().toString().trim());
        currentInfo.setExtraSentence(extraSentenceInput.getText().toString().trim());
        currentInfo.setFiles(new ArrayList<>(selectedFiles));
    }

    // 선택된 파일 처리 함수 (중복 처리)
    private void processSelectedFile(Uri uri) {
        try {
            String fileName = getFileName(uri);
            String mimeType = getContentResolver().getType(uri);

            // 파일명 유효성 확인
            if (fileName == null || fileName.trim().isEmpty()) {
                showSafeToast("잘못된 파일입니다.");
                return;
            }

            // 중복 파일 확인
            if (isFileAlreadySelected(fileName)) {
                showSafeToast("'" + fileName + "'은(는) 이미 선택된 파일입니다.");
                return;
            }

            // PDF 파일인지 확인
            if (mimeType == null || !mimeType.equals("application/pdf")) {
                showSafeToast("PDF 파일만 선택할 수 있습니다.");
                return;
            }

            // Copy file to app's private storage
            String filePath = copyFileToPrivateStorage(uri, fileName);

            if (filePath != null) {
                UserInfo.FileInfo fileInfo = new UserInfo.FileInfo(fileName, filePath, mimeType);
                selectedFiles.add(fileInfo);
                updateFileList();

                // 파일 추가 성공 Toast
                showSafeToast("'" + fileName + "'이(가) 추가되었습니다.");
            } else {
                showSafeToast("파일 저장에 실패했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showSafeToast("파일 처리 중 오류가 발생했습니다.");
        }
    }

    // 이미 선택된 파일인지 확인하는 메서드
    private boolean isFileAlreadySelected(String fileName) {
        for (UserInfo.FileInfo fileInfo : selectedFiles) {
            if (fileInfo.getFileName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

//    // 안전한 Toast 표시 메서드 - 단순하고 안정적
//    private void showSafeToast(String message) {
//        try {
//            Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 150);
//            toast.show();
//        } catch (Exception e) {
//            e.printStackTrace();
//            // 최후의 수단으로 가장 기본적인 Toast
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//        }
//    }

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

    // 파일 이름 가져오는 함수 (개선된 버전)
    private String getFileName(Uri uri) {
        String result = null;

        if (uri == null) {
            return null;
        }

        try {
            if ("content".equals(uri.getScheme())) {
                try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            result = cursor.getString(nameIndex);
                        }
                    }
                }
            }

            if (result == null) {
                result = uri.getPath();
                if (result != null) {
                    int cut = result.lastIndexOf('/');
                    if (cut != -1) {
                        result = result.substring(cut + 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    // 파일 복사 함수 (개선된 버전)
    private String copyFileToPrivateStorage(Uri uri, String fileName) {
        try {
            // 파일명이 이미 존재하는 경우 새로운 이름 생성
            File destinationFile = new File(getFilesDir(), fileName);
            int counter = 1;
            String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));

            while (destinationFile.exists()) {
                String newFileName = nameWithoutExt + "_" + counter + extension;
                destinationFile = new File(getFilesDir(), newFileName);
                counter++;
            }

            try (InputStream is = getContentResolver().openInputStream(uri);
                 OutputStream os = new FileOutputStream(destinationFile)) {

                if (is == null) {
                    return null;
                }

                byte[] buffer = new byte[4096]; // 버퍼 크기 증가
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
                return destinationFile.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 파일 리스트 업데이트
    private void updateFileList() {
        fileListContainer.removeAllViews();
        for (int i = 0; i < selectedFiles.size(); i++) {
            UserInfo.FileInfo fileInfo = selectedFiles.get(i);
            View fileItemView = getLayoutInflater().inflate(R.layout.file_item, fileListContainer, false);

            TextView fileNameText = fileItemView.findViewById(R.id.fileName);
            ImageButton deleteButton = fileItemView.findViewById(R.id.deleteFile);

            fileNameText.setText(fileInfo.getFileName());

            final int position = i;
            deleteButton.setOnClickListener(v -> {
                selectedFiles.remove(position);
                updateFileList();
            });

            fileListContainer.addView(fileItemView);
        }
    }

    private void saveUserInfo() {
//        // 기존 저장된 데이터 불러오기
//        UserInfo savedInfo = userInfoStorage.loadUserInfo();
//        UserInfo userInfo = (savedInfo != null) ? savedInfo : new UserInfo();
//
//        // 추가 정보 설정 (기존 기본 정보는 유지)
//        userInfo.setExperience(new ArrayList<>(jobChips));
//        userInfo.setKeywords(new ArrayList<>(keywordChips));
//        userInfo.setPersonality(personalityInput.getText().toString());
//        userInfo.setProjects(experienceInput.getText().toString());
//        userInfo.setExtraSentence(extraSentenceInput.getText().toString());
//        userInfo.setFiles(new ArrayList<>(selectedFiles));

        // 기존 저장된 데이터 불러오기
        UserInfo savedInfo = userInfoStorage.loadUserInfo();
        if (savedInfo == null) {
            savedInfo = new UserInfo();
        }

        // 추가 정보만 업데이트 (기존 기본 정보는 유지)
        savedInfo.setExperience(new ArrayList<>(jobChips));
        savedInfo.setKeywords(new ArrayList<>(keywordChips));
        savedInfo.setPersonality(personalityInput.getText().toString().trim());
        savedInfo.setProjects(experienceInput.getText().toString().trim());
        savedInfo.setExtraSentence(extraSentenceInput.getText().toString().trim());
        savedInfo.setFiles(new ArrayList<>(selectedFiles));

        // 저장
        userInfoStorage.saveUserInfo(savedInfo);

        // 저장 후 UI 업데이트 - 모든 Toast를 showSafeToast로 처리
        showSafeToast("저장되었습니다.");

        // 1.5초 후 HomeActivity로 이동
        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(InfoForm2Activity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 1500);
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
        fileListContainer = findViewById(R.id.fileListContainer);
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
}