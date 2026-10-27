package com.mottainai.cliente.adapters;

import android.view.LayoutInflater;
import android.location.Location;
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
    private Double latitude;
    private Double longitude;

    public StoreAdapter(OnStoreClickListener listener) {
        this.listener = listener;
    }

    public void setStores(List<PartnerStore> newStores) {
        stores.clear();
        stores.addAll(newStores);
        notifyDataSetChanged();
    }

    public void setOrigin(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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
        String address = store.getAddressLabel().isEmpty() ? "Endereço indisponível" : store.getAddressLabel();
        if (latitude != null && longitude != null && store.hasValidLocation()) {
            float[] distance = new float[1];
            Location.distanceBetween(latitude, longitude, store.getLatitude(), store.getLongitude(), distance);
            address = String.format(Locale.forLanguageTag("pt-BR"), "Aprox. %.1f km · %s", distance[0] / 1000, address);
        }
        holder.meta.setText(address);
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
