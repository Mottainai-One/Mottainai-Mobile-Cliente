package com.mottainai.cliente.repository;

import com.google.gson.Gson;
import com.mottainai.cliente.network.dto.CustomerProfileResponse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CustomerProfileResponseTest {
    @Test public void readsProtectedProfileWithoutExpectingPlaintextContactFields() {
        CustomerProfileResponse profile = new Gson().fromJson(
                "{\"id\":7,\"birthYear\":2000,\"active\":true,\"marketingConsent\":false}",
                CustomerProfileResponse.class);
        assertEquals(Integer.valueOf(7), profile.id);
        assertEquals(Short.valueOf((short) 2000), profile.birthYear);
        assertTrue(profile.active);
        assertNull(profile.fullName);
        assertNull(profile.email);
    }

    @Test public void neverDisplaysProtectedTokensAsNameOrContact() {
        CustomerProfileResponse profile = new Gson().fromJson(
                "{\"id\":7,\"fullName\":\"CUS_SHA256_example\",\"email\":\"EMAIL_SHA256_example\",\"phone\":\"PHONE_SHA256_example\"}",
                CustomerProfileResponse.class);
        assertNull(profile.fullName);
        assertNull(profile.email);
        assertNull(profile.phone);
    }
}
