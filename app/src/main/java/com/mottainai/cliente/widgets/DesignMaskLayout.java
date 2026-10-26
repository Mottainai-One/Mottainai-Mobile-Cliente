package com.mottainai.cliente.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.content.res.AppCompatResources;

import com.mottainai.cliente.R;

/** Composes native drawable masks over proportional artwork layers. */
public class DesignMaskLayout extends DesignArtLayout {
    private final Drawable mask;
    private final float maskX;
    private final float maskY;
    private final float maskWidth;
    private final float maskHeight;
    private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DesignMaskLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.DesignMaskLayout);
        int resource = values.getResourceId(R.styleable.DesignMaskLayout_designMask, 0);
        maskX = values.getFloat(R.styleable.DesignMaskLayout_maskX, 0);
        maskY = values.getFloat(R.styleable.DesignMaskLayout_maskY, 0);
        maskWidth = values.getFloat(R.styleable.DesignMaskLayout_maskWidth, 20);
        maskHeight = values.getFloat(R.styleable.DesignMaskLayout_maskHeight, 20);
        values.recycle();
        mask = AppCompatResources.getDrawable(context, resource);
        if (mask == null) throw new IllegalStateException("Missing native artwork mask");
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int layer = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        super.dispatchDraw(canvas);
        int maskLayer = canvas.saveLayer(0, 0, getWidth(), getHeight(), maskPaint);
        float scale = getWidth() / designWidth;
        int width = Math.max(1, mask.getIntrinsicWidth());
        int height = Math.max(1, mask.getIntrinsicHeight());
        canvas.translate(maskX * scale, maskY * scale);
        canvas.scale(maskWidth * scale / width, maskHeight * scale / height);
        mask.setBounds(0, 0, width, height);
        mask.draw(canvas);
        canvas.restoreToCount(maskLayer);
        canvas.restoreToCount(layer);
    }
}
