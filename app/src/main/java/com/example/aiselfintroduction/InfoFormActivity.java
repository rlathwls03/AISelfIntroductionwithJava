package com.example.aiselfintroduction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        getSupportActionBar().hide();

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

    private void setupRecyclerView() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        certificatesRecyclerView.setLayoutManager(layoutManager);
        certificatesAdapter = new com.example.aiselfintroduction.ChipsAdapter(certificates, this::removeCertificate);
        certificatesRecyclerView.setAdapter(certificatesAdapter);
    }

    private void setupListeners() {
        findViewById(R.id.addCertificateButton).setOnClickListener(v -> addCertificate());

        nextButton.setOnClickListener(v -> {
            // Save user info and navigate to next screen
            saveUserInfo();
            startActivity(new Intent(this, InfoForm2Activity.class));
        });

        homeButton.setOnClickListener(v -> {
            // Navigate to home screen
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