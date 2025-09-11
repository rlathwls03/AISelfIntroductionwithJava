package com.example.aiselfintroduction.translate;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;

import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Translator {
    private static final String API_KEY = "AIzaSyDrseeMFm65AKJCnCQus8FaWq-hwSaHVIc";

    public static void translate(String originalText, String targetLang, Context context, Consumer<String> callback) {
        TranslationService service = ApiClient.getClient().create(TranslationService.class);

        Call<JsonObject> call = service.translateText(originalText, targetLang, "text", API_KEY);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String translatedText = response.body()
                                .getAsJsonObject("data")
                                .getAsJsonArray("translations")
                                .get(0)
                                .getAsJsonObject()
                                .get("translatedText")
                                .getAsString();
                        callback.accept(translatedText);
                    } catch (Exception e) {
                        Toast.makeText(context, "번역 파싱 오류", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("Translator", "번역 실패: responseCode=" + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("Translator", "errorBody=" + response.errorBody().string());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(context, "번역 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(context, "API 호출 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}