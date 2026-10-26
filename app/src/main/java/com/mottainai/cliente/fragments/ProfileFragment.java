package com.mottainai.cliente.fragments;

import android.os.Bundle;

/** Native themed Profile screen from App Cliente / Ready for Dev. */
public class ProfileFragment extends ScreenFragment {
    public ProfileFragment() {
        Bundle args = new Bundle();
        args.putString("screen", "2005:4483");
        setArguments(args);
    }
}
