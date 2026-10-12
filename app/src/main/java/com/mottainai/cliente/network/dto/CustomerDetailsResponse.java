package com.mottainai.cliente.network.dto;

public final class CustomerDetailsResponse {
    public Integer id;
    public String fullName;
    public String cpf;
    public String email;
    public String phone;
    public String birthDate;
    public Boolean active;
    public Boolean marketingConsent;
    public Address address;

    public static final class Address {
        public Integer id;
        public String zipCode;
        public String street;
        public String number;
        public String complement;
        public String neighborhood;
        public String city;
        public String state;
    }
}
