
package com.example.aiselfintroduction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiselfintroduction.R;

import java.util.List;

public class SelfIntroAdapter extends RecyclerView.Adapter<SelfIntroAdapter.ViewHolder> {

    private List<String> items;

    public SelfIntroAdapter(List<String> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_self_intro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView star, file, menu;
        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleText);
            star = itemView.findViewById(R.id.starIcon);
            file = itemView.findViewById(R.id.fileIcon);
            menu = itemView.findViewById(R.id.menuIcon);
        }
    }
}
