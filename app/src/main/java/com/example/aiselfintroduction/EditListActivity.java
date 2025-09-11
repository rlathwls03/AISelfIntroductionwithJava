package com.example.aiselfintroduction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiselfintroduction.translate.Translator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditListActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        for (Map.Entry<String, Button> entry : promptButtons.entrySet()) {
            String key = entry.getKey();
            Button btn = entry.getValue();
            if (isPromptDone(key)) {
                activateButton(key);
            } else {
                disableButton(btn);
            }
        }
    }

    private Button btnRole, btnGoal, btnReason, btnPersonality, btnEditAll, btnCheck;
    private TextView titleText;
    private ImageButton btnClose, btnHome;
    private final Handler promptHandler = new Handler();
    private final Queue<String> promptKeys = new LinkedList<>(Arrays.asList("직무역량", "입사후포부", "지원동기", "성격장단점"));
    private final Map<String, Button> promptButtons = new HashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final StringBuilder responseBuffer = new StringBuilder();
    private Runnable flushRunnable;
    private static final long BUFFER_DELAY_MS = 300;
    private Toast ongoingToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editlist);

        btnRole = findViewById(R.id.btnRole);
        btnGoal = findViewById(R.id.btnGoal);
        btnReason = findViewById(R.id.btnReason);
        btnPersonality = findViewById(R.id.btnPersonality);
        btnEditAll = findViewById(R.id.btnEditAll);
        btnHome = findViewById(R.id.btnHome);

        // ✅ XML에만 있고 기능이 없던 요소들 연결 추가
        titleText = findViewById(R.id.titleText);
        btnCheck = findViewById(R.id.btnCheck);
        disableButton(btnCheck);

        promptButtons.put("직무역량", btnRole);
        promptButtons.put("입사후포부", btnGoal);
        promptButtons.put("지원동기", btnReason);
        promptButtons.put("성격장단점", btnPersonality);

        for (Map.Entry<String, Button> entry : promptButtons.entrySet()) {
            String key = entry.getKey();
            Button btn = entry.getValue();
            if (isPromptDone(key)) {
                activateButton(key);
            } else {
                disableButton(btn);
            }
        }

        promptKeys.removeIf(this::isPromptDone);


        btnCheck.setOnClickListener(v -> {
            Intent intent = new Intent(EditListActivity.this, DownloadActivity.class);

            startActivity(intent);
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(EditListActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        btnEditAll.setOnClickListener(v -> startActivity(new Intent(this, EditActivity.class)));

        promptHandler.post(promptRunnable);
    }

    private final Runnable promptRunnable = new Runnable() {
        @Override
        public void run() {
            if (promptKeys.isEmpty()) return;

            String key = promptKeys.poll();
            JSONObject user = getUserInfoJson();
            String ocr = loadOcrText();
            String prompt = buildPromptForKey(user, ocr, key);

            sendPrompt(prompt, key);
        }
    };

    private void sendPrompt(String prompt, String key) {
        if (GenieWrapper.Instance == null) {
            Toast.makeText(this, "AI 모델이 로드되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        ongoingToast = Toast.makeText(this, "\"" + key + "\" 문장을 생성 중입니다.", Toast.LENGTH_LONG);
        ongoingToast.show();


        StringBuilder fullResponse = new StringBuilder();
        final boolean[] saved = {false};
        responseBuffer.setLength(0); // 버퍼 초기화

        GenieWrapper.Instance.getResponseForPrompt(prompt, str -> {
            // 1. 토큰을 버퍼에 추가
            responseBuffer.append(str);

            // 2. 이전 딜레이 제거 후 다시 예약
            uiHandler.removeCallbacks(flushRunnable);
            flushRunnable = () -> executor.execute(() -> {
                String chunk = responseBuffer.toString();
                responseBuffer.setLength(0); // 버퍼 비우기
                fullResponse.append(chunk);

                //Log.d("AI_응답", chunk);

                String trimmed = fullResponse.toString().trim();
                if (!saved[0] && (trimmed.endsWith("End") || trimmed.endsWith("End."))) {
                    saved[0] = true;

                    if (trimmed.endsWith("End.")) {
                        trimmed = trimmed.substring(0, trimmed.length() - 4).trim();
                    } else if (trimmed.endsWith("End")) {
                        trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
                    }

                    // 번역 후 저장 및 UI 업데이트
                    Translator.translate(trimmed, "ko", this, translatedText -> {
                        String translatedTrimmed = translatedText;
                        try {
                            JSONObject result = new JSONObject();
                            result.put("response", translatedTrimmed);

                            File file = new File(getFilesDir(), "AI_" + key + ".xml");
                            try (FileWriter writer = new FileWriter(file)) {
                                writer.write(result.toString());
                                Log.d("AI_저장완료", "파일 저장 성공: " + file.getName());
                            }

                            SharedPreferences prefs = getSharedPreferences("AIStatePrefs", MODE_PRIVATE);
                            prefs.edit().putBoolean("done_" + key, true).apply();

                            uiHandler.post(() -> {
                                if (ongoingToast != null) {
                                    ongoingToast.cancel();
                                    ongoingToast = null;
                                }
                                activateButton(key);
                                promptHandler.post(promptRunnable);
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            });

            uiHandler.postDelayed(flushRunnable, BUFFER_DELAY_MS);
        });
    }

    private boolean isPromptDone(String key) {
        SharedPreferences prefs = getSharedPreferences("AIStatePrefs", MODE_PRIVATE);
        return prefs.getBoolean("done_" + key, false);
    }

    private void activateButton(String key) {
        Button btn = promptButtons.get(key);
        if (btn == null) return;

        btn.setEnabled(true);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFD54F")));
        btn.setTextColor(Color.BLACK);

        btn.setOnClickListener(v -> {
            String content = loadResponse(key);
            Intent intent = new Intent(this, EditSingleActivity.class);
            intent.putExtra("fieldKey", key);
            intent.putExtra("fieldContent", content);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }

    private void disableButton(Button btn) {
        btn.setEnabled(false);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
        btn.setTextColor(Color.parseColor("#888888"));
    }

    private String loadOcrText() {
        SharedPreferences prefs = getSharedPreferences("OCRPrefs", MODE_PRIVATE);
        String json = prefs.getString("ocr_json", null);
        if (json != null) {
            try {
                JSONObject obj = new JSONObject(json);
                return obj.getString("ocr_text");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private JSONObject getUserInfoJson() {
        File userFile = new File(getApplicationInfo().dataDir + "/shared_prefs/UserInfoPrefs.xml");
        if (!userFile.exists()) return null;
        try (FileInputStream fis = new FileInputStream(userFile)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, "UTF-8");
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG && "string".equals(parser.getName())) {
                    String nameAttr = parser.getAttributeValue(null, "name");
                    if ("user_info".equals(nameAttr)) {
                        String jsonText = parser.nextText();
                        return new JSONObject(jsonText);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String buildPromptForKey(JSONObject user, String jobText, String key) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음은 나의 정보입니다.\n");
        prompt.append("전공: ").append(user.optString("major")).append("\n");
        prompt.append("학력: ").append(user.optString("educationLevel")).append("\n");
        prompt.append("성격: ").append(user.optString("personality")).append("\n");
        prompt.append("프로젝트 경험: ").append(user.optString("projects")).append("\n");
        appendJsonArray(prompt, "자격증", user.optJSONArray("certificates"));
        appendJsonArray(prompt, "경력", user.optJSONArray("experience"));
        appendJsonArray(prompt, "기술 키워드", user.optJSONArray("keywords"));
        prompt.append("넣고싶은말 : ").append(user.optString("extraSentence")).append("\n");
        prompt.append("다음은 내가 지원할 회사의 채용 공고입니다.\n").append(jobText).append("\n\n");
        prompt.append("이 정보를 바탕으로 \"").append(key).append("\"에 들어갈 자기소개서를 작성해줘.");
        return prompt.toString();
    }

    private void appendJsonArray(StringBuilder builder, String label, JSONArray array) {
        if (array != null && array.length() > 0) {
            builder.append(label).append(": ");
            for (int i = 0; i < array.length(); i++) {
                builder.append(array.optString(i));
                if (i < array.length() - 1) builder.append(", ");
            }
            builder.append("\n");
        }
    }

    private String loadResponse(String key) {
        File file = new File(getFilesDir(), "AI_" + key + ".xml");
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            JSONObject obj = new JSONObject(builder.toString());
            return obj.getString("response");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}