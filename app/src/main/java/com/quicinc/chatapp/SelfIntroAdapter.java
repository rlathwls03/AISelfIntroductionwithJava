package com.quicinc.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quicinc.chatapp.R;
import com.quicinc.chatapp.SelfIntro;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    // 이름 변경 인터페이스
    public interface OnItemRenameListener {
        void onItemRename(String oldName, String newName);
    }

    private OnItemRenameListener renameListener;

    // 아이템 클릭 인터페이스 추가
    public interface OnItemClickListener {
        void onItemClick(String title);
    }

    private OnItemClickListener clickListener;

    public SelfIntroAdapter(Context context, List<SelfIntro> list) {
        this.context = context;
        this.selfIntroList = list;
        this.prefs = context.getSharedPreferences("IntroPrefs", Context.MODE_PRIVATE);
    }
    public void setOnItemRenameListener(OnItemRenameListener listener) {
        this.renameListener = listener;
    }

    // 아이템 클릭 리스너 설정 메서드 추가
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }


    public interface OnDownloadClickListener {
        void onDownloadClicked(String introTitle);
    }

    private OnDownloadClickListener downloadClickListener;

    public void setOnDownloadClickListener(OnDownloadClickListener listener) {
        this.downloadClickListener = listener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView starIcon, moreIcon, arrowIcon, documentIcon;
        TextView titleText;
        EditText editText;
        private boolean isEditing = false;

        public ViewHolder(View view) {
            super(view);
            starIcon = view.findViewById(R.id.starIcon);
            moreIcon = view.findViewById(R.id.moreIcon);
            titleText = view.findViewById(R.id.titleText);
            documentIcon = view.findViewById(R.id.documentIcon);

            // EditText를 동적으로 생성하여 titleText와 같은 위치에 배치
            ViewGroup parent = (ViewGroup) titleText.getParent();
            editText = new EditText(view.getContext());
            editText.setLayoutParams(titleText.getLayoutParams());
            editText.setTextSize(16);
            editText.setTextColor(titleText.getCurrentTextColor());
            editText.setBackgroundResource(android.R.color.transparent);
            editText.setSingleLine(true);
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            editText.setVisibility(View.GONE);
            parent.addView(editText);
        }

        public void startEditing(String currentTitle, SelfIntroAdapter adapter) {
            isEditing = true;

            // TextView 숨기고 EditText 보이기
            titleText.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            editText.setText(currentTitle);
            editText.selectAll();
            editText.requestFocus();

            // 키보드 표시
            InputMethodManager imm = (InputMethodManager) itemView.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

            // Enter 키 또는 포커스 잃을 때 편집 완료
            editText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    finishEditing(adapter);
                    return true;
                }
                return false;
            });

            editText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && isEditing) {
                    finishEditing(adapter);
                }
            });
        }

        public void finishEditing(SelfIntroAdapter adapter) {
            if (!isEditing) return;

            isEditing = false;
            String newName = editText.getText().toString().trim();
            String oldName = titleText.getText().toString();

            // 키보드 숨기기
            InputMethodManager imm = (InputMethodManager) itemView.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

            // EditText 숨기고 TextView 보이기
            editText.setVisibility(View.GONE);
            titleText.setVisibility(View.VISIBLE);

            // 이름이 변경되었고 비어있지 않다면 콜백 호출
            if (!newName.isEmpty() && !newName.equals(oldName)) {
                if (adapter.renameListener != null) {
                    adapter.renameListener.onItemRename(oldName, newName);
                }
            }
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

        // 제목 클릭 시 상세 화면으로 이동 (편집 중이 아닐 때만)
        holder.titleText.setOnClickListener(v -> {
            if (!holder.isEditing) {
                String title = item.getTitle();

                Intent intent = new Intent(context, EditListActivityFromHome.class);
                intent.putExtra("resumeTitle", title);
                context.startActivity(intent);

                // 최근 편집 항목으로 저장
                prefs.edit().putString(HomeActivity.LAST_EDITED_KEY, item.getTitle()).apply();
            }
        });

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


            // 아이콘 표시 활성화 (중요)
            try {
                Field field = PopupMenu.class.getDeclaredField("mPopup");
                field.setAccessible(true);
                Object menuPopupHelper = field.get(popup);

                Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                setForceShowIcon.invoke(menuPopupHelper, true);

                // 오른쪽 여백을 줄이기 위한 오프셋 설정
                Method method = classPopupHelper.getMethod("show", int.class, int.class);
                method.invoke(menuPopupHelper, -30, 0); // x좌표를 왼쪽으로 30dp 이동
            } catch (Exception e) {
                e.printStackTrace();
            }

// 팝업 메뉴 표시
            popup.show();

            popup.setOnMenuItemClickListener(menuItem -> {
                int id = menuItem.getItemId();
                if (id == R.id.menu_rename) { // 이름 변경
                    // 이름 변경 - ic_more_vert를 누르면 이름 편집 모드 시작
                    holder.startEditing(item.getTitle(), this);
                    return true;
                } else if (id == R.id.menu_delete) { //  삭제
                    // 삭제 시 즐겨찾기에서도 제거
                    Set<String> currentFavorites = new HashSet<>(
                            prefs.getStringSet(FAVORITES_KEY, new HashSet<>())
                    );
                    currentFavorites.remove(item.getTitle());
                    prefs.edit().putStringSet(FAVORITES_KEY, currentFavorites).commit();

                    selfIntroList.remove(position);
                    notifyItemRemoved(position);
                    // 삭제 - 저장소에서도 제거하도록 수정
                    deleteIntroItem(item, position);
                    return true;
                } else if (id == R.id.menu_download) {
                    if (downloadClickListener != null) {
                        downloadClickListener.onDownloadClicked(item.getTitle());
                    } else {
                        // 리스너가 없는 경우 기존 방식으로 분기 처리
                        if (context instanceof HomeActivity) {
                            ((HomeActivity) context).navigateToDownload(item.getTitle());
                        } else if (context instanceof FavoritesActivity) {
                            ((FavoritesActivity) context).navigateToDownload(item.getTitle());
                        }
                    }
                    return true;
                }

                return false;
            });
            popup.show();
        });
    }

    // 삭제 처리를 위한 새로운 메서드 추가 (SelfIntroAdapter 클래스 내부)
    private void deleteIntroItem(SelfIntro item, int position) {
        try {
            // 1. 저장소에서 삭제
            if (context instanceof HomeActivity) {
                ((HomeActivity) context).deleteSelfIntroFromStorage(item.getTitle());
            } else if (context instanceof FavoritesActivity) {
                ((FavoritesActivity) context).deleteSelfIntroFromStorage(item.getTitle());
            }

            // 2. 즐겨찾기에서도 제거
            Set<String> currentFavorites = new HashSet<>(
                    prefs.getStringSet(FAVORITES_KEY, new HashSet<>())
            );
            currentFavorites.remove(item.getTitle());
            prefs.edit().putStringSet(FAVORITES_KEY, currentFavorites).commit();

            // 3. UI에서 제거
            selfIntroList.remove(position);
            notifyItemRemoved(position);

            Log.d("SelfIntroAdapter", "자기소개서 삭제 완료: " + item.getTitle());

        } catch (Exception e) {
            Log.e("SelfIntroAdapter", "자기소개서 삭제 실패", e);
            e.printStackTrace();
        }
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