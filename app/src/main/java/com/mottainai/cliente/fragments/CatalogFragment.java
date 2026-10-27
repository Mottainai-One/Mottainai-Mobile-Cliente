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
import android.graphics.Rect;

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
    private String storeId;
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
        boolean firstLoad = viewModel.promotions().getValue() == null;
        storeId = firstLoad && getArguments() != null
                ? getArguments().getString("store_id") : viewModel.promotionStoreId();
        currentQuery = viewModel.promotionQuery();

        RecyclerView recyclerView = view.findViewById(R.id.rv_offers);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        int gridSpacing = getResources().getDimensionPixelSize(R.dimen.offer_grid_spacing);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View item,
                                       @NonNull RecyclerView parent,
                                       @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(item);
                if (position < 0) return;
                if (position % 2 == 0) outRect.right = gridSpacing;
                int lastRow = (state.getItemCount() - 1) / 2;
                if (position / 2 < lastRow) outRect.bottom = gridSpacing;
            }
        });
        offerAdapter = new OfferAdapter(offer -> {
            Bundle args = new Bundle();
            args.putString("offer_id", offer.getId());
            Navigation.findNavController(view).navigate(R.id.offerDetailActivity, args);
        });
        recyclerView.setAdapter(offerAdapter);

        EditText searchField = view.findViewById(R.id.et_search);
        searchField.setText(currentQuery);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString();
                if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
                if (currentQuery.trim().equals(viewModel.promotionQuery())) return;
                pendingSearch = () -> reload();
                handler.postDelayed(pendingSearch, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        View clearStore = view.findViewById(R.id.catalog_clear_store);
        clearStore.setVisibility(storeId == null ? View.GONE : View.VISIBLE);
        clearStore.setOnClickListener(v -> {
            storeId = null;
            clearStore.setVisibility(View.GONE);
            reload();
        });
        view.findViewById(R.id.catalog_retry).setOnClickListener(v -> loadPage(viewModel.promotionPage()));
        view.findViewById(R.id.catalog_load_more).setOnClickListener(v -> loadPage(viewModel.nextPromotionPage()));
        viewModel.promotions().observe(getViewLifecycleOwner(), state -> render(view, state));
        if (firstLoad) reload();
    }

    private void reload() {
        loadPage(0);
    }

    private void loadPage(int requestedPage) {
        viewModel.loadPromotions(requestedPage, storeId, currentQuery);
    }

    private void render(View view, LoadState<com.mottainai.cliente.network.dto.CatalogPage<CatalogPromotion>> state) {
        if (state == null) return;
        TextView message = view.findViewById(R.id.catalog_message);
        View status = view.findViewById(R.id.catalog_state);
        View loading = view.findViewById(R.id.catalog_loading);
        View retry = view.findViewById(R.id.catalog_retry);
        View more = view.findViewById(R.id.catalog_load_more);
        offers.clear();
        if (state.data != null) {
            for (CatalogPromotion promotion : state.data.items()) offers.add(new Offer(promotion));
            offerCountLabel.setText(String.format(Locale.getDefault(),
                    "%d ofertas para aproveitar", state.data.totalElements));
        } else {
            offerCountLabel.setText("");
        }
        offerAdapter.setOffers(offers);
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
        status.setVisibility(offers.isEmpty() ? View.VISIBLE : View.GONE);
        message.setText("Nenhuma oferta disponível no momento.");
        retry.setVisibility(View.GONE);
        more.setVisibility(state.data.number + 1 < state.data.totalPages ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
        super.onDestroyView();
    }
}
