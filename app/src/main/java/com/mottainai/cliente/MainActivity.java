package com.mottainai.cliente;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.mottainai.cliente.fragments.ScreenFragment;
import com.mottainai.cliente.repository.ScreenRepository;
import com.mottainai.cliente.utils.SessionManager;

/** Hosts the existing Navigation graph and panels above its current screen. */
public class MainActivity extends AppCompatActivity {
    private SessionManager session;
    private NavHostFragment navigationHost;
    private NavController navigation;
    private boolean artworkFullBleed;

    @Override
    protected void onCreate(Bundle state) {
        session = new SessionManager(this);
        AppCompatDelegate.setDefaultNightMode(session.getTheme());
        super.onCreate(state);
        if (!isDesignPreview()) session.removePrototypeIdentity();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        navigationHost =
                (NavHostFragment)
                        getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navigation = navigationHost.getNavController();

        String initial = getIntent().getStringExtra("screen");
        if (initial == null)
            initial =
                    session.hasLocalSession()
                            ? "2005:3812"
                            : session.isOnboardingDone() ? "2028:4492" : "2542:29599";
        ScreenRepository repository = ScreenRepository.get(this);
        initial = repository.available(initial) ? repository.canonical(initial) : "2005:3812";
        boolean overlay = repository.screen(initial).optBoolean("overlay");
        String start = overlay ? overlayBase(initial) : initial;

        Bundle arguments = new Bundle();
        arguments.putString("screen", start);
        navigation.setGraph(R.navigation.nav_graph);
        applyWindowInsets();
        if (state == null) {
            showScreen(start, false);
            if (overlay) showScreen(initial, true);
        }
        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new androidx.activity.OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                FragmentManager manager = getSupportFragmentManager();
                                if (manager.findFragmentById(R.id.design_overlay) != null) {
                                    manager.popBackStackImmediate(
                                            "overlay", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                } else if (!navigation.popBackStack()) {
                                    finish();
                                }
                            }
                        });
    }

    private void applyWindowInsets() {
        boolean dark =
                (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                        == Configuration.UI_MODE_NIGHT_YES;
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(!dark);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(!dark);
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.design_content),
                (view, insets) -> {
                    Insets bars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                                            | WindowInsetsCompat.Type.displayCutout());
                    Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
                    view.setPadding(
                            bars.left,
                            artworkFullBleed ? 0 : bars.top,
                            bars.right,
                            artworkFullBleed
                                    ? keyboard.bottom
                                    : Math.max(bars.bottom, keyboard.bottom));
                    return insets;
                });
    }

    public void showScreen(String id, boolean history) {
        ScreenRepository repository = ScreenRepository.get(this);
        if (!repository.available(id)) return;
        String canonical = repository.canonical(id);
        if (!isDesignPreview() && canonical.equals("2557:30792")) {
            ScreenFragment current = currentScreen();
            if (current != null) {
                current.requestNotificationPermission();
                return;
            }
            canonical = "2557:30760";
        }
        View focused = getCurrentFocus();
        if (focused != null) {
            InputMethodManager keyboard =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(R.id.design_overlay) != null)
            manager.popBackStackImmediate("overlay", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (repository.screen(canonical).optBoolean("overlay")) {
            manager.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.design_overlay, ScreenFragment.create(canonical), canonical)
                    .addToBackStack("overlay")
                    .commit();
            return;
        }
        Bundle arguments = new Bundle();
        arguments.putString("screen", canonical);
        NavOptions.Builder options = new NavOptions.Builder();
        if (!history && navigation.getCurrentDestination() != null)
            options.setPopUpTo(navigation.getCurrentDestination().getId(), true);
        navigation.navigate(destinationFor(canonical), arguments, options.build());
    }

    private int destinationFor(String screen) {
        switch (screen) {
            case "2005:3812":
                return R.id.homeFragment;
            case "2005:3914":
                return R.id.catalogFragment;
            case "2005:4305":
                return R.id.storesFragment;
            case "2093:6331":
                return R.id.pointsFragment;
            case "2005:4483":
                return R.id.profileFragment;
            case "2143:11079":
                return R.id.impactFragment;
            default:
                return R.id.screenFragment;
        }
    }

    public ScreenFragment currentScreen() {
        Fragment overlay = getSupportFragmentManager().findFragmentById(R.id.design_overlay);
        Fragment content = navigationHost.getChildFragmentManager().getPrimaryNavigationFragment();
        return (ScreenFragment) (overlay != null ? overlay : content);
    }

    public boolean isDesignPreview() {
        return BuildConfig.DEBUG && getIntent().getBooleanExtra("design_preview", false);
    }

    private String overlayBase(String id) {
        String name = ScreenRepository.get(this).screen(id).optString("name");
        if (name.contains("Foto do perfil")) return "2022:4219";
        if (name.contains("Fidelidade")) return "2093:6331";
        if (name.contains("Sair da conta")) return "2005:4483";
        if (name.contains("Filtros")) return "2005:3914";
        if (name.contains("Conversa")) return "2055:5379";
        if (name.contains("Cadastro")) return "2143:9538";
        return "2005:3812";
    }

    public void setAppearance(boolean dark) {
        int mode = dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        session.setTheme(mode);
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public void setArtworkFullBleed(boolean fullBleed) {
        artworkFullBleed = fullBleed;
        ViewCompat.requestApplyInsets(findViewById(R.id.design_content));
    }

    public void setLocalSession(boolean active) {
        session.setLocalSession(active);
        if (!active) navigation.popBackStack(R.id.nav_graph, false);
    }
}
