package com.mottainai.cliente;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.PictureDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.zip.ZipFile;

/** The shipped interface must use native Android resources and no SVG renderer. */
@RunWith(AndroidJUnit4.class)
public class NativeResourcesTest {
    @Test
    public void apkContainsNoSvgFilesOrSvgRenderer() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        try (ZipFile apk = new ZipFile(context.getApplicationInfo().sourceDir)) {
            assertFalse(
                    "SVG exports must not be packaged",
                    apk.stream()
                            .anyMatch(
                                    entry ->
                                            entry.getName()
                                                    .toLowerCase(Locale.ROOT)
                                                    .endsWith(".svg")));
        }
        try {
            Class.forName("com.caverock.androidsvg.SVG", false, context.getClassLoader());
            fail("The application must not depend on an SVG rendering library");
        } catch (ClassNotFoundException expected) {
            // Native Android drawables render the artwork.
        }
    }

    @Test
    public void screensUseNativeImageDrawables() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, MainActivity.class).putExtra("screen", "2005:3812");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> verifyImages(activity.currentScreen().requireView()));
        }
    }

    private void verifyImages(View view) {
        if (view instanceof ImageView) {
            assertFalse(
                    "Image views must not hold parsed SVG pictures",
                    ((ImageView) view).getDrawable() instanceof PictureDrawable);
        }
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int index = 0; index < parent.getChildCount(); index++)
                verifyImages(parent.getChildAt(index));
        }
    }
}
