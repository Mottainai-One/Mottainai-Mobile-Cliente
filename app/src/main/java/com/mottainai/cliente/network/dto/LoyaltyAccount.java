package com.mottainai.cliente.network.dto;

public class LoyaltyAccount {
    public String id;
    public String customerId;
    public Integer pointsBalance;
    // Optional mobile contract extension; missing values must not be calculated locally.
    public String statusName;
    public Integer pointsToNextStatus;
}
