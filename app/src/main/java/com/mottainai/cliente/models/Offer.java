package com.mottainai.cliente.models;

public class Offer {

    private final String id;
    private final String title;
    private final String storeName;
    private final double distanceKm;
    private final int discountPercent;
    private final String expiryLabel;
    private final String category;
    private final int thumbnailColorRes;
    private final String badgeLabel;
    private final String description;

    public Offer(String id, String title, String storeName, double distanceKm, int discountPercent,
                 String expiryLabel, String category, int thumbnailColorRes, String badgeLabel,
                 String description) {
        this.id = id;
        this.title = title;
        this.storeName = storeName;
        this.distanceKm = distanceKm;
        this.discountPercent = discountPercent;
        this.expiryLabel = expiryLabel;
        this.category = category;
        this.thumbnailColorRes = thumbnailColorRes;
        this.badgeLabel = badgeLabel;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStoreName() {
        return storeName;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public String getExpiryLabel() {
        return expiryLabel;
    }

    public String getCategory() {
        return category;
    }

    public int getThumbnailColorRes() {
        return thumbnailColorRes;
    }

    public String getBadgeLabel() {
        return badgeLabel;
    }

    public String getDescription() {
        return description;
    }
}
