package com.mottainai.cliente.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mottainai.cliente.R;
import com.mottainai.cliente.activities.LoginClienteActivity;
import com.mottainai.cliente.network.dto.CustomerProfileResponse;
import com.mottainai.cliente.utils.SessionManager;
import com.mottainai.cliente.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.row_your_data).setVisibility(View.GONE);
        view.findViewById(R.id.row_preferences).setVisibility(View.GONE);
        view.findViewById(R.id.row_help).setVisibility(View.GONE);

        ProfileViewModel model = new ViewModelProvider(this).get(ProfileViewModel.class);
        TextView name = view.findViewById(R.id.tv_profile_name);
        TextView email = view.findViewById(R.id.tv_profile_level);
        TextView initials = view.findViewById(R.id.tv_avatar_initials);
        TextView status = view.findViewById(R.id.tv_profile_status);
        View retry = view.findViewById(R.id.btn_profile_retry);
        model.state().observe(getViewLifecycleOwner(), current -> {
            if (current == null) return;
            retry.setVisibility(current.kind == ProfileViewModel.State.Kind.ERROR
                    ? View.VISIBLE : View.GONE);
            if (current.kind == ProfileViewModel.State.Kind.LOADING) {
                status.setText("Carregando perfil…");
            } else if (current.kind == ProfileViewModel.State.Kind.ERROR) {
                status.setText(current.message);
            } else if (current.kind == ProfileViewModel.State.Kind.READY) {
                CustomerProfileResponse profile = current.profile;
                String fullName = profile.fullName == null ? "Cliente" : profile.fullName.trim();
                name.setText(fullName.isEmpty() ? "Cliente" : fullName);
                email.setText(profile.email == null ? "" : profile.email);
                initials.setText(fullName.isEmpty() ? "C"
                        : fullName.substring(0, 1).toUpperCase(java.util.Locale.ROOT));
                status.setText("");
            }
        });
        retry.setOnClickListener(v -> model.load());
        if (model.state().getValue() == null
                || model.state().getValue().kind == ProfileViewModel.State.Kind.IDLE) {
            model.load();
        }

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new SessionManager(requireContext()).clearSession();
            Intent login = new Intent(requireContext(), LoginClienteActivity.class);
            login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(login);
        });
    }
}
