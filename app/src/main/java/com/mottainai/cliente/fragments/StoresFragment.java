package com.mottainai.cliente.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.StoreAdapter;
import com.mottainai.cliente.repository.MockStoreRepository;
import com.mottainai.cliente.widgets.PartnerMapView;

public class StoresFragment extends Fragment {

    private final MockStoreRepository storeRepository = new MockStoreRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stores, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PartnerMapView mapView = view.findViewById(R.id.map_stores);
        mapView.setStores(storeRepository.getNearbyStores());

        RecyclerView recyclerView = view.findViewById(R.id.rv_stores);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        StoreAdapter storeAdapter = new StoreAdapter(store ->
                Toast.makeText(requireContext(), store.getName(), Toast.LENGTH_SHORT).show());
        recyclerView.setAdapter(storeAdapter);
        storeAdapter.setStores(storeRepository.getNearbyStores());
    }
}
