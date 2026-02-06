package com.josegarcia.appgym.ui.setup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.Split;

import java.util.List;

public class SplitAdapter extends RecyclerView.Adapter<SplitAdapter.ViewHolder> {

    private final List<Split> splits;
    private final OnSplitClickListener listener;
    private OnSplitDeleteListener deleteListener;

    public interface OnSplitClickListener {
        void onSplitClick(Split split);
    }

    public interface OnSplitDeleteListener {
        void onSplitDelete(Split split);
    }

    public SplitAdapter(List<Split> splits, OnSplitClickListener listener) {
        this(splits, listener, null);
    }

    public SplitAdapter(List<Split> splits, OnSplitClickListener listener, OnSplitDeleteListener deleteListener) {
        this.splits = splits;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_split_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(splits.get(position));
    }

    @Override
    public int getItemCount() {
        return splits.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, desc, activeLabel;
        android.widget.ImageButton deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvSplitName);
            desc = itemView.findViewById(R.id.tvSplitDesc);
            activeLabel = itemView.findViewById(R.id.tvActiveLabel);
            deleteBtn = itemView.findViewById(R.id.btnDeleteSplit);

            itemView.setOnClickListener(v -> listener.onSplitClick(splits.get(getAdapterPosition())));

            deleteBtn.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onSplitDelete(splits.get(getAdapterPosition()));
                }
            });
        }

        void bind(Split split) {
            name.setText(split.name);
            desc.setText(split.description);
            activeLabel.setVisibility(split.isActive ? View.VISIBLE : View.GONE);

            // Only show delete if delete listener is provided AND split is user-created (not template)
            // But List only contains user splits in MyPlans.
            // Templates in Classic list -> no deleteListener passed.
            if (deleteListener != null) {
                deleteBtn.setVisibility(View.VISIBLE);
            } else {
                deleteBtn.setVisibility(View.GONE);
            }
        }
    }
}
