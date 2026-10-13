package com.mottainai.cliente.utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public final class FirebaseSupport {

    private FirebaseSupport() {
    }

    @Nullable
    public static FirebaseAuth authIfConfigured(Context context) {
        if (FirebaseApp.getApps(context.getApplicationContext()).isEmpty()) return null;
        try {
            return FirebaseAuth.getInstance();
        } catch (IllegalStateException exception) {
            return null;
        }
    }
}
