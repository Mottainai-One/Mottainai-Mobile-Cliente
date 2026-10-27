package com.mottainai.cliente.models;

/** Product-approved radii, shared by location-related screens. */
public enum OfferRadius {
    KM_5(5), KM_10(10), KM_15(15), KM_20(20);

    public static final OfferRadius DEFAULT = KM_5;
    private final int kilometers;

    OfferRadius(int kilometers) { this.kilometers = kilometers; }

    public int kilometers() { return kilometers; }

    public static OfferRadius fromKilometers(int kilometers) {
        for (OfferRadius radius : values()) {
            if (radius.kilometers == kilometers) return radius;
        }
        throw new IllegalArgumentException("Raio não permitido: " + kilometers);
    }
}
