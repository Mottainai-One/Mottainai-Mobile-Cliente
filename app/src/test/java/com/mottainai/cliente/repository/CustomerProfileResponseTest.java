package com.mottainai.cliente.repository;

import com.google.gson.Gson;
import com.mottainai.cliente.network.dto.CustomerProfileResponse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Reúne os cenários de teste de CustomerProfileResponse. */
public class CustomerProfileResponseTest {
    /** Verifica a leitura do perfil protegido sem exigir contatos em texto aberto. */
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

    /** Verifica se valores protegidos não são exibidos como nome ou contato. */
    @Test public void neverDisplaysProtectedTokensAsNameOrContact() {
        CustomerProfileResponse profile = new Gson().fromJson(
                "{\"id\":7,\"fullName\":\"CUS_SHA256_example\",\"email\":\"EMAIL_SHA256_example\",\"phone\":\"PHONE_SHA256_example\"}",
                CustomerProfileResponse.class);
        assertNull(profile.fullName);
        assertNull(profile.email);
        assertNull(profile.phone);
    }

    /** Verifica se os campos exclusivos de apresentação ficam fora do JSON persistido. */
    @Test public void presentationFieldsAreNotPersistedInJson() {
        CustomerProfileResponse profile = new CustomerProfileResponse();
        profile.id = 7;
        profile.active = true;
        profile.fullName = "Nome confirmado pela atualização SQL";
        profile.email = "login@example.invalid";
        profile.phone = "telefone de apresentação";

        CustomerProfileResponse restored = new Gson().fromJson(
                new Gson().toJson(profile), CustomerProfileResponse.class);
        assertEquals(profile.id, restored.id);
        assertTrue(restored.active);
        assertNull(restored.fullName);
        assertNull(restored.email);
        assertNull(restored.phone);
    }
}
