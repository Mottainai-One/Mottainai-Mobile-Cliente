package com.mottainai.cliente.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mottainai.cliente.R;
import com.mottainai.cliente.models.UserProfile;
import com.mottainai.cliente.repository.MockUserRepository;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private final MockUserRepository userRepository = new MockUserRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserProfile user = userRepository.getCurrentUser();

        ((TextView) view.findViewById(R.id.tv_avatar_initials)).setText(user.getInitials());
        ((TextView) view.findViewById(R.id.tv_profile_name)).setText(user.getName());
        ((TextView) view.findViewById(R.id.tv_profile_level)).setText(String.format(Locale.forLanguageTag("pt-BR"),
                "%s · %,d pontos", user.getLevelLabel(), user.getPointsBalance()));

        View.OnClickListener pending = v ->
                Toast.makeText(requireContext(), "Em desenvolvimento", Toast.LENGTH_SHORT).show();
        view.findViewById(R.id.row_your_data).setOnClickListener(pending);
        view.findViewById(R.id.row_preferences).setOnClickListener(pending);
        view.findViewById(R.id.row_help).setOnClickListener(pending);

        view.findViewById(R.id.btn_logout).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Sessão encerrada", Toast.LENGTH_SHORT).show());
    }
}
