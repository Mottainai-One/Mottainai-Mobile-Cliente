package com.mottainai.cliente.models;

public class PartnerStore {

    private final String id;
    private final String name;
    private final double distanceKm;
    private final String hoursLabel;
    private final boolean openNow;
    private final float pinXPercent;
    private final float pinYPercent;

    public PartnerStore(String id, String name, double distanceKm, String hoursLabel, boolean openNow,
                         float pinXPercent, float pinYPercent) {
        this.id = id;
        this.name = name;
        this.distanceKm = distanceKm;
        this.hoursLabel = hoursLabel;
        this.openNow = openNow;
        this.pinXPercent = pinXPercent;
        this.pinYPercent = pinYPercent;
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
}
