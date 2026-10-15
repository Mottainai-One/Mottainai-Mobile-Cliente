package com.mottainai.cliente.network.dto;

public final class CreateFirebaseCustomerProfileRequest {
    public final String fullName;
    public final String cpf;
    public final String phone;
    public final String birthDate;
    public final Boolean marketingConsent;
    public final CreateAddressRequest address;

    public CreateFirebaseCustomerProfileRequest(String fullName, String cpf, String phone, String birthDate,
                                               Boolean marketingConsent, CreateAddressRequest address) {
        this.fullName = fullName;
        this.cpf = cpf;
        this.phone = phone;
        this.birthDate = birthDate;
        this.marketingConsent = marketingConsent;
        this.address = address;
    }
}
