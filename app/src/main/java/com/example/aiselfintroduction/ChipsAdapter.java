package com.example.aiselfintroduction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiselfintroduction.R;

import java.util.List;

public class ChipsAdapter extends RecyclerView.Adapter<ChipsAdapter.ChipViewHolder> {
    private List<String> items;
    private OnChipRemoveListener listener;

    public interface OnChipRemoveListener {
        void onChipRemove(int position);
    }

    public ChipsAdapter(List<String> items, OnChipRemoveListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chip, parent, false);
        return new ChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ChipViewHolder extends RecyclerView.ViewHolder {
        TextView chipText;
        TextView deleteButton;

        ChipViewHolder(@NonNull View itemView) {
            super(itemView);
            chipText = itemView.findViewById(R.id.chipText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(String text, final int position) {
            chipText.setText(text);
            deleteButton.setOnClickListener(v -> listener.onChipRemove(position));
        }
    }
}