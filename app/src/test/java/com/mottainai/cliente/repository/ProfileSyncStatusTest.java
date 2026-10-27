package com.mottainai.cliente.repository;

import com.mottainai.cliente.network.dto.CustomerProfileResponse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProfileSyncStatusTest {
    @Test public void successfulResponseRequiresAValidSqlIdentity() {
        assertEquals(ProfileSyncStatus.READY, ProfileSyncStatus.from(200, profile(7, true)));
        assertEquals(ProfileSyncStatus.READY, ProfileSyncStatus.from(201, profile(7, true)));
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(200, null));
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(204, null));
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(200, profile(null, true)));
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(200, profile(0, true)));
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(200, profile(-1, true)));
    }

    @Test public void profileMustExplicitlyBeActive() {
        assertEquals(ProfileSyncStatus.INACTIVE, ProfileSyncStatus.from(200, profile(7, false)));
        assertEquals(ProfileSyncStatus.INACTIVE, ProfileSyncStatus.from(201, profile(7, false)));
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(200, profile(7, null)));
    }

    @Test public void pendingOrEmptySuccessDoesNotConfirmSynchronization() {
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(202, profile(7, true)));
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(204, profile(7, true)));
    }

    @Test public void onlyMissingProfileStartsRegistrationCompletion() {
        assertEquals(ProfileSyncStatus.PROFILE_REQUIRED, ProfileSyncStatus.from(404, null));
        assertEquals(ProfileSyncStatus.ERROR, ProfileSyncStatus.from(401, null));
        assertEquals(ProfileSyncStatus.ERROR, ProfileSyncStatus.from(403, null));
        assertEquals(ProfileSyncStatus.ERROR, ProfileSyncStatus.from(409, null));
        assertEquals(ProfileSyncStatus.ERROR, ProfileSyncStatus.from(500, null));
        assertEquals(ProfileSyncStatus.ERROR, ProfileSyncStatus.from(0, null));
    }

    @Test public void errorPayloadCannotGrantAccessEvenWithAnId() {
        assertEquals(ProfileSyncStatus.ERROR, ProfileSyncStatus.from(401, profile(7, true)));
        assertEquals(ProfileSyncStatus.ERROR, ProfileSyncStatus.from(403, profile(7, true)));
        assertEquals(ProfileSyncStatus.ERROR, ProfileSyncStatus.from(500, profile(7, true)));
        assertEquals(ProfileSyncStatus.PROFILE_REQUIRED, ProfileSyncStatus.from(404, profile(7, true)));
    }

    private static CustomerProfileResponse profile(Integer id, Boolean active) {
        CustomerProfileResponse profile = new CustomerProfileResponse();
        profile.id = id;
        profile.active = active;
        return profile;
    }
}
