package com.yunxin.midnighttarotai.learnspreads;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yunxin.midnighttarotai.R;

import java.util.List;

public class SpreadAdapter extends RecyclerView.Adapter<SpreadAdapter.SpreadViewHolder> {
    private List<SpreadModel> spreads;
    private Context context;
    private OnSpreadClickListener listener;

    public interface OnSpreadClickListener {
        void onSpreadClick(SpreadModel spread);
    }

    public SpreadAdapter(Context context, List<SpreadModel> spreads, OnSpreadClickListener listener) {
        this.context = context;
        this.spreads = spreads;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpreadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_spread_grid, parent, false);
        return new SpreadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpreadViewHolder holder, int position) {
        SpreadModel spread = spreads.get(position);
        holder.spreadName.setText(spread.getName());
        holder.spreadPreview.setImageResource(spread.getPreviewImageRes());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSpreadClick(spread);
            }
        });
    }

    @Override
    public int getItemCount() {
        return spreads.size();
    }

    static class SpreadViewHolder extends RecyclerView.ViewHolder {
        ImageView spreadPreview;
        TextView spreadName;

        public SpreadViewHolder(@NonNull View itemView) {
            super(itemView);
            spreadPreview = itemView.findViewById(R.id.spreadPreview);
            spreadName = itemView.findViewById(R.id.spreadName);
        }
    }
}
