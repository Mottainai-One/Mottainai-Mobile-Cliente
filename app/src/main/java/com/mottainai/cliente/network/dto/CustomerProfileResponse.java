package com.mottainai.cliente.network.dto;

public final class CustomerProfileResponse {
    public Integer id;
    public Short birthYear;
    public Boolean active;
    // Presentation-only fields, excluded from JSON. Firebase supplies only the login email.
    public transient String fullName;
    public transient String email;
    public transient String phone;
    public Boolean marketingConsent;
}
