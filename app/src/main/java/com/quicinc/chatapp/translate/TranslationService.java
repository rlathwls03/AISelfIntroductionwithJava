package com.quicinc.chatapp.translate;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface TranslationService {
    @POST("language/translate/v2")
    @FormUrlEncoded
    Call<JsonObject> translateText(
            @Field("q") String text,
            @Field("target") String targetLang,
            @Field("format") String format,
            @Field("key") String apiKey
    );
}