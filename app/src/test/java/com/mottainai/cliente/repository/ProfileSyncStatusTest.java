package com.mottainai.cliente.repository;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProfileSyncStatusTest {
    @Test public void successfulResponseRequiresAValidSqlIdentity() {
        assertEquals(ProfileSyncStatus.READY, ProfileSyncStatus.from(200, 7));
        assertEquals(ProfileSyncStatus.READY, ProfileSyncStatus.from(201, 7));
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(200, null));
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(204, null));
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(200, 0));
        assertEquals(ProfileSyncStatus.INVALID_RESPONSE, ProfileSyncStatus.from(200, -1));
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
        assertEquals(ProfileSyncStatus.ERROR, ProfileSyncStatus.from(401, 7));
        assertEquals(ProfileSyncStatus.ERROR, ProfileSyncStatus.from(500, 7));
        assertEquals(ProfileSyncStatus.PROFILE_REQUIRED, ProfileSyncStatus.from(404, 7));
    }
}
