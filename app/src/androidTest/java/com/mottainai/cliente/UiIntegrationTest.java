package com.mottainai.cliente;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.res.Configuration;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.mottainai.cliente.adapters.OfferAdapter;
import com.mottainai.cliente.models.Offer;
import com.mottainai.cliente.widgets.PartnerMapView;

import org.junit.Test;
import org.junit.runner.RunWith;

/** Guards the existing controllers' contracts while their XML presentation changes. */
@RunWith(AndroidJUnit4.class)
public class UiIntegrationTest {
    @Test public void screenBindingsSurviveInLightAndDarkThemes() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            for (int mode : new int[]{Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES}) {
                Context context = themedContext(mode);
                View home = inflate(context, R.layout.fragment_home);
                assertTrue(home.findViewById(R.id.layout_featured_offers) instanceof RecyclerView);
                assertTrue(home.findViewById(R.id.map_preview) instanceof PartnerMapView);
                assertNotNull(home.findViewById(R.id.home_open_catalog));
                View catalog = inflate(context, R.layout.fragment_catalog);
                assertTrue(catalog.findViewById(R.id.et_search) instanceof EditText);
                assertTrue(catalog.findViewById(R.id.rv_offers) instanceof RecyclerView);
                assertNotNull(catalog.findViewById(R.id.catalog_retry));
                View login = inflate(context, R.layout.activity_login_cliente);
                assertTrue(login.findViewById(R.id.input_login_email) instanceof EditText);
                assertTrue(login.findViewById(R.id.btn_login_google) instanceof Button);
                assertTrue(login.findViewById(R.id.btn_retry_profile_sync) instanceof Button);
                View registration = inflate(context, R.layout.activity_register_cliente);
                assertTrue(registration.findViewById(R.id.input_register_cpf) instanceof EditText);
                assertTrue(registration.findViewById(R.id.input_register_phone) instanceof EditText);
                assertNotNull(registration.findViewById(R.id.register_pages));
                View stores = inflate(context, R.layout.fragment_stores);
                assertTrue(stores.findViewById(R.id.map_stores) instanceof PartnerMapView);
                assertNotNull(stores.findViewById(R.id.stores_retry));
                assertNotNull(inflate(context, R.layout.fragment_points).findViewById(R.id.rv_activity));
                assertNotNull(inflate(context, R.layout.fragment_profile).findViewById(R.id.btn_logout));
                assertNotNull(inflate(context, R.layout.fragment_impact).findViewById(R.id.impact_points));
                assertNotNull(inflate(context, R.layout.activity_offer_detail).findViewById(R.id.btn_redeem));
                assertNotNull(inflate(context, R.layout.activity_assistant).findViewById(R.id.rv_messages));
            }
        });
    }

    @Test public void bothOfferPresentationsBindAndClearRecycledBadges() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Context context = themedContext(Configuration.UI_MODE_NIGHT_NO);
            Offer withBadge = new Offer("one", "Produto", "Mercado", 2.5, 40, "Hoje",
                    null, R.color.thumb_green, "Últimas unidades", "");
            Offer withoutBadge = new Offer("two", "Outro produto", "Outra loja", 1, 20, "Amanhã",
                    null, R.color.thumb_green, null, "");
            for (int layout : new int[]{R.layout.item_offer, R.layout.item_offer_compact}) {
                View card = inflate(context, layout);
                assertTrue(card.findViewById(R.id.view_offer_thumb) instanceof ImageView);
                OfferAdapter.bind(card, withBadge);
                assertEquals("Produto", ((TextView) card.findViewById(R.id.tv_offer_title)).getText().toString());
                assertEquals(View.VISIBLE, card.findViewById(R.id.tv_offer_badge_label).getVisibility());
                OfferAdapter.bind(card, withoutBadge);
                assertEquals("Outro produto", ((TextView) card.findViewById(R.id.tv_offer_title)).getText().toString());
                assertEquals(View.GONE, card.findViewById(R.id.tv_offer_badge_label).getVisibility());
            }
        });
    }

    private static Context themedContext(int nightMode) {
        Context base = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.uiMode = (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | nightMode;
        return new ContextThemeWrapper(base.createConfigurationContext(config), R.style.Theme_MottainaiCliente);
    }

    private static View inflate(Context context, int layout) {
        return LayoutInflater.from(context).inflate(layout, null, false);
    }
}
