package com.mottainai.cliente.utils;

import android.content.res.ColorStateList;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mottainai.cliente.R;

public final class ProductImages {
    private ProductImages() {}

    public static void load(ImageView view, String url, int color, String title) {
        String source = ProductImageSource.httpsUrl(url);
        view.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(view.getContext(), color)));
        view.setContentDescription(source == null ? "Imagem indisponível: " + title
                : "Imagem do produto: " + title);
        Glide.with(view).load(source).placeholder(R.drawable.ic_leaf)
                .error(R.drawable.ic_leaf).fallback(R.drawable.ic_leaf).fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(view);
    }
}
