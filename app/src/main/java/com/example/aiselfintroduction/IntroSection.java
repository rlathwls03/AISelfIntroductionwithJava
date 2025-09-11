package com.example.aiselfintroduction;

import androidx.annotation.NonNull;

public class IntroSection {

    private final String title;
    private String content;

    public IntroSection(@NonNull String title, @NonNull String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    @NonNull
    @Override
    public String toString() {
        return "IntroSection{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}