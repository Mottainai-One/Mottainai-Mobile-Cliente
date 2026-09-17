package com.mottainai.cliente.models;

public class PointsActivityEntry {

    private final String label;
    private final int pointsDelta;
    private final int colorRes;

    public PointsActivityEntry(String label, int pointsDelta, int colorRes) {
        this.label = label;
        this.pointsDelta = pointsDelta;
        this.colorRes = colorRes;
    }

    public String getLabel() {
        return label;
    }

    public int getPointsDelta() {
        return pointsDelta;
    }

    public int getColorRes() {
        return colorRes;
    }
}
