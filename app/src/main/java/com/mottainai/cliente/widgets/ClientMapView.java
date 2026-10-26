package com.mottainai.cliente.widgets;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.mottainai.cliente.R;
import com.mottainai.cliente.utils.ScreenContent;

import java.util.Locale;

/** A real map centered only on an authorized device location; no fictitious partner pin. */
public final class ClientMapView extends LinearLayout {
    public interface LocationPermissionRequester {
        void requestLocationPermission(ClientMapView map);
    }

    private final FrameLayout viewport;
    private final TextView status;
    private final Button locate;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LocationManager manager;
    private LocationListener listener;
    private WebView web;

    public ClientMapView(Context context, LocationPermissionRequester permissionRequester) {
        super(context);
        setOrientation(VERTICAL);
        setMinimumHeight(dp(320));
        viewport = new FrameLayout(getContext());
        addView(viewport, new LayoutParams(-1, dp(260)));
        status =
                ScreenContent.label(
                        getContext(), getContext().getString(R.string.design_map_location), 16);
        status.setGravity(android.view.Gravity.CENTER);
        status.setPadding(dp(16), dp(16), dp(16), dp(16));
        viewport.addView(status, new FrameLayout.LayoutParams(-1, -1));
        locate = new Button(getContext());
        locate.setText(R.string.design_map_location);
        locate.setAllCaps(false);
        styleButton(locate, true);
        addView(locate, buttonParams());
        locate.setOnClickListener(
                v -> {
                    if (ContextCompat.checkSelfPermission(
                                    getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) onPermissionResult(true);
                    else permissionRequester.requestLocationPermission(this);
                });
        Button external = new Button(getContext());
        external.setText(R.string.design_map_open);
        external.setAllCaps(false);
        styleButton(external, false);
        addView(external, buttonParams());
        external.setOnClickListener(v -> openMaps(getContext()));
        post(
                () ->
                        loadMap(
                                "https://www.openstreetmap.org/export/embed.html?bbox=-180,-70,180,80&layer=mapnik"));
        if (ContextCompat.checkSelfPermission(
                        getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) post(() -> onPermissionResult(true));
    }

    public void onPermissionResult(boolean allowed) {
        if (!isAttachedToWindow()) return;
        if (!allowed) {
            status.setText(R.string.design_map_permission_denied);
            return;
        }
        stopLocation();
        status.setText(R.string.design_map_loading);
        manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            Location recent = null;
            String provider = null;
            for (String candidate : manager.getProviders(true)) {
                if (candidate.equals(LocationManager.PASSIVE_PROVIDER)) continue;
                if (candidate.equals(LocationManager.GPS_PROVIDER)
                        && ContextCompat.checkSelfPermission(
                                        getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) continue;
                Location last = manager.getLastKnownLocation(candidate);
                if (last != null
                        && (recent == null
                                || last.getElapsedRealtimeNanos()
                                        > recent.getElapsedRealtimeNanos())) recent = last;
                if (provider == null || candidate.equals(LocationManager.NETWORK_PROVIDER))
                    provider = candidate;
            }
            if (recent != null
                    && android.os.SystemClock.elapsedRealtimeNanos()
                                    - recent.getElapsedRealtimeNanos()
                            < 15L * 60 * 1000000000) {
                showLocation(recent);
                return;
            }
            if (provider == null) {
                status.setText(R.string.design_map_location_unavailable);
                return;
            }
            listener =
                    new LocationListener() {
                        public void onLocationChanged(Location location) {
                            stopLocation();
                            showLocation(location);
                        }

                        public void onProviderEnabled(String p) {}

                        public void onProviderDisabled(String p) {}

                        public void onStatusChanged(String p, int s, Bundle b) {}
                    };
            manager.requestLocationUpdates(provider, 0, 0, listener, Looper.getMainLooper());
            handler.postDelayed(
                    () -> {
                        if (listener != null) {
                            stopLocation();
                            status.setText(R.string.design_map_location_unavailable);
                        }
                    },
                    12000);
        } catch (SecurityException | IllegalArgumentException exception) {
            stopLocation();
            status.setText(R.string.design_map_location_unavailable);
        }
    }

    private void showLocation(Location location) {
        double lat = location.getLatitude(), lon = location.getLongitude(), delta = .025;
        loadMap(
                String.format(
                        Locale.ROOT,
                        "https://www.openstreetmap.org/export/embed.html?bbox=%.6f,%.6f,%.6f,%.6f&layer=mapnik&marker=%.6f,%.6f",
                        lon - delta,
                        lat - delta,
                        lon + delta,
                        lat + delta,
                        lat,
                        lon));
        locate.setText(R.string.design_map_location);
    }

    private void loadMap(String url) {
        if (!isAttachedToWindow()) return;
        if (web == null) {
            web = new WebView(getContext());
            web.getSettings().setJavaScriptEnabled(true);
            web.getSettings().setAllowFileAccess(false);
            web.getSettings().setAllowContentAccess(false);
            web.setVisibility(INVISIBLE);
            web.setWebViewClient(
                    new WebViewClient() {
                        @Override
                        public void onPageCommitVisible(WebView view, String url) {
                            view.setVisibility(VISIBLE);
                            status.setVisibility(GONE);
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(
                                WebView view, WebResourceRequest request) {
                            Uri url = request.getUrl();
                            if ("www.openstreetmap.org".equals(url.getHost())
                                    && url.getPath() != null
                                    && url.getPath().startsWith("/export/embed")) return false;
                            if ("https".equals(url.getScheme()))
                                try {
                                    getContext().startActivity(new Intent(Intent.ACTION_VIEW, url));
                                } catch (android.content.ActivityNotFoundException ignored) {
                                }
                            return true;
                        }

                        @Override
                        public void onReceivedError(
                                WebView view,
                                WebResourceRequest request,
                                android.webkit.WebResourceError error) {
                            if (request.isForMainFrame()) {
                                view.setVisibility(GONE);
                                status.setText(R.string.design_map_error);
                                status.setVisibility(VISIBLE);
                            }
                        }
                    });
            viewport.addView(web, new FrameLayout.LayoutParams(-1, -1));
            web.setOnTouchListener(
                    (v, event) -> {
                        v.getParent()
                                .requestDisallowInterceptTouchEvent(
                                        event.getAction() != android.view.MotionEvent.ACTION_UP);
                        return false;
                    });
        }
        status.setText(R.string.design_map_loading);
        status.setVisibility(VISIBLE);
        web.setVisibility(INVISIBLE);
        web.loadUrl(url);
    }

    private LayoutParams buttonParams() {
        LayoutParams params = new LayoutParams(-1, dp(56));
        params.topMargin = dp(12);
        return params;
    }

    private void styleButton(Button button, boolean primary) {
        int color = ContextCompat.getColor(getContext(), R.color.design_primary);
        android.graphics.drawable.GradientDrawable shape =
                new android.graphics.drawable.GradientDrawable();
        shape.setCornerRadius(dp(16));
        shape.setColor(primary ? color : android.graphics.Color.TRANSPARENT);
        if (!primary) shape.setStroke(dp(1), color);
        button.setBackground(shape);
        button.setTextColor(
                primary ? ContextCompat.getColor(getContext(), R.color.design_on_primary) : color);
        button.setTypeface(
                androidx.core.content.res.ResourcesCompat.getFont(getContext(), R.font.dm_sans));
        button.setFontVariationSettings("'wght' 700, 'opsz' 14");
        button.setTextSize(15);
        button.setPadding(dp(12), dp(8), dp(12), dp(8));
    }

    private void stopLocation() {
        handler.removeCallbacksAndMessages(null);
        if (manager != null && listener != null) manager.removeUpdates(listener);
        listener = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        stopLocation();
        if (web != null) {
            web.stopLoading();
            viewport.removeView(web);
            web.destroy();
            web = null;
        }
        super.onDetachedFromWindow();
    }

    public static void openMaps(Context context) {
        Intent intent =
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=" + Uri.encode("supermercados próximos")));
        try {
            context.startActivity(intent);
        } catch (android.content.ActivityNotFoundException exception) {
            try {
                context.startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://www.google.com/maps/search/?api=1&query=supermercados")));
            } catch (android.content.ActivityNotFoundException ignored) {
            }
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
