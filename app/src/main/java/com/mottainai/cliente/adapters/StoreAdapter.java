package com.mottainai.cliente.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.models.PartnerStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    public interface OnStoreClickListener {
        void onStoreClick(PartnerStore store);
    }

    private final List<PartnerStore> stores = new ArrayList<>();
    private final OnStoreClickListener listener;

    public StoreAdapter(OnStoreClickListener listener) {
        this.listener = listener;
    }

    public void setStores(List<PartnerStore> newStores) {
        stores.clear();
        stores.addAll(newStores);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        PartnerStore store = stores.get(position);
        holder.name.setText(store.getName());
        String hours = store.isOpenNow() ? "aberto agora" : store.getHoursLabel();
        holder.meta.setText(String.format(Locale.getDefault(), "%.1f km · %s", store.getDistanceKm(), hours));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStoreClick(store);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;

        StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_store_name);
            meta = itemView.findViewById(R.id.tv_store_meta);
        }
    }
}
