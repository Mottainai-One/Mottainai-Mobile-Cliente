package com.mottainai.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mottainai.cliente.activities.AssistantActivity;
import com.mottainai.cliente.activities.LoginClienteActivity;
import com.mottainai.cliente.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override protected void onStart() {
        super.onStart();
        if (!isFinishing() && !new SessionManager(this).canBrowse()) {
            Intent login = new Intent(this, LoginClienteActivity.class);
            login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(login);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        if (!session.canBrowse()) {
            Intent loginIntent = new Intent(this, LoginClienteActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        TextView demoBanner = findViewById(R.id.demo_banner);
        demoBanner.setVisibility(session.isDemoMode() ? View.VISIBLE : View.GONE);
        View assistantButton = findViewById(R.id.fab_assistant);
        assistantButton.setVisibility(View.VISIBLE);
        assistantButton.setOnClickListener(v ->
                startActivity(new Intent(this, AssistantActivity.class)));

        applyWindowInsets(bottomNav);
    }

    private void applyWindowInsets(View bottomNav) {
        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(),
                    bottomNav.getPaddingRight(), bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
