package com.mottainai.cliente.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.StoreAdapter;
import com.mottainai.cliente.models.PartnerStore;
import com.mottainai.cliente.network.dto.CatalogStore;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.viewmodel.CatalogViewModel;
import com.mottainai.cliente.widgets.PartnerMapView;

import java.util.ArrayList;
import java.util.List;

public class StoresFragment extends Fragment {

    private final List<PartnerStore> stores = new ArrayList<>();
    private CatalogViewModel viewModel;
    private StoreAdapter storeAdapter;
    private PartnerMapView mapView;
    private int page;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stores, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CatalogViewModel.class);
        mapView = view.findViewById(R.id.map_stores);
        RecyclerView recyclerView = view.findViewById(R.id.rv_stores);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        storeAdapter = new StoreAdapter(store ->
                Toast.makeText(requireContext(), store.getAddressLabel(), Toast.LENGTH_LONG).show());
        recyclerView.setAdapter(storeAdapter);
        view.findViewById(R.id.stores_retry).setOnClickListener(v -> loadPage(page));
        view.findViewById(R.id.stores_load_more).setOnClickListener(v -> loadPage(page + 1));
        viewModel.stores().observe(getViewLifecycleOwner(), state -> render(view, state));
        loadPage(0);
    }

    private void loadPage(int requestedPage) {
        if (requestedPage == 0) {
            stores.clear();
            storeAdapter.setStores(stores);
            mapView.setStores(stores);
            mapView.setVisibility(View.GONE);
        }
        page = requestedPage;
        viewModel.loadStores(requestedPage, null);
    }

    private void render(View view,
                        LoadState<com.mottainai.cliente.network.dto.CatalogPage<CatalogStore>> state) {
        if (state == null) return;
        TextView status = view.findViewById(R.id.stores_status);
        TextView mapLabel = view.findViewById(R.id.tv_nearest_store);
        View retry = view.findViewById(R.id.stores_retry);
        View more = view.findViewById(R.id.stores_load_more);
        if (state.status == LoadState.Status.LOADING) {
            status.setText("Carregando lojas...");
            retry.setVisibility(View.GONE);
            more.setVisibility(View.GONE);
            return;
        }
        if (state.status == LoadState.Status.ERROR) {
            status.setText(state.message);
            retry.setVisibility(View.VISIBLE);
            more.setVisibility(View.GONE);
            if (stores.isEmpty()) mapView.setVisibility(View.GONE);
            return;
        }
        for (CatalogStore store : state.data.items()) stores.add(new PartnerStore(store));
        storeAdapter.setStores(stores);
        mapView.setStores(stores);
        long located = stores.stream().filter(PartnerStore::hasValidLocation).count();
        mapView.setVisibility(located == 0 ? View.GONE : View.VISIBLE);
        mapLabel.setText(located == 0 ? "Sem localização no mapa"
                : located + " lojas com localização");
        status.setText(stores.isEmpty() ? "Nenhuma loja disponível no momento."
                : stores.size() + " lojas encontradas. Endereços sem coordenadas aparecem na lista.");
        retry.setVisibility(View.GONE);
        more.setVisibility(page + 1 < state.data.totalPages ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.resumeMap();
    }

    @Override
    public void onPause() {
        if (mapView != null) mapView.pauseMap();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) mapView.releaseMap();
        mapView = null;
        super.onDestroyView();
    }
}
