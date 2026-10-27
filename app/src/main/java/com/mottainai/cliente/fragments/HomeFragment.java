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
import com.mottainai.cliente.adapters.OfferAdapter;
import com.mottainai.cliente.models.Offer;
import com.mottainai.cliente.models.PartnerStore;
import com.mottainai.cliente.network.dto.CatalogPromotion;
import com.mottainai.cliente.network.dto.CatalogStore;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.viewmodel.CatalogViewModel;
import com.mottainai.cliente.viewmodel.ProfileViewModel;
import com.mottainai.cliente.viewmodel.LoyaltyViewModel;
import com.mottainai.cliente.network.dto.LoyaltyAccount;
import com.mottainai.cliente.widgets.PartnerMapView;
import com.mottainai.cliente.utils.LoyaltyPresentation;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private CatalogViewModel viewModel;
    private PartnerMapView mapPreview;
    private OfferAdapter featuredAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CatalogViewModel.class);
        RecyclerView featured = view.findViewById(R.id.layout_featured_offers);
        featured.setLayoutManager(new LinearLayoutManager(requireContext()));
        featuredAdapter = new OfferAdapter(offer -> openOfferDetail(view, offer), true);
        featured.setAdapter(featuredAdapter);
        view.findViewById(R.id.home_open_catalog).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.catalogFragment));
        mapPreview = view.findViewById(R.id.map_preview);
        mapPreview.createMap(savedInstanceState);
        ProfileViewModel profile = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        LoyaltyViewModel loyalty = new ViewModelProvider(requireActivity()).get(LoyaltyViewModel.class);
        profile.state().observe(getViewLifecycleOwner(), current -> {
            TextView greeting = view.findViewById(R.id.tv_greeting);
            if (current.profile != null && current.profile.fullName != null
                    && !current.profile.fullName.trim().isEmpty()) {
                greeting.setText("Olá, " + current.profile.fullName.trim().split("\\s+", 2)[0] + "!");
            } else greeting.setText("Olá!");
            ((TextView) view.findViewById(R.id.home_profile_status)).setText(
                    current.kind == ProfileViewModel.State.Kind.LOADING ? "Carregando seu perfil…"
                            : current.kind == ProfileViewModel.State.Kind.ERROR ? current.message : "");
            view.findViewById(R.id.home_profile_retry).setVisibility(
                    current.kind == ProfileViewModel.State.Kind.ERROR ? View.VISIBLE : View.GONE);
        });
        loyalty.account().observe(getViewLifecycleOwner(), current -> renderPoints(view, current));
        view.findViewById(R.id.home_profile_retry).setOnClickListener(v -> profile.load());
        view.findViewById(R.id.home_points_retry).setOnClickListener(v -> loyalty.loadAccount());
        view.findViewById(R.id.home_open_points).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.pointsFragment));
        view.findViewById(R.id.home_open_stores).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.storesFragment));
        view.findViewById(R.id.home_refresh).setOnClickListener(v -> {
            profile.load();
            loyalty.loadAccount();
            viewModel.loadPromotions(0, null, null);
            viewModel.requestLocation();
        });
        view.findViewById(R.id.home_offers_retry).setOnClickListener(v ->
                viewModel.loadPromotions(0, null, null));
        view.findViewById(R.id.home_stores_retry).setOnClickListener(v -> {
            if (viewModel.storeLatitude() == null) viewModel.requestLocation();
            else viewModel.loadNearbyStores(0, null);
        });
        viewModel.promotions().observe(getViewLifecycleOwner(), state -> renderOffers(view, state));
        viewModel.stores().observe(getViewLifecycleOwner(), state -> renderStores(view, state));
        if (viewModel.promotions().getValue() == null) viewModel.loadPromotions(0, null, null);
        if (viewModel.stores().getValue() == null) viewModel.loadNearbyStores(0, null);
        if (profile.state().getValue().kind == ProfileViewModel.State.Kind.IDLE) profile.load();
        if (loyalty.account().getValue() == null) loyalty.loadAccount();
    }

    private void renderPoints(View view, LoadState<LoyaltyAccount> state) {
        if (state == null) return;
        ((TextView) view.findViewById(R.id.home_points_balance)).setText(
                LoyaltyPresentation.points(state.data == null ? null : state.data.pointsBalance));
        ((TextView) view.findViewById(R.id.home_points_status)).setText(
                state.status == LoadState.Status.LOADING ? "Carregando saldo…"
                        : state.status == LoadState.Status.ERROR ? state.message
                        : state.data == null || state.data.pointsBalance == null ? "Saldo indisponível." : "");
        view.findViewById(R.id.home_points_retry).setVisibility(
                state.status == LoadState.Status.ERROR ? View.VISIBLE : View.GONE);
    }

    private void renderOffers(View view,
                              LoadState<com.mottainai.cliente.network.dto.CatalogPage<CatalogPromotion>> state) {
        if (state == null) return;
        TextView status = view.findViewById(R.id.home_offers_status);
        View retry = view.findViewById(R.id.home_offers_retry);
        List<CatalogPromotion> featuredOffers = state.data == null
                ? java.util.Collections.emptyList() : state.data.items();
        int count = Math.min(2, featuredOffers.size());
        List<Offer> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Offer(featuredOffers.get(i)));
        }
        featuredAdapter.setOffers(cards);
        status.setText(state.status == LoadState.Status.LOADING ? "Carregando ofertas…"
                : state.status == LoadState.Status.ERROR ? state.message
                : count == 0 ? "Nenhuma oferta disponível no momento." : "");
        retry.setVisibility(state.status == LoadState.Status.ERROR ? View.VISIBLE : View.GONE);
    }

    private void renderStores(View view,
                              LoadState<com.mottainai.cliente.network.dto.CatalogPage<CatalogStore>> state) {
        if (state == null) return;
        TextView status = view.findViewById(R.id.tv_nearest_store);
        View retry = view.findViewById(R.id.home_stores_retry);
        List<PartnerStore> stores = new ArrayList<>();
        if (state.data != null) {
            for (CatalogStore store : state.data.items()) stores.add(new PartnerStore(store));
        }
        mapPreview.setUserPosition(viewModel.storeLatitude(), viewModel.storeLongitude());
        mapPreview.setStores(stores);
        long located = stores.stream().filter(PartnerStore::hasValidLocation).count();
        boolean showMap = located > 0 && mapPreview.isConfigured();
        mapPreview.setVisibility(showMap ? View.VISIBLE : View.GONE);
        status.setText(state.status == LoadState.Status.LOADING
                ? viewModel.isLocating() ? "Obtendo sua localização…" : "Carregando lojas…"
                : state.status == LoadState.Status.ERROR ? state.message
                : stores.isEmpty() ? "Nenhuma loja disponível."
                : located == 0 ? "Lojas sem coordenadas. Consulte a lista."
                : !mapPreview.isConfigured() ? "Mapa indisponível. Consulte a lista."
                : located + " lojas com localização");
        retry.setVisibility(state.status == LoadState.Status.ERROR ? View.VISIBLE : View.GONE);
        ((TextView) retry).setText(viewModel.storeLatitude() == null
                ? "Atualizar localização" : "Tentar novamente");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapPreview != null) mapPreview.resumeMap();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapPreview != null) mapPreview.startMap();
    }

    @Override
    public void onPause() {
        if (mapPreview != null) mapPreview.pauseMap();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mapPreview != null) mapPreview.stopMap();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mapPreview != null) mapPreview.saveMapState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapPreview != null) mapPreview.mapLowMemory();
    }

    @Override
    public void onDestroyView() {
        if (getView() != null) {
            ((RecyclerView) getView().findViewById(R.id.layout_featured_offers)).setAdapter(null);
        }
        featuredAdapter = null;
        if (mapPreview != null) mapPreview.releaseMap();
        mapPreview = null;
        super.onDestroyView();
    }

    private void openOfferDetail(View view, Offer offer) {
        Bundle args = new Bundle();
        args.putString("offer_id", offer.getId());
        Navigation.findNavController(view).navigate(R.id.offerDetailActivity, args);
    }
}
