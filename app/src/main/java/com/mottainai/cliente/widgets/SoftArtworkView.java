package com.mottainai.cliente.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.mottainai.cliente.R;

/** Native vector glow, rendered at the current view resolution on Android 8 and later. */
public final class SoftArtworkView extends AppCompatImageView {
    private final float sigma;
    private final float viewportWidth;
    private final float viewportHeight;
    private final Paint color = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int[] offset = new int[2];
    private Bitmap blurredAlpha;

    public SoftArtworkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.SoftArtworkView);
        sigma = values.getFloat(R.styleable.SoftArtworkView_artworkBlurSigma, 0);
        viewportWidth = values.getFloat(R.styleable.SoftArtworkView_artworkViewportWidth, 1);
        viewportHeight = values.getFloat(R.styleable.SoftArtworkView_artworkViewportHeight, 1);
        color.setColor(values.getColor(R.styleable.SoftArtworkView_artworkBlurColor, 0));
        values.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null || getWidth() == 0 || getHeight() == 0 || sigma <= 0) {
            super.onDraw(canvas);
            return;
        }
        if (blurredAlpha == null) {
            Bitmap pixels = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            super.onDraw(new Canvas(pixels));
            float scale =
                    Math.min(
                            (getWidth() - getPaddingLeft() - getPaddingRight()) / viewportWidth,
                            (getHeight() - getPaddingTop() - getPaddingBottom()) / viewportHeight);
            float radius = Math.max(0.01f, (sigma * scale - 0.5f) * 1.7320508f);
            Paint filter = new Paint();
            filter.setMaskFilter(new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL));
            blurredAlpha = pixels.extractAlpha(filter, offset);
            pixels.recycle();
        }
        canvas.drawBitmap(blurredAlpha, offset[0], offset[1], color);
    }

    private void clearCache() {
        if (blurredAlpha != null) {
            blurredAlpha.recycle();
            blurredAlpha = null;
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        clearCache();
    }

    @Override
    protected void onDetachedFromWindow() {
        clearCache();
        super.onDetachedFromWindow();
    }
}
