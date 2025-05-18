package com.example.aiselfintroduction;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InputActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText convertedText;
    private LinearLayout imageUploadBox, imageContainer;
    private Button generateButton;
    private NestedScrollView scrollView;

    private final Set<String> uploadedImageNames = new HashSet<>();
    private final Map<ImageView, View> imageDividerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        convertedText = findViewById(R.id.convertedText);
        convertedText.setVerticalScrollBarEnabled(true);
        imageUploadBox = findViewById(R.id.imageUploadBox);
        imageContainer = findViewById(R.id.imageContainer);
        generateButton = findViewById(R.id.generateButton);
        scrollView = findViewById(R.id.scrollView);

        convertedText.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        convertedText.setVerticalScrollBarEnabled(true); // 스크롤바 표시

        convertedText.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        imageUploadBox.setOnClickListener(v -> openImagePicker());

        generateButton.setOnClickListener(v -> {
            // 예: MainActivity.java의 onCreate() 메서드 내에 추가
            // 1. 누락된 파일이 있다면 생성
            if (!AIResponseFileManager.allFilesExist(this)) {
                XMLFileChecker.createTestXMLFiles(this);
                Toast.makeText(this, "누락된 XML 파일을 생성했습니다.", Toast.LENGTH_SHORT).show();
            }

            String text = convertedText.getText().toString();
            if (!text.isEmpty()) {
                sendToAI(text);
            } else {
                Toast.makeText(this, "공고문을 입력하세요.", Toast.LENGTH_SHORT).show();
            }

            // 3. 다음 화면으로 이동
            Intent intent = new Intent(InputActivity.this, EditListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        setupKeyboardEvents(convertedText, scrollView);

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(InputActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    handleImageUpload(imageUri);
                }
            } else if (data.getData() != null) {
                handleImageUpload(data.getData());
            }
        }
    }

    private void handleImageUpload(Uri imageUri) {
        String imageName = getFileNameFromUri(imageUri);
        if (imageName != null && uploadedImageNames.contains(imageName)) {
            Toast.makeText(this, "중복된 이미지는 업로드할 수 없습니다: " + imageName, Toast.LENGTH_SHORT).show();
            return;
        }

        uploadedImageNames.add(imageName);

        try {
            // 고해상도 Bitmap 가져오기
            InputStream input = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400));
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            View divider = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 2);
            params.setMargins(0, 8, 0, 8);
            divider.setLayoutParams(params);
            divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

            // 삭제 기능
            imageView.setOnClickListener(v -> {
                imageContainer.removeView(imageView);
                imageContainer.removeView(divider);
                uploadedImageNames.remove(imageName);
                Toast.makeText(this, "이미지 삭제됨", Toast.LENGTH_SHORT).show();
            });

            imageContainer.addView(imageView);
            imageContainer.addView(divider);
            imageDividerMap.put(imageView, divider);

            callOCR(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callOCR(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            String base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

            JsonObject image = new JsonObject();
            image.addProperty("content", base64);

            JsonObject feature = new JsonObject();
            feature.addProperty("type", "TEXT_DETECTION");

            JsonObject request = new JsonObject();
            request.add("image", image);
            JsonArray features = new JsonArray();
            features.add(feature);
            request.add("features", features);

            JsonArray requests = new JsonArray();
            requests.add(request);

            JsonObject postData = new JsonObject();
            postData.add("requests", requests);

            OkHttpClient client = new OkHttpClient();
            String apiKey = getString(R.string.google_api_key);

            RequestBody body = RequestBody.create(postData.toString(), MediaType.parse("application/json"));
            Request requestObj = new Request.Builder()
                    .url("https://vision.googleapis.com/v1/images:annotate?key=" + apiKey)
                    .post(body)
                    .build();

            client.newCall(requestObj).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(InputActivity.this, "OCR 요청 실패", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        runOnUiThread(() -> {
                            try {
                                JSONObject root = new JSONObject(json);
                                JSONArray responses = root.getJSONArray("responses");
                                if (responses.length() > 0) {
                                    JSONObject annotation = responses.getJSONObject(0);
                                    if (annotation.has("fullTextAnnotation")) {
                                        String text = annotation.getJSONObject("fullTextAnnotation").getString("text");
                                        String current = convertedText.getText().toString();
                                        convertedText.setText(current + "\n\n" + text);
                                    } else {
                                        Toast.makeText(InputActivity.this, "텍스트를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception e) {
                                Toast.makeText(InputActivity.this, "OCR 결과 파싱 오류", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void sendToAI(String inputText) {
        Toast.makeText(this, "AI에게 전송 완료: " + inputText, Toast.LENGTH_LONG).show();
    }

    private void setupKeyboardEvents(EditText editText, NestedScrollView scrollView) {
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            scrollView.getWindowVisibleDisplayFrame(rect);
            int screenHeight = scrollView.getRootView().getHeight();
            int keypadHeight = screenHeight - rect.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                // 키보드 열림
                scrollView.post(() -> scrollView.smoothScrollTo(0, editText.getBottom()));
            } else {
                // 키보드 닫힘 시 포커스 제거
                if (editText.hasFocus()) {
                    editText.clearFocus();
                }
            }
        });
    }
}