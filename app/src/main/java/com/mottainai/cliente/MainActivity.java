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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mottainai.cliente.activities.AssistantActivity;
import com.mottainai.cliente.utils.SessionManager;
import com.mottainai.cliente.utils.ClientAccessGuard;
import com.mottainai.cliente.viewmodel.CatalogViewModel;

public class MainActivity extends AppCompatActivity {
    private CatalogViewModel catalogViewModel;

    @Override protected void onResume() {
        super.onResume();
        if (ClientAccessGuard.redirectIfRequired(this)) return;
        if (catalogViewModel != null) catalogViewModel.requestLocation();
    }

    @Override protected void onStop() {
        if (catalogViewModel != null) catalogViewModel.cancelLocationRequest();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ClientAccessGuard.redirectIfRequired(this)) return;
        SessionManager session = new SessionManager(this);
        catalogViewModel = new ViewModelProvider(this).get(CatalogViewModel.class);

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
