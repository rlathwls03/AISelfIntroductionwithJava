package com.example.aiselfintroduction;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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

                String[] keys = {"직무역량", "입사후포부", "지원동기", "성격장단점"};
                String[] sectionTitles = {"직무 역량", "입사 후 포부", "지원 동기", "성격 장단점"};
                for (int i = 0; i < keys.length; i++) {
                    if (json.has(keys[i])) {
                        String rawVal = json.get(keys[i]).getAsString(); // 무조건 String으로 받음
                        String toShow;
                        try {
                            // {"response":"내용"} 형식이면 response만 추출
                            JsonObject obj = new Gson().fromJson(rawVal, JsonObject.class);
                            if (obj.has("response")) {
                                toShow = obj.get("response").getAsString();
                            } else {
                                toShow = rawVal;
                            }
                        } catch (Exception e) {
                            // 파싱 실패(plain text) → 그대로 사용
                            toShow = rawVal;
                        }
                        sectionMap.put(sectionTitles[i], toShow);
                    }
                }

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

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(DownloadActivity.this, EditListActivityFromHome.class);
            backIntent.putExtra("resumeTitle", resumeTitle);
            startActivity(backIntent);
        });

        btnHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(DownloadActivity.this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(homeIntent);
        });
    }

    private void showYellowToast(String message) {
        try {
            // 커스텀 레이아웃 생성
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(40, 20, 40, 20);

            // 노란색 배경 설정
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(Color.parseColor("#FCD965"));
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
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showPdfPreview() {
        try {
            // 먼저 토스트 메시지 표시
            showYellowToast("PDF 다운로드 준비 중...");

            // 약간의 딜레이 후 프린트 작업 실행
            new android.os.Handler().postDelayed(() -> {
                try {
                    PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
                    PrintAttributes.Builder builder = new PrintAttributes.Builder();
                    builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);

                    String jobName = introName + "_자기소개서";

                    printManager.print(jobName,
                            webView.createPrintDocumentAdapter(jobName),
                            builder.build());
                } catch (Exception e) {
                    Log.e("DownloadActivity", "PDF 미리보기 오류", e);
                    showYellowToast("PDF 준비 중 오류가 발생했습니다.");
                }
            }, 200); // 200ms 딜레이
        } catch (Exception e) {
            Log.e("DownloadActivity", "토스트 표시 오류", e);
            // 기본 토스트로 폴백
            Toast.makeText(this, "PDF 다운로드 준비 중...", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><style>");
        html.append("body { font-family: Arial, sans-serif; padding: 30px; font-size: 14px; color: #000; }");
        html.append("h1 { text-align: center; font-size: 20px; margin-bottom: 40px; }");
        html.append("table { width: 100%; border-collapse: collapse; }");
        html.append("tr { border: 1px solid #888; }");
        html.append("td { border: 1px solid #888; padding: 10px; vertical-align: top; }");
        html.append(".title-cell { font-weight: bold; background-color: #f4b400; }");
        html.append("</style></head><body>");

        html.append("<h1>").append(introName).append("</h1>");

        html.append("<table>");

        for (Map.Entry<String, String> entry : sectionMap.entrySet()) {
            // 제목 행
            html.append("<tr>");
            html.append("<td class=\"title-cell\">").append(entry.getKey()).append("</td>");
            html.append("</tr>");

            // 내용 행
            html.append("<tr>");
            // 줄바꿈을 <br> 태그로 변환
            String content = entry.getValue().replace("\n", "<br/>");
            html.append("<td>").append(content).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");
        html.append("</body></html>");

        // 로그로 생성된, HTML 확인 (디버깅용)
        Log.d("DownloadActivity", "생성된 HTML: " + html.toString().substring(0, Math.min(200, html.length())) + "...");

        return html.toString();
    }
}