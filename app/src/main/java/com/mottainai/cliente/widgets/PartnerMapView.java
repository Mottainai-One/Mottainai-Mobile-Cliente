package com.mottainai.cliente.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.mottainai.cliente.R;
import com.mottainai.cliente.models.PartnerStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Stylized stand-in for a real map SDK: draws partner store pins over a flat
 * background at fixed percentage coordinates, matching the prototype design
 * (no tiles, no location permissions needed).
 */
public class PartnerMapView extends View {

    private final List<PartnerStore> stores = new ArrayList<>();
    private final Paint pinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pinDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float pinRadiusPx;
    private final float pinDotRadiusPx;

    public PartnerMapView(Context context) {
        this(context, null);
    }

    public PartnerMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PartnerMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pinPaint.setColor(ContextCompat.getColor(context, R.color.accent_red));
        pinDotPaint.setColor(ContextCompat.getColor(context, R.color.white));
        float density = context.getResources().getDisplayMetrics().density;
        pinRadiusPx = 8f * density;
        pinDotRadiusPx = 3f * density;
    }

    public void setStores(List<PartnerStore> newStores) {
        stores.clear();
        stores.addAll(newStores);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        for (PartnerStore store : stores) {
            float cx = store.getPinXPercent() * width;
            float cy = store.getPinYPercent() * height;
            canvas.drawCircle(cx, cy, pinRadiusPx, pinPaint);
            canvas.drawCircle(cx, cy, pinDotRadiusPx, pinDotPaint);
        }
    }
}
