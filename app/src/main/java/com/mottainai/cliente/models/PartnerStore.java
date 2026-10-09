package com.mottainai.cliente.models;

import com.mottainai.cliente.network.dto.CatalogStore;

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
        } else {
            String street = store.address.street == null ? "" : store.address.street;
            String number = store.address.number == null ? "" : ", " + store.address.number;
            String city = store.address.city == null ? "" : " · " + store.address.city;
            this.addressLabel = (street + number + city).trim();
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
    public boolean hasValidLocation() {
        return latitude != null && longitude != null
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }
}
