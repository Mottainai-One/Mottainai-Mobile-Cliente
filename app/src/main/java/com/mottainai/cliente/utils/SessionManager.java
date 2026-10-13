package com.mottainai.cliente.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Stores the minimum local state needed to route the client through protected screens.
 * Passwords and other sensitive profile data must never be persisted here.
 */
public class SessionManager {

    private static final String PREFS_NAME = Constants.SESSION_PREFERENCES_NAME;

    private final Context context;
    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().remove("auth_token").apply();
    }

    public boolean isOnboardingDone() {
        return preferences.getBoolean(Constants.KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone() {
        preferences.edit().putBoolean(Constants.KEY_ONBOARDING_DONE, true).apply();
    }

    public void saveClientSession(@NonNull String clientId) {
        if (clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("O ID do cliente é obrigatório para salvar a sessão.");
        }
        preferences.edit()
                .putString(Constants.KEY_CLIENT_ID, clientId)
                .putBoolean(Constants.KEY_SESSION_ACTIVE, true)
                .apply();
    }

    public boolean isLoggedIn() {
        FirebaseAuth auth = FirebaseSupport.authIfConfigured(context);
        return auth != null && auth.getCurrentUser() != null
                && preferences.getBoolean(Constants.KEY_SESSION_ACTIVE, false)
                && !isBlank(getClientId());
    }

    @Nullable
    public String getClientId() {
        return preferences.getString(Constants.KEY_CLIENT_ID, null);
    }

    /** Clears all authentication state in a single preferences transaction. */
    public void clearSession() {
        FirebaseAuth auth = FirebaseSupport.authIfConfigured(context);
        if (auth != null) auth.signOut();
        clearClientSession();
    }

    /** Clears the SQL profile association without changing the Firebase identity. */
    public void clearClientSession() {
        preferences.edit()
                .remove(Constants.KEY_CLIENT_ID)
                .remove(Constants.KEY_SESSION_ACTIVE)
                .apply();
    }

    private boolean isBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }
}
