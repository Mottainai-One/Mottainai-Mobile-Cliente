package com.mottainai.cliente.activities;

import android.content.res.ColorStateList;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.mottainai.cliente.R;
import com.mottainai.cliente.models.Offer;
import com.mottainai.cliente.network.dto.CatalogPromotion;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.viewmodel.CatalogViewModel;

import java.util.Locale;

public class OfferDetailActivity extends AppCompatActivity {

    private CatalogViewModel viewModel;
    private String offerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);

        offerId = getIntent().getStringExtra("offer_id");
        if (offerId == null || offerId.isEmpty()) {
            finish();
            return;
        }
        viewModel = new ViewModelProvider(this).get(CatalogViewModel.class);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_redeem).setVisibility(View.GONE);
        findViewById(R.id.offer_detail_retry).setOnClickListener(v ->
                viewModel.loadPromotion(offerId));
        viewModel.detail().observe(this, this::render);
        viewModel.loadPromotion(offerId);
    }

    private void render(LoadState<CatalogPromotion> state) {
        if (state == null) return;
        TextView status = findViewById(R.id.offer_detail_status);
        View retry = findViewById(R.id.offer_detail_retry);
        if (state.status == LoadState.Status.LOADING) {
            status.setText("Carregando oferta...");
            retry.setVisibility(View.GONE);
            return;
        }
        if (state.status == LoadState.Status.ERROR) {
            status.setText(state.message);
            retry.setVisibility(View.VISIBLE);
            return;
        }
        status.setText("");
        retry.setVisibility(View.GONE);
        bind(new Offer(state.data));
        View route = findViewById(R.id.btn_redeem);
        if (state.data.store != null && state.data.store.latitude != null
                && state.data.store.longitude != null) {
            route.setVisibility(View.VISIBLE);
            route.setOnClickListener(v -> {
                String coordinates = state.data.store.latitude + "," + state.data.store.longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=" + Uri.encode(coordinates)));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Nenhum app de mapas disponível.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            route.setVisibility(View.GONE);
        }
    }

    private void bind(Offer offer) {
        findViewById(R.id.view_offer_image).setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, offer.getThumbnailColorRes())));

        ((TextView) findViewById(R.id.tv_offer_title)).setText(offer.getTitle());
        ((TextView) findViewById(R.id.tv_offer_store)).setText(offer.getStoreName());
        ((TextView) findViewById(R.id.tv_offer_discount)).setText(
                offer.getPriceLabel() == null ? offer.getExpiryLabel()
                        : offer.getPriceLabel() + " · " + offer.getExpiryLabel());
        ((TextView) findViewById(R.id.tv_offer_availability)).setText(
                offer.getItemsLabel().isEmpty() ? offer.getDescription()
                        : offer.getItemsLabel() + "\n" + offer.getDescription());
    }
}
