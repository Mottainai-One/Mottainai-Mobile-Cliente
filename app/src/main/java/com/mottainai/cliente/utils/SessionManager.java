package com.mottainai.cliente.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Stores the minimum local state needed to route the client through protected screens.
 * Passwords and other sensitive profile data must never be persisted here.
 */
public class SessionManager {

    private static final String PREFS_NAME = Constants.SESSION_PREFERENCES_NAME;

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isOnboardingDone() {
        return preferences.getBoolean(Constants.KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone() {
        preferences.edit().putBoolean(Constants.KEY_ONBOARDING_DONE, true).apply();
    }

    public void saveSession(@NonNull String authToken, @NonNull String clientId) {
        if (authToken.trim().isEmpty() || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Token e clientId são obrigatórios para salvar a sessão.");
        }
        preferences.edit()
                .putString(Constants.KEY_AUTH_TOKEN, authToken)
                .putString(Constants.KEY_CLIENT_ID, clientId)
                .putBoolean(Constants.KEY_SESSION_ACTIVE, true)
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(Constants.KEY_SESSION_ACTIVE, false)
                && !isBlank(getAuthToken())
                && !isBlank(getClientId());
    }

    @Nullable
    public String getAuthToken() {
        return preferences.getString(Constants.KEY_AUTH_TOKEN, null);
    }

    @Nullable
    public String getClientId() {
        return preferences.getString(Constants.KEY_CLIENT_ID, null);
    }

    /** Clears all authentication state in a single preferences transaction. */
    public void clearSession() {
        preferences.edit()
                .remove(Constants.KEY_AUTH_TOKEN)
                .remove(Constants.KEY_CLIENT_ID)
                .remove(Constants.KEY_SESSION_ACTIVE)
                .apply();
    }

    /** Prevents an old in-flight response from invalidating a newer login. */
    public synchronized boolean clearSessionIfTokenMatches(@NonNull String token) {
        if (!token.equals(getAuthToken()) || !isLoggedIn()) {
            return false;
        }
        clearSession();
        return true;
    }

    private boolean isBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }
}
