package com.mottainai.cliente.repository;

import com.mottainai.cliente.network.dto.CustomerProfileResponse;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProfileCreationPolicyTest {
    @Test public void onlyConfirmed404AllowsCreation() {
        assertTrue(ProfileSyncStatus.from(404, null).allowsProfileCreation(true));
        for (int status : new int[]{400, 401, 403, 409, 429, 500, 503, 0}) {
            assertFalse(ProfileSyncStatus.from(status, null).allowsProfileCreation(true));
        }
    }
    @Test public void conflictReconciliationNeverLoopsBackToPost() {
        assertFalse(ProfileSyncStatus.from(404, null).allowsProfileCreation(false));
    }
    @Test public void existingInactiveAndIncompleteProfilesAreNotRecreated() {
        CustomerProfileResponse profile = new CustomerProfileResponse();
        profile.id = 1;
        profile.active = true;
        assertFalse(ProfileSyncStatus.from(200, profile).allowsProfileCreation(true));
        profile.active = false;
        assertFalse(ProfileSyncStatus.from(200, profile).allowsProfileCreation(true));
        assertFalse(ProfileSyncStatus.from(200, null).allowsProfileCreation(true));
    }
}
