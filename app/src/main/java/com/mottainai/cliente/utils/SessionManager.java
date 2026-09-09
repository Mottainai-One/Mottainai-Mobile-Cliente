package com.mottainai.cliente.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** Tracks whether the user has already been through the onboarding intro. */
public class SessionManager {

    private static final String PREFS_NAME = "mottainai_cliente_session";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isOnboardingDone() {
        return preferences.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone() {
        preferences.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply();
    }
}
