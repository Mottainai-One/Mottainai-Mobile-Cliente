package com.mottainai.cliente;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.mottainai.cliente.fragments.CatalogFragment;
import com.mottainai.cliente.utils.SessionManager;

import org.junit.Test;
import org.junit.runner.RunWith;

/** Regression checks for graph destinations, panel dismissal and persisted navigation. */
@RunWith(AndroidJUnit4.class)
public class NavigationStructureTest {
    private Context context() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private ActivityScenario<MainActivity> launch(String screen) {
        context()
                .getSharedPreferences("cliente_preferences", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .putInt("theme", 1)
                .commit();
        return ActivityScenario.launch(
                new Intent(context(), MainActivity.class).putExtra("screen", screen));
    }

    private void idle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test
    public void primaryDestinationsUseExistingNavigationGraph() {
        try (ActivityScenario<MainActivity> scenario = launch("2005:3812")) {
            scenario.onActivity(activity -> activity.showScreen("2005:3914", true));
            idle();
            scenario.onActivity(
                    activity -> {
                        NavHostFragment host =
                                (NavHostFragment)
                                        activity.getSupportFragmentManager()
                                                .findFragmentById(R.id.nav_host_fragment);
                        assertNotNull(host);
                        assertEquals(
                                R.id.catalogFragment,
                                host.getNavController().getCurrentDestination().getId());
                        assertTrue(activity.currentScreen() instanceof CatalogFragment);
                        assertEquals("2005:3914", activity.currentScreen().getScreenId());
                    });
            scenario.recreate();
            idle();
            scenario.onActivity(
                    activity -> assertEquals("2005:3914", activity.currentScreen().getScreenId()));
        }
    }

    @Test
    public void backDismissesPanelBeforePoppingGraphDestination() {
        try (ActivityScenario<MainActivity> scenario = launch("2005:3812")) {
            scenario.onActivity(activity -> activity.showScreen("2005:4483", true));
            idle();
            Fragment[] base = new Fragment[1];
            scenario.onActivity(
                    activity -> {
                        base[0] = activity.currentScreen();
                        activity.showScreen("2028:4409", true);
                    });
            idle();
            scenario.onActivity(activity -> activity.getOnBackPressedDispatcher().onBackPressed());
            idle();
            scenario.onActivity(
                    activity -> {
                        assertSame(base[0], activity.currentScreen());
                        assertEquals("2005:4483", activity.currentScreen().getScreenId());
                        activity.getOnBackPressedDispatcher().onBackPressed();
                    });
            idle();
            scenario.onActivity(
                    activity -> assertEquals("2005:3812", activity.currentScreen().getScreenId()));
        }
    }

    @Test
    public void previousOnboardingPreferenceRemainsCompatible() {
        Context context = context();
        context.getSharedPreferences("cliente_preferences", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
        context.getSharedPreferences("mottainai_cliente_session", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_done", true)
                .commit();
        try {
            SessionManager session = new SessionManager(context);
            assertTrue(session.isOnboardingDone());
            session.setLocalSession(true);
            assertTrue(new SessionManager(context).hasLocalSession());
        } finally {
            context.getSharedPreferences("mottainai_cliente_session", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit();
            context.getSharedPreferences("cliente_preferences", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit();
        }
    }
}
