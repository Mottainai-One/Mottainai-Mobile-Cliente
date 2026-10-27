package com.mottainai.cliente.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClientAccessStateTest {
    @Test public void sessionIsRequiredEvenWhenLocationIsAvailable() {
        assertEquals(ClientAccessState.LOGIN_REQUIRED,
                ClientAccessState.evaluate(false, false, true, true));
    }

    @Test public void missingPermissionBlocksServices() {
        assertEquals(ClientAccessState.LOCATION_REQUIRED,
                ClientAccessState.evaluate(true, false, false, true));
    }

    @Test public void disabledDeviceLocationBlocksServices() {
        assertEquals(ClientAccessState.LOCATION_REQUIRED,
                ClientAccessState.evaluate(true, false, true, false));
    }

    @Test public void grantAndEnabledDeviceAllowForegroundServices() {
        assertEquals(ClientAccessState.READY,
                ClientAccessState.evaluate(true, false, true, true));
    }

    @Test public void debugDemoDoesNotNeedRealLocation() {
        assertEquals(ClientAccessState.READY,
                ClientAccessState.evaluate(true, true, false, false));
    }

    @Test public void demoFlagCannotOverrideMissingBrowseSession() {
        assertEquals(ClientAccessState.LOGIN_REQUIRED,
                ClientAccessState.evaluate(false, true, true, true));
    }

    @Test public void allPermissionCombinationsRespectSessionAndDeviceRequirements() {
        for (boolean session : new boolean[]{false, true}) {
            for (boolean permission : new boolean[]{false, true}) {
                for (boolean enabled : new boolean[]{false, true}) {
                    ClientAccessState expected = !session ? ClientAccessState.LOGIN_REQUIRED
                            : permission && enabled ? ClientAccessState.READY : ClientAccessState.LOCATION_REQUIRED;
                    assertEquals(expected, ClientAccessState.evaluate(session, false, permission, enabled));
                }
            }
        }
    }
}
