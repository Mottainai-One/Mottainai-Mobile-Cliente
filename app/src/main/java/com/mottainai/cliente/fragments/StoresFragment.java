package com.mottainai.cliente.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.StoreAdapter;
import com.mottainai.cliente.models.PartnerStore;
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
    private CancellationSignal locationRequest;
    private final Handler locationHandler = new Handler(Looper.getMainLooper());
    private final ActivityResultLauncher<String[]> locationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                if (getView() == null) return;
                if (hasLocationPermission()) requestLocation();
                else locationStatus("Localização não autorizada. Você pode continuar pela lista de lojas.");
            });

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
        mapView.createMap(savedInstanceState);
        mapView.setOnStoreSelected(store -> showStore(view, store));
        view.findViewById(R.id.stores_use_location).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Encontrar lojas próximas")
                        .setMessage("Usaremos sua localização para buscar lojas em até 5 km. É opcional e você pode continuar pela lista geral.")
                        .setNegativeButton("Agora não", null)
                        .setPositiveButton("Continuar", (dialog, which) -> {
                            if (hasLocationPermission()) requestLocation();
                            else locationPermission.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION});
                        }).show());
        view.findViewById(R.id.stores_clear_location).setOnClickListener(v -> {
            stopLocationRequest();
            viewModel.useStoreLocation(null, null);
            locationStatus("Mostrando todas as lojas, inclusive as que ainda não têm coordenadas.");
        });
        RecyclerView recyclerView = view.findViewById(R.id.rv_stores);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        storeAdapter = new StoreAdapter(store -> showStore(view, store));
        recyclerView.setAdapter(storeAdapter);
        view.findViewById(R.id.stores_retry).setOnClickListener(v -> loadPage(viewModel.storePage()));
        view.findViewById(R.id.stores_load_more).setOnClickListener(v -> loadPage(viewModel.nextStorePage()));
        viewModel.stores().observe(getViewLifecycleOwner(), state -> render(view, state));
        if (viewModel.stores().getValue() == null) loadPage(0);
    }

    private void loadPage(int requestedPage) {
        viewModel.loadStores(requestedPage, null);
    }

    private boolean hasLocationPermission() {
        return getContext() != null && (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestLocation() {
        if (getView() == null || !hasLocationPermission()) return;
        stopLocationRequest();
        LocationManager manager = requireContext().getSystemService(LocationManager.class);
        if (manager == null || !LocationManagerCompat.isLocationEnabled(manager)) {
            locationStatus("Ative a localização do aparelho e tente novamente. A lista geral continua disponível.");
            return;
        }
        String provider = manager.getProviders(true).contains(LocationManager.NETWORK_PROVIDER)
                ? LocationManager.NETWORK_PROVIDER : LocationManager.GPS_PROVIDER;
        CancellationSignal request = new CancellationSignal();
        locationRequest = request;
        getView().findViewById(R.id.stores_use_location).setEnabled(false);
        locationStatus("Obtendo sua localização…");
        try {
            LocationManagerCompat.getCurrentLocation(manager, provider, request,
                    ContextCompat.getMainExecutor(requireContext()), location -> {
                        if (locationRequest != request || getView() == null) return;
                        stopLocationRequest();
                        if (location == null || !hasLocationPermission()) {
                            locationStatus("Não foi possível obter sua localização. Tente novamente ou use a lista geral.");
                            return;
                        }
                        viewModel.useStoreLocation(location.getLatitude(), location.getLongitude());
                    });
            locationHandler.postDelayed(() -> {
                if (locationRequest != request) return;
                stopLocationRequest();
                locationStatus("A localização demorou a responder. Tente novamente ou use a lista geral.");
            }, 15000);
        } catch (SecurityException | IllegalArgumentException exception) {
            stopLocationRequest();
            locationStatus("Localização indisponível. Confira a permissão e os serviços do aparelho.");
        }
    }

    private void locationStatus(String message) {
        if (getView() != null) ((TextView) getView().findViewById(R.id.stores_location_status)).setText(message);
    }

    private void stopLocationRequest() {
        if (locationRequest != null) locationRequest.cancel();
        locationRequest = null;
        locationHandler.removeCallbacksAndMessages(null);
        if (getView() != null) getView().findViewById(R.id.stores_use_location).setEnabled(true);
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
        view.findViewById(R.id.stores_clear_location).setVisibility(
                viewModel.storeLatitude() == null ? View.GONE : View.VISIBLE);
        if (viewModel.storeLatitude() != null) {
            locationStatus("Lojas em até 5 km. Distâncias aproximadas em linha reta.");
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
        status.setText(stores.isEmpty() ? "Nenhuma loja disponível no momento."
                : stores.size() + " lojas encontradas. Endereços sem coordenadas aparecem na lista.");
        retry.setVisibility(View.GONE);
        more.setVisibility(state.data.number + 1 < state.data.totalPages ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel.storeLatitude() != null && !hasLocationPermission()) {
            viewModel.useStoreLocation(null, null);
            locationStatus("Permissão de localização desativada. Mostrando a lista geral.");
        }
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
        if (locationRequest != null) {
            stopLocationRequest();
            locationStatus("Busca de localização pausada. Toque em buscar para tentar novamente.");
        }
        if (mapView != null) mapView.stopMap();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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
        stopLocationRequest();
        if (mapView != null) mapView.releaseMap();
        mapView = null;
        super.onDestroyView();
    }
}
