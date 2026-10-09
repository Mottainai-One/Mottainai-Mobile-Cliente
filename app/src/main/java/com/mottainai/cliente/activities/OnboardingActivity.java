package com.mottainai.cliente.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.mottainai.cliente.MainActivity;
import com.mottainai.cliente.R;
import com.mottainai.cliente.utils.SessionManager;

/** Launcher activity that displays the intro once before routing by session state. */
public class OnboardingActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isOnboardingDone()) {
            goToNextScreen();
            return;
        }

        setContentView(R.layout.activity_onboarding);
        findViewById(R.id.btn_get_started).setOnClickListener(v -> {
            sessionManager.setOnboardingDone();
            goToNextScreen();
        });
    }

    private void goToNextScreen() {
        Class<?> destination = sessionManager.isLoggedIn()
                ? MainActivity.class
                : LoginClienteActivity.class;
        startActivity(new Intent(this, destination));
        finish();
    }
}
