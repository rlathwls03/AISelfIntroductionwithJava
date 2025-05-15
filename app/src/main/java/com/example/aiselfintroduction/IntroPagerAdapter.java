package com.example.aiselfintroduction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        // ✅ 데이터 설정
        holder.title.setText(section.getTitle());
        holder.editText.setText(section.getContent());
        holder.counter.setText(section.getContent().length() + "/1000 자 (공백 포함)");

        holder.editText.setMovementMethod(new android.text.method.ScrollingMovementMethod());

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
}