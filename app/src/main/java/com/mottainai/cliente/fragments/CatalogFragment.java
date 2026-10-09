package com.mottainai.cliente.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.OfferAdapter;
import com.mottainai.cliente.models.Offer;
import com.mottainai.cliente.network.dto.CatalogPromotion;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.viewmodel.CatalogViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CatalogFragment extends Fragment {

    private final List<Offer> offers = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private CatalogViewModel viewModel;
    private OfferAdapter offerAdapter;
    private TextView offerCountLabel;
    private String currentQuery = "";
    private int page;
    private boolean hasMore;
    private Runnable pendingSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        offerCountLabel = view.findViewById(R.id.tv_offer_count);
        viewModel = new ViewModelProvider(this).get(CatalogViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rv_offers);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        offerAdapter = new OfferAdapter(offer -> {
            Bundle args = new Bundle();
            args.putString("offer_id", offer.getId());
            Navigation.findNavController(view).navigate(R.id.offerDetailActivity, args);
        });
        recyclerView.setAdapter(offerAdapter);

        EditText searchField = view.findViewById(R.id.et_search);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString();
                if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
                pendingSearch = () -> reload();
                handler.postDelayed(pendingSearch, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        view.findViewById(R.id.catalog_retry).setOnClickListener(v -> loadPage(page));
        view.findViewById(R.id.catalog_load_more).setOnClickListener(v -> loadPage(page + 1));
        viewModel.promotions().observe(getViewLifecycleOwner(), state -> render(view, state));
        reload();
    }

    private void reload() {
        page = 0;
        hasMore = false;
        offers.clear();
        if (offerAdapter != null) offerAdapter.setOffers(offers);
        loadPage(0);
    }

    private void loadPage(int requestedPage) {
        page = requestedPage;
        viewModel.loadPromotions(requestedPage, null, currentQuery);
    }

    private void render(View view, LoadState<com.mottainai.cliente.network.dto.CatalogPage<CatalogPromotion>> state) {
        if (state == null) return;
        TextView message = view.findViewById(R.id.catalog_message);
        View status = view.findViewById(R.id.catalog_state);
        View loading = view.findViewById(R.id.catalog_loading);
        View retry = view.findViewById(R.id.catalog_retry);
        View more = view.findViewById(R.id.catalog_load_more);
        loading.setVisibility(state.status == LoadState.Status.LOADING ? View.VISIBLE : View.GONE);
        if (state.status == LoadState.Status.LOADING) {
            status.setVisibility(offers.isEmpty() ? View.VISIBLE : View.GONE);
            message.setText("Carregando ofertas...");
            retry.setVisibility(View.GONE);
            more.setVisibility(View.GONE);
            return;
        }
        if (state.status == LoadState.Status.ERROR) {
            status.setVisibility(View.VISIBLE);
            message.setText(state.message);
            retry.setVisibility(View.VISIBLE);
            more.setVisibility(View.GONE);
            return;
        }
        for (CatalogPromotion promotion : state.data.items()) {
            offers.add(new Offer(promotion));
        }
        offerAdapter.setOffers(offers);
        hasMore = page + 1 < state.data.totalPages;
        status.setVisibility(offers.isEmpty() ? View.VISIBLE : View.GONE);
        message.setText("Nenhuma oferta disponível no momento.");
        retry.setVisibility(View.GONE);
        more.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        offerCountLabel.setText(String.format(Locale.getDefault(),
                "%d ofertas para aproveitar", state.data.totalElements));
    }

    @Override
    public void onDestroyView() {
        if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
        super.onDestroyView();
    }
}
