package com.mottainai.cliente.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mottainai.cliente.R;
import com.mottainai.cliente.models.Offer;
import com.mottainai.cliente.repository.MockOfferRepository;

import java.util.Locale;

public class OfferDetailActivity extends AppCompatActivity {

    private final MockOfferRepository offerRepository = new MockOfferRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);

        String offerId = getIntent().getStringExtra("offer_id");
        Offer offer = offerId != null ? offerRepository.findById(offerId) : null;
        if (offer == null) {
            finish();
            return;
        }

        bind(offer);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_redeem).setOnClickListener(v ->
                Toast.makeText(this, "Rota até " + offer.getStoreName() + " — em desenvolvimento", Toast.LENGTH_SHORT).show());
    }

    private void bind(Offer offer) {
        findViewById(R.id.view_offer_image).setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, offer.getThumbnailColorRes())));

        ((TextView) findViewById(R.id.tv_offer_title)).setText(offer.getTitle());
        ((TextView) findViewById(R.id.tv_offer_store)).setText(
                String.format(Locale.getDefault(), "%s · %.1f km", offer.getStoreName(), offer.getDistanceKm()));
        ((TextView) findViewById(R.id.tv_offer_discount)).setText(
                String.format(Locale.getDefault(), "-%d%% %s", offer.getDiscountPercent(), offer.getExpiryLabel()));
        ((TextView) findViewById(R.id.tv_offer_availability)).setText(offer.getDescription());
    }
}
