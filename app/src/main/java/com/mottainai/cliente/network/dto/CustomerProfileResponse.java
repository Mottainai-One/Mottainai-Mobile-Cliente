package com.mottainai.cliente.network.dto;

public final class CustomerProfileResponse {
    public Integer id;
    public Short birthYear;
    public Boolean active;
    // The protected SQL response omits plaintext contact data; Firebase supplies display identity.
    public transient String fullName;
    public transient String email;
    public transient String phone;
    public Boolean marketingConsent;
}
