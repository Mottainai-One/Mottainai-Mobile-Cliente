package com.mottainai.cliente.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mottainai.cliente.MainActivity;
import com.mottainai.cliente.R;
import com.mottainai.cliente.utils.ClientAccessGuard;
import com.mottainai.cliente.utils.ClientAccessState;
import com.mottainai.cliente.utils.SessionManager;

/** Explains the location requirement without repeatedly opening a system permission prompt. */
public final class LocationAccessActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String[]> permission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> refresh());

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_access);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.location_access_root), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(findViewById(R.id.location_access_root));
        findViewById(R.id.location_access_allow).setOnClickListener(view -> {
            if (!ClientAccessGuard.hasLocationPermission(this)) {
                permission.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION});
            } else if (!ClientAccessGuard.isLocationEnabled(this)) {
                openSettings(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } else refresh();
        });
        findViewById(R.id.location_access_settings).setOnClickListener(view ->
                openSettings(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()))));
        findViewById(R.id.location_access_retry).setOnClickListener(view -> refresh());
        findViewById(R.id.location_access_logout).setOnClickListener(view -> {
            new SessionManager(this).clearSession();
            refresh();
        });
    }

    @Override protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        if (isFinishing()) return;
        ClientAccessState state = ClientAccessGuard.state(this);
        if (state != ClientAccessState.LOCATION_REQUIRED) {
            Class<?> target = state == ClientAccessState.READY ? MainActivity.class : LoginClienteActivity.class;
            startActivity(new Intent(this, target)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        ((TextView) findViewById(R.id.location_access_status)).setText(
                ClientAccessGuard.hasLocationPermission(this)
                        ? R.string.location_access_device_off : R.string.location_access_permission_missing);
        ((TextView) findViewById(R.id.location_access_allow)).setText(
                ClientAccessGuard.hasLocationPermission(this)
                        ? R.string.location_access_enable_device : R.string.location_access_allow);
    }

    private void openSettings(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException unavailable) {
            Toast.makeText(this, R.string.location_access_settings_unavailable, Toast.LENGTH_LONG).show();
        }
    }
}
