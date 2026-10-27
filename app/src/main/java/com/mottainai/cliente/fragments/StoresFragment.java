package com.mottainai.cliente.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.StoreAdapter;
import com.mottainai.cliente.models.PartnerStore;
import com.mottainai.cliente.models.OfferRadius;
import com.mottainai.cliente.network.dto.CatalogStore;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.viewmodel.CatalogViewModel;
import com.mottainai.cliente.widgets.PartnerMapView;
import com.mottainai.cliente.utils.MapNavigation;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class StoresFragment extends Fragment {

    private final List<PartnerStore> stores = new ArrayList<>();
    private CatalogViewModel viewModel;
    private StoreAdapter storeAdapter;
    private PartnerMapView mapView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stores, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CatalogViewModel.class);
        if (savedInstanceState != null) {
            try {
                viewModel.useStoreRadius(OfferRadius.fromKilometers(savedInstanceState.getInt(
                        "stores_radius_km", OfferRadius.DEFAULT.kilometers())));
            } catch (IllegalArgumentException invalidSavedRadius) {
                viewModel.useStoreRadius(OfferRadius.DEFAULT);
            }
        }
        mapView = view.findViewById(R.id.map_stores);
        mapView.createMap(savedInstanceState);
        mapView.setOnStoreSelected(store -> showStore(view, store));
        view.findViewById(R.id.stores_use_location).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Encontrar lojas próximas")
                        .setMessage(getString(R.string.stores_location_explanation, viewModel.storeRadius().kilometers()))
                        .setNegativeButton("Agora não", null)
                        .setPositiveButton("Continuar", (dialog, which) -> viewModel.requestLocation()).show());
        view.findViewById(R.id.stores_clear_location).setOnClickListener(v -> chooseRadius());
        RecyclerView recyclerView = view.findViewById(R.id.rv_stores);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        storeAdapter = new StoreAdapter(store -> showStore(view, store));
        recyclerView.setAdapter(storeAdapter);
        view.findViewById(R.id.stores_retry).setOnClickListener(v -> {
            if (viewModel.storeLatitude() == null) view.findViewById(R.id.stores_use_location).performClick();
            else loadPage(viewModel.storePage());
        });
        view.findViewById(R.id.stores_load_more).setOnClickListener(v -> loadPage(viewModel.nextStorePage()));
        viewModel.stores().observe(getViewLifecycleOwner(), state -> render(view, state));
        viewModel.location().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            view.findViewById(R.id.stores_use_location).setEnabled(!viewModel.isLocating());
            if (state.status == LoadState.Status.LOADING) locationStatus("Obtendo sua localização…");
            else if (state.status == LoadState.Status.ERROR) locationStatus(state.message);
        });
        if (viewModel.stores().getValue() == null) loadPage(0);
        updateRadiusLabel(view);
    }

    private void chooseRadius() {
        OfferRadius[] radii = OfferRadius.values();
        String[] labels = new String[radii.length];
        for (int index = 0; index < radii.length; index++) {
            labels[index] = getString(R.string.stores_radius_option, radii[index].kilometers());
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.stores_radius_title)
                .setSingleChoiceItems(labels, viewModel.storeRadius().ordinal(), (dialog, which) -> {
                    viewModel.useStoreRadius(radii[which]);
                    if (getView() != null) updateRadiusLabel(getView());
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateRadiusLabel(View view) {
        ((TextView) view.findViewById(R.id.stores_clear_location)).setText(
                getString(R.string.stores_radius_button, viewModel.storeRadius().kilometers()));
    }

    private void loadPage(int requestedPage) {
        viewModel.loadNearbyStores(requestedPage, null);
    }

    private void locationStatus(String message) {
        if (getView() != null) ((TextView) getView().findViewById(R.id.stores_location_status)).setText(message);
    }

    private void showStore(View view, PartnerStore store) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(store.getName())
                .setMessage(store.getAddressLabel())
                .setPositiveButton("Ver ofertas", (dialog, which) -> {
                    Bundle args = new Bundle();
                    args.putString("store_id", store.getId());
                    Navigation.findNavController(view).navigate(R.id.catalogFragment, args);
                })
                .setNeutralButton("Como chegar", (dialog, which) -> MapNavigation.openRoute(requireContext(), store))
                .setNegativeButton("Fechar", null)
                .show();
    }

    private void render(View view,
                        LoadState<com.mottainai.cliente.network.dto.CatalogPage<CatalogStore>> state) {
        if (state == null) return;
        TextView status = view.findViewById(R.id.stores_status);
        TextView mapLabel = view.findViewById(R.id.tv_nearest_store);
        View retry = view.findViewById(R.id.stores_retry);
        View more = view.findViewById(R.id.stores_load_more);
        stores.clear();
        if (state.data != null) {
            for (CatalogStore store : state.data.items()) stores.add(new PartnerStore(store));
        }
        storeAdapter.setOrigin(viewModel.storeLatitude(), viewModel.storeLongitude());
        storeAdapter.setStores(stores);
        updateRadiusLabel(view);
        if (viewModel.storeLatitude() != null) {
            locationStatus(getString(R.string.stores_radius_status, viewModel.storeRadius().kilometers()));
        }
        mapView.setUserPosition(viewModel.storeLatitude(), viewModel.storeLongitude());
        mapView.setStores(stores);
        long located = stores.stream().filter(PartnerStore::hasValidLocation).count();
        boolean showMap = located > 0 && mapView.isConfigured();
        mapView.setVisibility(showMap ? View.VISIBLE : View.GONE);
        mapLabel.setText(located == 0 ? "Sem localização no mapa"
                : !mapView.isConfigured() ? "Mapa indisponível no momento"
                : located + " lojas com localização");
        if (state.status == LoadState.Status.LOADING) {
            status.setText(viewModel.isLocating() ? "Obtendo sua localização…" : "Carregando lojas...");
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
        status.setText(stores.isEmpty() ? "Nenhuma loja disponível no momento."
                : stores.size() + " lojas encontradas. Endereços sem coordenadas aparecem na lista.");
        retry.setVisibility(View.GONE);
        more.setVisibility(state.data.number + 1 < state.data.totalPages ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.resumeMap();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.startMap();
    }

    @Override
    public void onPause() {
        if (mapView != null) mapView.pauseMap();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mapView != null) mapView.stopMap();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (viewModel != null) outState.putInt("stores_radius_km", viewModel.storeRadius().kilometers());
        if (mapView != null) mapView.saveMapState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.mapLowMemory();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) mapView.releaseMap();
        mapView = null;
        super.onDestroyView();
    }
}
