package com.mottainai.cliente.network.dto;

public final class CreateCustomerRequest {
    public final String fullName;
    public final String cpf;
    public final String email;
    public final String password;
    public final String phone;
    public final String birthDate;
    public final Boolean marketingConsent;
    public final CreateAddressRequest address;

    public CreateCustomerRequest(String fullName, String cpf, String email, String password,
                                 String phone, String birthDate, Boolean marketingConsent,
                                 CreateAddressRequest address) {
        this.fullName = fullName;
        this.cpf = cpf;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.birthDate = birthDate;
        this.marketingConsent = marketingConsent;
        this.address = address;
    }
}
