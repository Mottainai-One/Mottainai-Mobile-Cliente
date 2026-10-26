package com.mottainai.cliente.fragments;

import android.os.Bundle;

/** Native themed Home screen from App Cliente / Ready for Dev. */
public class HomeFragment extends ScreenFragment {
    public HomeFragment() {
        Bundle args = new Bundle();
        args.putString("screen", "2005:3812");
        setArguments(args);
    }
}
