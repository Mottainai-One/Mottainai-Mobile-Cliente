package com.mottainai.cliente.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.mottainai.cliente.MainActivity;
import com.mottainai.cliente.R;
import com.mottainai.cliente.utils.SessionManager;

/** Launcher activity: shows the intro once, then MainActivity takes over on every later launch. */
public class OnboardingActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isOnboardingDone()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_onboarding);
        findViewById(R.id.btn_get_started).setOnClickListener(v -> {
            sessionManager.setOnboardingDone();
            goToMain();
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
