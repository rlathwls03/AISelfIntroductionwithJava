package com.example.aiselfintroduction;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

public class IntroPagerAdapter extends RecyclerView.Adapter<IntroPagerAdapter.ViewHolder> {

    private final List<IntroSection> sectionList;

    public IntroPagerAdapter(List<IntroSection> sections) {
        this.sectionList = sections;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_intro_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IntroSection section = sectionList.get(position);

        // ✅ 기존 TextWatcher 제거 (중복 방지)
        if (holder.textWatcher != null) {
            holder.editText.removeTextChangedListener(holder.textWatcher);
        }

        //데이터 전처리
        String myText  = section.getContent();
        String displayText = "오류발생";
        if(myText != null){
            displayText = preprocessContent(myText);
        }

        // ✅ 데이터 설정
        holder.title.setText(section.getTitle());
        holder.editText.setText(displayText);
        holder.counter.setText(section.getContent().length() + "/1000 자 (공백 포함)");

        holder.editText.setMovementMethod(new ScrollingMovementMethod());

        // ✅ 새로운 TextWatcher 정의 및 등록
        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.counter.setText(s.length() + "/1000 자 (공백 포함)");
            }

            @Override
            public void afterTextChanged(Editable s) {
                section.setContent(s.toString()); // 수정 내용 반영
            }
        };

        holder.editText.addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    // ✅ ViewHolder 정의
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, counter;
        EditText editText;
        TextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sectionTitle);
            editText = itemView.findViewById(R.id.sectionEdit);
            counter = itemView.findViewById(R.id.charCounter);
        }
    }

    // ➊ 클래스 안에 전처리 메서드 추가
    private String preprocessContent(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        String text = raw;

        // — 1) HTML 태그나 엔티티 디코딩 (예: <br>, &quot; 등)
        //    Html.fromHtml 을 쓰면 안드로이드가 자동으로 디코딩해줍니다.
        text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();

        // — 2) JSON 감싸기가 되어 있으면 response 필드만 추출
        try {
            JsonObject obj = new Gson().fromJson(text, JsonObject.class);
            if (obj.has("response")) {
                text = obj.get("response").getAsString();
            }
        } catch (Exception ignored) {
            // “response” 필드 포맷이 아니면 그대로 두기
        }

        // — 3) 혹시 남아있는 XML/HTML 태그가 있으면 제거
        text = text.replaceAll("<[^>]+>", "");

        // — 4) 앞뒤 공백/개행 제거
        return text.trim();
    }
}