package com.mottainai.cliente.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mottainai.cliente.models.PartnerStore;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenStreetMap tiles and markers positioned at the coordinates supplied by the API.
 * Stores without coordinates remain in the list rather than receiving invented pins.
 */
public class PartnerMapView extends FrameLayout {

    private final MapView map;
    private final List<Marker> markers = new ArrayList<>();

    public PartnerMapView(Context context) {
        this(context, null);
    }

    public PartnerMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PartnerMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Configuration.getInstance().setUserAgentValue(context.getPackageName());
        map = new MapView(context);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(13.0);
        addView(map, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void setStores(List<PartnerStore> newStores) {
        map.getOverlays().removeAll(markers);
        markers.clear();
        double north = -90, south = 90, east = -180, west = 180;
        for (PartnerStore store : newStores) {
            Double latitude = store.getLatitude();
            Double longitude = store.getLongitude();
            if (!store.hasValidLocation()) {
                continue;
            }
            GeoPoint location = new GeoPoint(latitude, longitude);
            Marker marker = new Marker(map);
            marker.setPosition(location);
            marker.setTitle(store.getName());
            marker.setSnippet(store.getAddressLabel());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(marker);
            markers.add(marker);
            north = Math.max(north, latitude);
            south = Math.min(south, latitude);
            east = Math.max(east, longitude);
            west = Math.min(west, longitude);
        }
        map.invalidate();
        if (markers.isEmpty()) return;
        if (markers.size() == 1) {
            map.getController().setZoom(15.0);
            map.getController().setCenter(markers.get(0).getPosition());
        } else {
            BoundingBox bounds = new BoundingBox(north, east, south, west);
            map.post(() -> map.zoomToBoundingBox(bounds, true, 48));
        }
    }

    public void resumeMap() { map.onResume(); }
    public void pauseMap() { map.onPause(); }
    public void releaseMap() { map.onDetach(); }
}
