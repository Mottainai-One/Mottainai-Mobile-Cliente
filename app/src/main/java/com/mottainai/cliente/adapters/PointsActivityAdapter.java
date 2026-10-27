package com.mottainai.cliente.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.models.PointsActivityEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PointsActivityAdapter extends RecyclerView.Adapter<PointsActivityAdapter.EntryViewHolder> {

    private final List<PointsActivityEntry> entries = new ArrayList<>();

    public void setEntries(List<PointsActivityEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_points_activity, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        PointsActivityEntry entry = entries.get(position);
        holder.label.setText(entry.getLabel());

        boolean isPositive = entry.getPointsDelta() >= 0;
        String sign = isPositive ? "+" : "-";
        holder.points.setText(String.format(Locale.getDefault(), "%s %d pts", sign, Math.abs(entry.getPointsDelta())));
        holder.points.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                isPositive ? R.color.primary_green : R.color.accent_red));

        holder.row.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), entry.getColorRes())));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {
        final View row;
        final TextView label;
        final TextView points;

        EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.layout_activity_row);
            label = itemView.findViewById(R.id.tv_activity_label);
            points = itemView.findViewById(R.id.tv_activity_points);
        }
    }
}
