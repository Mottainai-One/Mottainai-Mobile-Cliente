package com.mottainai.cliente.repository;

import com.mottainai.cliente.models.UserProfile;

/** Stand-in for the future /users/me endpoint. See {@link MockOfferRepository}. */
public class MockUserRepository {

    public UserProfile getCurrentUser() {
        return new UserProfile("Maria Almeida", "MA", "Nível Semente", "72% para Florescer",
                1280, 72, 4.8, 12, 86.0);
    }
}
