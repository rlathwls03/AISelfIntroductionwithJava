package com.example.aiselfintroduction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiselfintroduction.HomeActivity;
import com.example.aiselfintroduction.InfoFormActivity;
import com.example.aiselfintroduction.R;
import com.example.aiselfintroduction.SelfIntro;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SelfIntroAdapter adapter;
    private List<SelfIntro> favoriteIntros;
    private SharedPreferences prefs;
    private static final String FAVORITES_KEY = "FAVORITE_INTROS";
    private boolean isManuallyUpdating = false;
    private LinearLayout emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        prefs = getSharedPreferences("IntroPrefs", MODE_PRIVATE);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_favorite);

        recyclerView = findViewById(R.id.recyclerview_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        emptyState = findViewById(R.id.emptyState);

        favoriteIntros = new ArrayList<>();
        adapter = new SelfIntroAdapter(this, favoriteIntros);
        adapter.setOnFavoriteChangedListener(new SelfIntroAdapter.OnFavoriteChangedListener() {
            @Override
            public void onFavoriteRemoved(SelfIntro item, int position) {
                isManuallyUpdating = true;
                adapter.removeItem(position);
                updateEmptyState();
                isManuallyUpdating = false;
            }
        });
        recyclerView.setAdapter(adapter);

        loadFavorites();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(FavoritesActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(FavoritesActivity.this, InfoFormActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isManuallyUpdating) {
            loadFavorites(); // 수동 업데이트 중이 아닐 때만 즐겨찾기 목록 갱신
        }
    }

    private void loadFavorites() {
        Set<String> favorites = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());

        favoriteIntros.clear();
        for (String title : favorites) {
            favoriteIntros.add(new SelfIntro(title, true));
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (favoriteIntros.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
}