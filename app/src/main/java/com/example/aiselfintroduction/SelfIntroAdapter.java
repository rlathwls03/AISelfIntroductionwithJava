package com.example.aiselfintroduction;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiselfintroduction.R;
import com.example.aiselfintroduction.SelfIntro;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelfIntroAdapter extends RecyclerView.Adapter<SelfIntroAdapter.ViewHolder> {
    private List<SelfIntro> selfIntroList;
    private Context context;
    private static final String FAVORITES_KEY = "FAVORITE_INTROS";
    private SharedPreferences prefs;
    private OnFavoriteChangedListener favoriteChangedListener;

    public interface OnFavoriteChangedListener {
        void onFavoriteRemoved(SelfIntro item, int position);
    }

    public void setOnFavoriteChangedListener(OnFavoriteChangedListener listener) {
        this.favoriteChangedListener = listener;
    }

    public SelfIntroAdapter(Context context, List<SelfIntro> list) {
        this.context = context;
        this.selfIntroList = list;
        this.prefs = context.getSharedPreferences("IntroPrefs", Context.MODE_PRIVATE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView starIcon, moreIcon;
        TextView titleText;

        public ViewHolder(View view) {
            super(view);
            starIcon = view.findViewById(R.id.starIcon);
            moreIcon = view.findViewById(R.id.moreIcon);
            titleText = view.findViewById(R.id.titleText);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_self_intro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SelfIntro item = selfIntroList.get(position);
        holder.titleText.setText(item.getTitle());

        // 즐겨찾기 상태 불러오기
        Set<String> favorites = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());
        item.setFavorite(favorites.contains(item.getTitle()));

        // 즐겨찾기 아이콘 업데이트
        holder.starIcon.setImageResource(
                item.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline
        );

        // 즐겨찾기 토글
        holder.starIcon.setOnClickListener(v -> {
            // 현재 즐겨찾기 목록 가져오기
            Set<String> currentFavorites = new HashSet<>(
                    prefs.getStringSet(FAVORITES_KEY, new HashSet<>())
            );

            // 즐겨찾기 상태 토글
            if (item.isFavorite()) {
                currentFavorites.remove(item.getTitle());
                if (favoriteChangedListener != null) {
                    favoriteChangedListener.onFavoriteRemoved(item, holder.getAdapterPosition());
                }
            } else {
                currentFavorites.add(item.getTitle());
            }

            // 변경된 즐겨찾기 목록 저장
            prefs.edit().putStringSet(FAVORITES_KEY, currentFavorites).commit();

            // UI 업데이트
            item.setFavorite(!item.isFavorite());
            notifyItemChanged(holder.getAdapterPosition());
        });

        // 더보기 팝업
        holder.moreIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.moreIcon);
            popup.getMenuInflater().inflate(R.menu.self_intro_popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(menuItem -> {
                int id = menuItem.getItemId();
                if (id == R.id.menu_edit) {
                    // 수정 로직
                    return true;
                } else if (id == R.id.menu_delete) {
                    // 삭제 시 즐겨찾기에서도 제거
                    Set<String> currentFavorites = new HashSet<>(
                            prefs.getStringSet(FAVORITES_KEY, new HashSet<>())
                    );
                    currentFavorites.remove(item.getTitle());
                    prefs.edit().putStringSet(FAVORITES_KEY, currentFavorites).commit();

                    selfIntroList.remove(position);
                    notifyItemRemoved(position);
                    return true;
                } else if (id == R.id.menu_download) {
                    // 다운로드 로직
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return selfIntroList.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < selfIntroList.size()) {
            selfIntroList.remove(position);
            notifyItemRemoved(position);
        }
    }
}