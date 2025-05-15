package com.example.aiselfintroduction;

public class SelfIntro {
    private String title;
    private boolean isFavorite;

    public SelfIntro(String title, boolean isFavorite) {
        this.title = title;
        this.isFavorite = isFavorite;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
    }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}

