package com.mottainai.cliente.utils;

public final class GeoCoordinates {
    private GeoCoordinates() { }

    public static boolean areValid(Double latitude, Double longitude) {
        return latitude != null && longitude != null
                && Double.isFinite(latitude) && Double.isFinite(longitude)
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }
}
