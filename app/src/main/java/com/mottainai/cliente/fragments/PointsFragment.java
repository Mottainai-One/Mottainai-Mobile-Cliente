package com.mottainai.cliente.fragments;

import android.os.Bundle;

/** Native themed Points screen from App Cliente / Ready for Dev. */
public class PointsFragment extends ScreenFragment {
    public PointsFragment() {
        Bundle args = new Bundle();
        args.putString("screen", "2093:6331");
        setArguments(args);
    }
}
