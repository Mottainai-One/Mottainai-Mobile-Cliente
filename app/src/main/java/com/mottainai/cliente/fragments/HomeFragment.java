package com.mottainai.cliente.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.OfferAdapter;
import com.mottainai.cliente.models.Offer;
import com.mottainai.cliente.models.PartnerStore;
import com.mottainai.cliente.repository.MockOfferRepository;
import com.mottainai.cliente.repository.MockStoreRepository;
import com.mottainai.cliente.widgets.PartnerMapView;

import java.util.List;

public class HomeFragment extends Fragment {

    private final MockOfferRepository offerRepository = new MockOfferRepository();
    private final MockStoreRepository storeRepository = new MockStoreRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout featuredOffersLayout = view.findViewById(R.id.layout_featured_offers);
        List<Offer> featuredOffers = offerRepository.getNearbyOffers();
        for (int i = 0; i < featuredOffers.size(); i++) {
            Offer offer = featuredOffers.get(i);
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

        PartnerMapView mapPreview = view.findViewById(R.id.map_preview);
        List<PartnerStore> stores = storeRepository.getNearbyStores();
        mapPreview.setStores(stores);
    }

    private void openOfferDetail(View view, Offer offer) {
        Bundle args = new Bundle();
        args.putString("offer_id", offer.getId());
        Navigation.findNavController(view).navigate(R.id.offerDetailActivity, args);
    }
}
