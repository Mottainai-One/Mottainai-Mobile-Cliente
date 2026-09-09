package com.mottainai.cliente.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.OfferAdapter;
import com.mottainai.cliente.models.Offer;
import com.mottainai.cliente.repository.MockOfferRepository;

import java.util.List;
import java.util.Locale;

public class CatalogFragment extends Fragment {

    private final MockOfferRepository offerRepository = new MockOfferRepository();
    private OfferAdapter offerAdapter;
    private TextView offerCountLabel;
    private String currentQuery = "";

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
                applyFilters(view);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ChipGroup chipGroup = view.findViewById(R.id.chip_group_filters);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters(view));

        applyFilters(view);
    }

    private void applyFilters(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_filters);
        int checkedId = chipGroup.getCheckedChipId();

        String category = null;
        boolean withinOneKm = false;
        if (checkedId == R.id.chip_dairy) {
            category = "Laticínios";
        } else if (checkedId == R.id.chip_bakery) {
            category = "Padaria";
        } else if (checkedId == R.id.chip_within_1km) {
            withinOneKm = true;
        }

        List<Offer> filtered = offerRepository.filter(currentQuery, category, withinOneKm);
        offerAdapter.setOffers(filtered);
        offerCountLabel.setText(String.format(Locale.getDefault(), "%d ofertas para aproveitar", filtered.size()));
    }
}
