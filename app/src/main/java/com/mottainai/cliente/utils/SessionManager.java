package com.mottainai.cliente.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** Local session and appearance state, retaining keys used by previous installations. */
public class SessionManager {

    private static final String PREFS_NAME = "mottainai_cliente_session";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences =
                context.getSharedPreferences("cliente_preferences", Context.MODE_PRIVATE);
        SharedPreferences previous = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!preferences.contains("introduction_seen")
                && previous.getBoolean(KEY_ONBOARDING_DONE, false)) {
            preferences.edit().putBoolean("introduction_seen", true).apply();
        }
    }

    public boolean isOnboardingDone() {
        return preferences.getBoolean("introduction_seen", false);
    }

    public void setOnboardingDone() {
        preferences.edit().putBoolean("introduction_seen", true).apply();
    }

    public SharedPreferences preferences() {
        return preferences;
    }

    public int getTheme() {
        return preferences.getInt(
                "theme", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setTheme(int mode) {
        preferences.edit().putInt("theme", mode).apply();
    }

    public boolean hasLocalSession() {
        // Keep the persisted key for compatibility; this is not remote authentication.
        return preferences.getBoolean("demo_session", false);
    }

    public void setLocalSession(boolean active) {
        preferences.edit().putBoolean("demo_session", active).apply();
    }

    public void removePrototypeIdentity() {
        if (preferences.getBoolean("fixtures_removed_v1", false)) return;
        SharedPreferences.Editor editor =
                preferences.edit().putBoolean("fixtures_removed_v1", true);
        for (String key : new String[] {"field_Nome completo", "field_E-mail", "field_Telefone"}) {
            String value = preferences.getString(key, "");
            if (value.equals("Camila Oliveira")
                    || value.equals("camila@exemplo.com")
                    || value.equals("(11) 90000-0000")) editor.remove(key);
        }
        editor.apply();
    }
}
