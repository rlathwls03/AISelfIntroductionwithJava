package com.example.aiselfintroduction;

import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class DownloadActivity extends AppCompatActivity {

    private WebView webView;
    private Button btnDownload;
    private String introName = "AI 자기소개서";

    private final Map<String, String> sectionMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        webView = findViewById(R.id.webView);
        btnDownload = findViewById(R.id.btnDownload);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnHome = findViewById(R.id.btnHome);

        // 전달받은 데이터 파싱
        Intent intent = getIntent();
        String resumeTitle = intent.getStringExtra("resumeTitle");
        String aiJson = intent.getStringExtra("aiJson");

        if (resumeTitle != null) introName = resumeTitle;

        if (aiJson != null) {
            try {
                JsonObject json = new Gson().fromJson(aiJson, JsonObject.class);
                if (json.has("직무역량"))
                    sectionMap.put("직무 역량", json.get("직무역량").getAsString());
                if (json.has("입사후포부"))
                    sectionMap.put("입사 후 포부", json.get("입사후포부").getAsString());
                if (json.has("지원동기"))
                    sectionMap.put("지원 동기", json.get("지원동기").getAsString());
                if (json.has("성격의장단점"))
                    sectionMap.put("성격의 장단점", json.get("성격의장단점").getAsString());
            } catch (Exception e) {
                Toast.makeText(this, "AI 데이터 파싱 오류", Toast.LENGTH_SHORT).show();
            }
        }

        // WebView 설정
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.setVerticalScrollBarEnabled(true);
        webView.setScrollbarFadingEnabled(false);

        webView.loadDataWithBaseURL(null, generateHtml(), "text/html", "UTF-8", null);

        btnDownload.setOnClickListener(v -> showPdfPreview());

        btnBack.setOnClickListener(v -> finish());

        btnHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(DownloadActivity.this, MainActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(homeIntent);
        });
    }

    private void showPdfPreview() {
        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);

        String jobName = introName + "_자기소개서";

        printManager.print(jobName,
                webView.createPrintDocumentAdapter(jobName),
                builder.build());

        Toast.makeText(this, "PDF 다운로드 준비 중...", Toast.LENGTH_SHORT).show();
    }

    private String generateHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><style>");
        html.append("body { font-family: 'sans-serif'; padding: 40px; font-size: 15px; color: #333; }");
        html.append("h1 { text-align: left; font-size: 22px; margin-bottom: 30px; }");
        html.append(".section { margin-bottom: 24px; }");
        html.append(".section-title { font-size: 16px; font-weight: bold; color: #f4b400; margin-bottom: 6px; }");
        html.append(".section-text { font-size: 14px; line-height: 1.6; }");
        html.append(".divider { border-bottom: 1px solid #ccc; margin-top: 12px; }");
        html.append("</style></head><body>");

        html.append("<h1>").append(introName).append("</h1>");

        for (Map.Entry<String, String> entry : sectionMap.entrySet()) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>").append(entry.getKey()).append("</div>");
            html.append("<div class='section-text'>").append(entry.getValue()).append("</div>");
            html.append("<div class='divider'></div>");
            html.append("</div>");
        }

        html.append("</body></html>");
        return html.toString();
    }
}
