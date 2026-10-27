package com.mottainai.cliente.utils;

import com.mottainai.cliente.models.PartnerStore;
import com.mottainai.cliente.network.dto.CatalogStore;

import org.junit.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;

import static org.junit.Assert.*;

public class GoogleMapsUrlTest {
    @Test public void coordinatesTakePrecedenceOverAddress() throws Exception {
        CatalogStore store = store();
        store.latitude = new BigDecimal("-23.55");
        store.longitude = new BigDecimal("-46.63");
        assertEquals("-23.55,-46.63", query(store));
    }

    @Test public void addressIncludesUnitCityStateAndZipAndEscapesSpecialCharacters() throws Exception {
        CatalogStore store = store();
        store.address.street = "Rua São João & Filhos #1";
        assertEquals("Rua São João & Filhos #1, 100, Centro, São Paulo, SP, 01000-000", query(store));
        URI uri = URI.create(GoogleMapsUrl.forStore(new PartnerStore(store)));
        assertEquals("https", uri.getScheme());
        assertEquals("www.google.com", uri.getHost());
        assertEquals("/maps/search/", uri.getPath());
        assertNull(uri.getFragment());
        assertTrue(uri.getRawQuery().startsWith("api=1&query="));
    }

    @Test public void differentUnitsOfSameChainHaveDifferentDestinations() {
        CatalogStore first = store();
        CatalogStore second = store();
        second.address.number = "200";
        assertNotEquals(GoogleMapsUrl.forStore(new PartnerStore(first)),
                GoogleMapsUrl.forStore(new PartnerStore(second)));
    }

    @Test public void invalidOrIncompleteCoordinatesUseFullAddress() throws Exception {
        CatalogStore store = store();
        store.latitude = new BigDecimal("91");
        store.longitude = BigDecimal.ZERO;
        assertTrue(query(store).contains("São Paulo, SP"));
        store.latitude = BigDecimal.ZERO;
        store.longitude = null;
        assertTrue(query(store).contains("São Paulo, SP"));
    }

    @Test public void missingOrPartialAddressDoesNotSearchForTheChainName() {
        CatalogStore store = store();
        store.address.state = " ";
        assertNull(GoogleMapsUrl.forStore(new PartnerStore(store)));
        store.address = null;
        assertNull(GoogleMapsUrl.forStore(new PartnerStore(store)));
        assertNull(GoogleMapsUrl.forStore(null));
    }

    @Test public void zeroCoordinatesAreValidAndDoNotRequireAddress() throws Exception {
        CatalogStore store = store();
        store.latitude = BigDecimal.ZERO;
        store.longitude = BigDecimal.ZERO;
        store.address = null;
        assertEquals("0.0,0.0", query(store));
    }

    @Test public void emptyAddressHasAnExplicitPlaceholder() {
        CatalogStore store = store();
        store.address = new CatalogStore.Address();
        PartnerStore partner = new PartnerStore(store);
        assertEquals("Endereço indisponível", partner.getAddressLabel());
        assertNull(GoogleMapsUrl.forStore(partner));
    }

    private String query(CatalogStore store) throws Exception {
        String url = GoogleMapsUrl.forStore(new PartnerStore(store));
        return URLDecoder.decode(url.substring(url.indexOf("&query=") + 7), "UTF-8");
    }

    private CatalogStore store() {
        CatalogStore store = new CatalogStore();
        store.id = "1";
        store.name = "Mercado parceiro";
        store.address = new CatalogStore.Address();
        store.address.street = "Rua Principal";
        store.address.number = "100";
        store.address.neighborhood = "Centro";
        store.address.city = "São Paulo";
        store.address.state = "SP";
        store.address.zipCode = "01000-000";
        return store;
    }
}
