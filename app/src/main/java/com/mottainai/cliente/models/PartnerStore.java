package com.mottainai.cliente.models;

import com.mottainai.cliente.network.dto.CatalogStore;
import com.mottainai.cliente.utils.GeoCoordinates;

import java.util.StringJoiner;

public class PartnerStore {

    private final String id;
    private final String name;
    private final double distanceKm;
    private final String hoursLabel;
    private final boolean openNow;
    private final float pinXPercent;
    private final float pinYPercent;
    private final Double latitude;
    private final Double longitude;
    private final String addressLabel;
    private final String routeAddress;

    public PartnerStore(String id, String name, double distanceKm, String hoursLabel, boolean openNow,
                         float pinXPercent, float pinYPercent) {
        this.id = id;
        this.name = name;
        this.distanceKm = distanceKm;
        this.hoursLabel = hoursLabel;
        this.openNow = openNow;
        this.pinXPercent = pinXPercent;
        this.pinYPercent = pinYPercent;
        this.latitude = null;
        this.longitude = null;
        this.addressLabel = "";
        this.routeAddress = null;
    }

    public PartnerStore(CatalogStore store) {
        this.id = store.id;
        this.name = store.name == null ? "Loja parceira" : store.name;
        this.distanceKm = 0;
        this.hoursLabel = "";
        this.openNow = false;
        this.pinXPercent = 0;
        this.pinYPercent = 0;
        this.latitude = store.latitude == null ? null : store.latitude.doubleValue();
        this.longitude = store.longitude == null ? null : store.longitude.doubleValue();
        if (store.address == null) {
            this.addressLabel = "Endereço indisponível";
            this.routeAddress = null;
        } else {
            StringJoiner address = new StringJoiner(", ");
            for (String part : new String[]{store.address.street, store.address.number,
                    store.address.neighborhood, store.address.city, store.address.state,
                    store.address.zipCode}) {
                if (hasText(part)) address.add(part.trim());
            }
            this.addressLabel = address.length() == 0 ? "Endereço indisponível" : address.toString();
            this.routeAddress = hasText(store.address.street) && hasText(store.address.number)
                    && hasText(store.address.city) && hasText(store.address.state)
                    ? address.toString() : null;
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public String getHoursLabel() {
        return hoursLabel;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public float getPinXPercent() {
        return pinXPercent;
    }

    public float getPinYPercent() {
        return pinYPercent;
    }

    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getAddressLabel() { return addressLabel; }
    public String getRouteAddress() { return routeAddress; }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
    public boolean hasValidLocation() {
        return GeoCoordinates.areValid(latitude, longitude);
    }
}
