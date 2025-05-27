package com.example.aiselfintroduction.ocr;

import android.content.Context;
import android.net.Uri;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoogleOCRProcessor {
    private static final String TAG = "GoogleOCRProcessor";
    private static final String API_KEY = "AIzaSyAWYaTbIDNls7W1wwKBYWRiRUoip7uJLzI";
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface OCRCallback {
        void onSuccess(String extractedText);
        void onFailure(String errorMessage);
    }

    public static void extractTextFromPDF(Context context, Uri pdfUri, OCRCallback callback) {
        executor.execute(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());

            try {
                // 1. PDF 파일 읽기 및 검증
                byte[] fileBytes = readBytesFromUri(context, pdfUri);
                if (fileBytes == null || fileBytes.length == 0) {
                    mainHandler.post(() -> callback.onFailure("파일 읽기 실패 또는 빈 파일"));
                    return;
                }

                Log.d(TAG, "파일 크기: " + fileBytes.length + " bytes");
                String base64Pdf = Base64.encodeToString(fileBytes, Base64.NO_WRAP);

                // 2. 요청 JSON 구성
                JSONObject requestJson = buildRequestJson(base64Pdf);

                // 3. API 요청
                String extractedText = sendOCRRequest(requestJson);

                if (extractedText != null && !extractedText.trim().isEmpty()) {
                    Log.d(TAG, "✅ OCR 성공: " + extractedText.length() + "글자 추출");
                    mainHandler.post(() -> callback.onSuccess(extractedText.trim()));
                } else {
                    Log.w(TAG, "⚠️ OCR 결과가 비어있음");
                    mainHandler.post(() -> callback.onFailure("OCR 결과가 없습니다"));
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ OCR 처리 오류: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onFailure("OCR 처리 중 오류: " + e.getMessage()));
            }
        });
    }

    private static JSONObject buildRequestJson(String base64Content) throws Exception {
        JSONObject requestJson = new JSONObject();
        JSONArray requestsArray = new JSONArray();
        JSONObject requestObj = new JSONObject();

        // Input Config
        JSONObject inputConfig = new JSONObject();
        inputConfig.put("mimeType", "application/pdf");
        inputConfig.put("content", base64Content);
        requestObj.put("inputConfig", inputConfig);

        // Features
        JSONArray features = new JSONArray();
        JSONObject feature = new JSONObject();
        feature.put("type", "DOCUMENT_TEXT_DETECTION");
        features.put(feature);
        requestObj.put("features", features);

        requestsArray.put(requestObj);
        requestJson.put("requests", requestsArray);

        return requestJson;
    }

    private static String sendOCRRequest(JSONObject requestJson) throws Exception {
        URL url = new URL("https://vision.googleapis.com/v1/files:annotate?key=" + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            // 요청 전송
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")) {
                writer.write(requestJson.toString());
                writer.flush();
            }

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "응답 코드: " + responseCode);

            // 응답 읽기
            InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            String jsonResponse = response.toString();
            Log.d(TAG, "API 응답: " + jsonResponse);

            if (responseCode >= 200 && responseCode < 300) {
                return parseOCRResponse(jsonResponse);
            } else {
                throw new Exception("API 오류 (코드: " + responseCode + "): " + jsonResponse);
            }

        } finally {
            conn.disconnect();
        }
    }

    private static String parseOCRResponse(String jsonResponse) throws Exception {
        JSONObject result = new JSONObject(jsonResponse);

        if (!result.has("responses")) {
            throw new Exception("응답에 'responses' 필드가 없습니다");
        }

        JSONArray responses = result.getJSONArray("responses");
        if (responses.length() == 0) {
            throw new Exception("응답 배열이 비어있습니다");
        }

        JSONObject firstResponse = responses.getJSONObject(0);

        // 에러 체크
        if (firstResponse.has("error")) {
            JSONObject error = firstResponse.getJSONObject("error");
            throw new Exception("OCR API 오류: " + error.toString());
        }

        // 텍스트 추출
        if (firstResponse.has("fullTextAnnotation")) {
            JSONObject fullTextAnnotation = firstResponse.getJSONObject("fullTextAnnotation");
            if (fullTextAnnotation.has("text")) {
                String extractedText = fullTextAnnotation.getString("text");
                Log.d(TAG, "📄 추출된 텍스트 길이: " + extractedText.length());
                return extractedText;
            }
        }

        // 페이지별 응답 체크 (files:annotate의 경우)
        if (firstResponse.has("responses")) {
            StringBuilder allText = new StringBuilder();
            JSONArray pageResponses = firstResponse.getJSONArray("responses");

            for (int i = 0; i < pageResponses.length(); i++) {
                JSONObject pageResponse = pageResponses.getJSONObject(i);
                if (pageResponse.has("fullTextAnnotation")) {
                    JSONObject annotation = pageResponse.getJSONObject("fullTextAnnotation");
                    if (annotation.has("text")) {
                        allText.append(annotation.getString("text")).append("\n");
                    }
                }
            }

            if (allText.length() > 0) {
                return allText.toString();
            }
        }

        Log.w(TAG, "OCR 결과를 찾을 수 없습니다. 응답 구조: " + firstResponse.toString());
        return "";
    }

//    private static byte[] readBytesFromUri(Context context, Uri uri) {
//        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
//            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//            byte[] data = new byte[4096];
//            int nRead;
//            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
//                buffer.write(data, 0, nRead);
//            }
//            return buffer.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    private static byte[] readBytesFromUri(Context context, Uri uri) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                Log.e(TAG, "InputStream이 null입니다");
                return null;
            }

            byte[] data = new byte[8192]; // 버퍼 크기 증가
            int bytesRead;
            while ((bytesRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            return buffer.toByteArray();

        } catch (Exception e) {
            Log.e(TAG, "파일 읽기 오류: " + e.getMessage(), e);
            return null;
        }
    }

    // 리소스 정리
    public static void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
