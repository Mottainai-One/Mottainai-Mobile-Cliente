package com.mottainai.cliente.fragments;

import android.os.Bundle;

/** Native themed Catalog screen from App Cliente / Ready for Dev. */
public class CatalogFragment extends ScreenFragment {
    public CatalogFragment() {
        Bundle args = new Bundle();
        args.putString("screen", "2005:3914");
        setArguments(args);
    }
}
