package com.mottainai.cliente.repository;

import com.mottainai.cliente.models.UserProfile;

/** Stand-in for the future /users/me endpoint. See {@link MockOfferRepository}. */
public class MockUserRepository {

    public UserProfile getCurrentUser() {
        return new UserProfile(
                "Camila Oliveira", "CO", "Broto", "55% para Planta", 320, 55, 4.8, 12, 86.0);
    }
}
