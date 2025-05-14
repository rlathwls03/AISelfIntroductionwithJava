package com.example.aiselfintroduction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.flexbox.FlexboxLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aiselfintroduction.InfoForm2Activity;
import com.example.aiselfintroduction.R;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;


public class InfoFormActivity extends AppCompatActivity {
    private EditText nameInput;
    private EditText phoneInput;
    private EditText emailInput;
    private Spinner educationSpinner;
    private EditText schoolInput;
    private EditText majorInput;
    private EditText certificateInput;
    private RecyclerView certificatesRecyclerView;
    private com.example.aiselfintroduction.ChipsAdapter certificatesAdapter;
    private List<String> certificates = new ArrayList<>();
    private Button nextButton;
    private ImageButton homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_form);
//        getSupportActionBar().hide();

        initializeViews();
        setupSpinner();
        setupRecyclerView();
        setupListeners();
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        educationSpinner = findViewById(R.id.educationSpinner);
        schoolInput = findViewById(R.id.schoolInput);
        majorInput = findViewById(R.id.majorInput);
        certificateInput = findViewById(R.id.certificateInput);
        certificatesRecyclerView = findViewById(R.id.certificatesRecyclerView);
        nextButton = findViewById(R.id.nextButton);
        homeButton = findViewById(R.id.homeButton);
    }

    private void setupSpinner() {
        String[] educationLevels = {
                "고등학교",
                "대학교 (2,3년제)",
                "대학교 (4년제)",
                "석사과정",
                "박사과정",
                "석박사통합과정"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                educationLevels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        educationSpinner.setAdapter(adapter);
    }

    // 키보드 숨기기 메서드
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
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

    private void setupRecyclerView() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        certificatesRecyclerView.setLayoutManager(layoutManager);
        certificatesAdapter = new com.example.aiselfintroduction.ChipsAdapter(certificates, this::removeCertificate);
        certificatesRecyclerView.setAdapter(certificatesAdapter);
    }

    private void setupListeners() {
        // + 버튼 클릭 시 동작
        findViewById(R.id.addCertificateButton).setOnClickListener(v -> addCertificate());

        // 키보드 완료(IME_ACTION_DONE) → + 버튼 동작과 동일하게 실행
        certificateInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addCertificate();
                return true;
            }
            return false;
        });

        // 다음 버튼 클릭 → InfoForm2Activity 이동
        nextButton.setOnClickListener(v -> {
            saveUserInfo();
            startActivity(new Intent(this, InfoForm2Activity.class));
        });

        // 홈 버튼 → 이전 화면 종료
        homeButton.setOnClickListener(v -> {
            finish();
        });
    }


    private void addCertificate() {
        String certificate = certificateInput.getText().toString().trim();
        if (!certificate.isEmpty() && !certificates.contains(certificate)) {
            certificates.add(certificate);
            certificatesAdapter.notifyDataSetChanged();
            certificateInput.setText("");
        }
    }

    private void removeCertificate(int position) {
        certificates.remove(position);
        certificatesAdapter.notifyItemRemoved(position);
    }

    private void saveUserInfo() {
        // TODO: Implement saving user info to storage
    }
}