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
import com.mottainai.cliente.network.dto.CustomerProfileResponse;
import com.mottainai.cliente.network.dto.LoyaltyAccount;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.utils.SessionManager;
import com.mottainai.cliente.viewmodel.CatalogViewModel;
import com.mottainai.cliente.viewmodel.HomeViewModel;
import com.mottainai.cliente.widgets.PartnerMapView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private CatalogViewModel viewModel;
    private HomeViewModel homeViewModel;
    private PartnerMapView mapPreview;
    private String clientId;

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
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        clientId = new SessionManager(requireContext()).getClientId();
        mapPreview = view.findViewById(R.id.map_preview);
        view.findViewById(R.id.home_profile_retry).setOnClickListener(v -> homeViewModel.loadProfile());
        view.findViewById(R.id.home_points_retry).setOnClickListener(v -> homeViewModel.loadPoints(clientId));
        view.findViewById(R.id.home_points_open).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.pointsFragment));
        view.findViewById(R.id.home_offers_retry).setOnClickListener(v ->
                viewModel.loadPromotions(0, null, null));
        view.findViewById(R.id.home_stores_retry).setOnClickListener(v ->
                viewModel.loadStores(0, null));
        view.findViewById(R.id.home_stores_open).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.storesFragment));
        viewModel.promotions().observe(getViewLifecycleOwner(), state -> renderOffers(view, state));
        viewModel.stores().observe(getViewLifecycleOwner(), state -> renderStores(view, state));
        homeViewModel.profile().observe(getViewLifecycleOwner(), state -> renderProfile(view, state));
        homeViewModel.points().observe(getViewLifecycleOwner(), state -> renderPoints(view, state));
        homeViewModel.loadIfNeeded(clientId);
        viewModel.loadPromotions(0, null, null);
        viewModel.loadStores(0, null);
    }

    private void renderProfile(View view, LoadState<CustomerProfileResponse> state) {
        if (state == null) return;
        TextView greeting = view.findViewById(R.id.tv_greeting);
        TextView status = view.findViewById(R.id.home_profile_status);
        View retry = view.findViewById(R.id.home_profile_retry);
        if (state.status == LoadState.Status.LOADING) {
            status.setText("Carregando seu perfil...");
            status.setVisibility(View.VISIBLE);
            retry.setVisibility(View.GONE);
        } else if (state.status == LoadState.Status.ERROR) {
            greeting.setText("Olá!");
            status.setText(state.message);
            status.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
        } else {
            String fullName = state.data.fullName == null ? "" : state.data.fullName.trim();
            String firstName = fullName.isEmpty() ? "" : fullName.split("\\s+", 2)[0];
            greeting.setText(firstName.isEmpty() ? "Olá!" : "Olá, " + firstName + "!");
            status.setVisibility(View.GONE);
            retry.setVisibility(View.GONE);
        }
    }

    private void renderPoints(View view, LoadState<LoyaltyAccount> state) {
        if (state == null) return;
        TextView balance = view.findViewById(R.id.tv_home_points);
        TextView status = view.findViewById(R.id.home_points_status);
        View retry = view.findViewById(R.id.home_points_retry);
        if (state.status == LoadState.Status.LOADING) {
            balance.setText("— pts");
            status.setText("Carregando saldo...");
            retry.setVisibility(View.GONE);
        } else if (state.status == LoadState.Status.ERROR) {
            balance.setText("— pts");
            status.setText(state.message);
            retry.setVisibility(View.VISIBLE);
        } else if (state.data.pointsBalance == null) {
            balance.setText("— pts");
            status.setText("Saldo indisponível no momento.");
            retry.setVisibility(View.GONE);
        } else {
            balance.setText(String.format(Locale.forLanguageTag("pt-BR"),
                    "%,d pts", state.data.pointsBalance));
            status.setText("");
            retry.setVisibility(View.GONE);
        }
    }

    private void renderOffers(View view,
                              LoadState<com.mottainai.cliente.network.dto.CatalogPage<CatalogPromotion>> state) {
        if (state == null) return;
        TextView status = view.findViewById(R.id.home_offers_status);
        View retry = view.findViewById(R.id.home_offers_retry);
        if (state.status == LoadState.Status.LOADING) {
            status.setText("Carregando ofertas...");
            retry.setVisibility(View.GONE);
            return;
        }
        if (state.status == LoadState.Status.ERROR) {
            status.setText(state.message);
            retry.setVisibility(View.VISIBLE);
            return;
        }
        LinearLayout featuredOffersLayout = view.findViewById(R.id.layout_featured_offers);
        featuredOffersLayout.removeAllViews();
        List<CatalogPromotion> featuredOffers = state.data.items();
        int count = Math.min(2, featuredOffers.size());
        for (int i = 0; i < count; i++) {
            Offer offer = new Offer(featuredOffers.get(i));
            View card = getLayoutInflater().inflate(R.layout.item_offer, featuredOffersLayout, false);
            OfferAdapter.bind(card, offer);
            card.setOnClickListener(v -> openOfferDetail(view, offer));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i > 0) {
                params.setMarginStart((int) (8 * getResources().getDisplayMetrics().density));
            }
            card.setLayoutParams(params);
            featuredOffersLayout.addView(card);
        }
        status.setText(count == 0 ? "Nenhuma oferta disponível no momento." : "");
        retry.setVisibility(View.GONE);
    }

    private void renderStores(View view,
                              LoadState<com.mottainai.cliente.network.dto.CatalogPage<CatalogStore>> state) {
        if (state == null) return;
        TextView status = view.findViewById(R.id.tv_nearest_store);
        View retry = view.findViewById(R.id.home_stores_retry);
        if (state.status == LoadState.Status.LOADING) {
            status.setText("Carregando lojas...");
            mapPreview.setVisibility(View.GONE);
            retry.setVisibility(View.GONE);
            return;
        }
        if (state.status == LoadState.Status.ERROR) {
            status.setText(state.message);
            mapPreview.setVisibility(View.GONE);
            retry.setVisibility(View.VISIBLE);
            return;
        }
        List<PartnerStore> stores = new ArrayList<>();
        for (CatalogStore store : state.data.items()) stores.add(new PartnerStore(store));
        mapPreview.setStores(stores);
        long located = stores.stream().filter(PartnerStore::hasValidLocation).count();
        mapPreview.setVisibility(located == 0 ? View.GONE : View.VISIBLE);
        status.setText(stores.isEmpty() ? "Nenhuma loja disponível."
                : located == 0 ? "Lojas sem coordenadas. Consulte a lista."
                : located + " lojas com localização");
        retry.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapPreview != null) mapPreview.resumeMap();
    }

    @Override
    public void onPause() {
        if (mapPreview != null) mapPreview.pauseMap();
        super.onPause();
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
