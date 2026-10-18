package com.mottainai.cliente.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private CatalogViewModel viewModel;
    private PartnerMapView mapPreview;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CatalogViewModel.class);
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
            viewModel.loadStores(0, null);
        });
        view.findViewById(R.id.home_offers_retry).setOnClickListener(v ->
                viewModel.loadPromotions(0, null, null));
        view.findViewById(R.id.home_stores_retry).setOnClickListener(v ->
                viewModel.loadStores(0, null));
        viewModel.promotions().observe(getViewLifecycleOwner(), state -> renderOffers(view, state));
        viewModel.stores().observe(getViewLifecycleOwner(), state -> renderStores(view, state));
        if (viewModel.promotions().getValue() == null) viewModel.loadPromotions(0, null, null);
        if (viewModel.stores().getValue() == null) viewModel.loadStores(0, null);
        if (profile.state().getValue().kind == ProfileViewModel.State.Kind.IDLE) profile.load();
        if (loyalty.account().getValue() == null) loyalty.loadAccount();
    }

    private void renderPoints(View view, LoadState<LoyaltyAccount> state) {
        if (state == null) return;
        ((TextView) view.findViewById(R.id.home_points_balance)).setText(
                state.data != null && state.data.pointsBalance != null
                        ? String.format(Locale.forLanguageTag("pt-BR"), "%,d pts", state.data.pointsBalance) : "— pts");
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
        LinearLayout featuredOffersLayout = view.findViewById(R.id.layout_featured_offers);
        featuredOffersLayout.removeAllViews();
        List<CatalogPromotion> featuredOffers = state.data == null
                ? java.util.Collections.emptyList() : state.data.items();
        int count = Math.min(2, featuredOffers.size());
        for (int i = 0; i < count; i++) {
            Offer offer = new Offer(featuredOffers.get(i));
            View card = getLayoutInflater().inflate(R.layout.item_offer, featuredOffersLayout, false);
            OfferAdapter.bind(card, offer);
            card.setOnClickListener(v -> openOfferDetail(view, offer));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i > 0) {
                params.setMarginStart(getResources().getDimensionPixelSize(R.dimen.offer_grid_spacing));
            }
            card.setLayoutParams(params);
            featuredOffersLayout.addView(card);
        }
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
        mapPreview.setStores(stores);
        long located = stores.stream().filter(PartnerStore::hasValidLocation).count();
        boolean showMap = located > 0 && mapPreview.isConfigured();
        mapPreview.setVisibility(showMap ? View.VISIBLE : View.GONE);
        status.setText(state.status == LoadState.Status.LOADING ? "Carregando lojas…"
                : state.status == LoadState.Status.ERROR ? state.message
                : stores.isEmpty() ? "Nenhuma loja disponível."
                : located == 0 ? "Lojas sem coordenadas. Consulte a lista."
                : !mapPreview.isConfigured() ? "Mapa indisponível. Consulte a lista."
                : located + " lojas com localização");
        retry.setVisibility(state.status == LoadState.Status.ERROR ? View.VISIBLE : View.GONE);
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
