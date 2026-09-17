package com.mottainai.cliente.models;

public class UserProfile {

    private final String name;
    private final String initials;
    private final String levelLabel;
    private final String levelSubLabel;
    private final int pointsBalance;
    private final int levelProgressPercent;
    private final double kgAvoided;
    private final int itemsSavedCount;
    private final double moneySavedBrl;

    public UserProfile(String name, String initials, String levelLabel, String levelSubLabel,
                        int pointsBalance, int levelProgressPercent, double kgAvoided,
                        int itemsSavedCount, double moneySavedBrl) {
        this.name = name;
        this.initials = initials;
        this.levelLabel = levelLabel;
        this.levelSubLabel = levelSubLabel;
        this.pointsBalance = pointsBalance;
        this.levelProgressPercent = levelProgressPercent;
        this.kgAvoided = kgAvoided;
        this.itemsSavedCount = itemsSavedCount;
        this.moneySavedBrl = moneySavedBrl;
    }

    public String getName() {
        return name;
    }

    public String getInitials() {
        return initials;
    }

    public String getLevelLabel() {
        return levelLabel;
    }

    public String getLevelSubLabel() {
        return levelSubLabel;
    }

    public int getPointsBalance() {
        return pointsBalance;
    }

    public int getLevelProgressPercent() {
        return levelProgressPercent;
    }

    public double getKgAvoided() {
        return kgAvoided;
    }

    public int getItemsSavedCount() {
        return itemsSavedCount;
    }

    public double getMoneySavedBrl() {
        return moneySavedBrl;
    }
}
