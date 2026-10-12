package com.mottainai.cliente.network.dto;

public final class UpdateCustomerRequest {
    public final String fullName;
    public final String email;
    public final String phone;
    public final String birthDate;
    public final Boolean marketingConsent;
    public final CreateAddressRequest address;

    public UpdateCustomerRequest(String fullName, String email, String phone, String birthDate,
                                 Boolean marketingConsent, CreateAddressRequest address) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.marketingConsent = marketingConsent;
        this.address = address;
    }
}
