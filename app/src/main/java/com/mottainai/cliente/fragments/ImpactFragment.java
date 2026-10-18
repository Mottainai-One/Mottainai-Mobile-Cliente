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
import androidx.navigation.Navigation;

import com.mottainai.cliente.R;
import com.mottainai.cliente.models.UserProfile;
import com.mottainai.cliente.repository.MockUserRepository;
import com.mottainai.cliente.utils.SessionManager;

import java.util.Locale;

public class ImpactFragment extends Fragment {

    private final MockUserRepository userRepository = new MockUserRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_impact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());
        boolean demo = new SessionManager(requireContext()).isDemoMode();
        view.findViewById(R.id.impact_demo_content).setVisibility(demo ? View.VISIBLE : View.GONE);
        ((TextView) view.findViewById(R.id.impact_availability)).setText(demo
                ? "Demonstração · métricas fictícias"
                : "Seu impacto ainda não está disponível. As métricas serão exibidas quando houver dados confirmados no sistema.");
        if (!demo) return;

        UserProfile user = userRepository.getCurrentUser();

        TextView kgAvoided = view.findViewById(R.id.tv_kg_avoided);
        kgAvoided.setText(String.format(Locale.getDefault(), "%.1f kg", user.getKgAvoided()).replace('.', ','));

        TextView itemsSaved = view.findViewById(R.id.tv_items_saved);
        itemsSaved.setText(String.valueOf(user.getItemsSavedCount()));

        TextView moneySaved = view.findViewById(R.id.tv_money_saved);
        moneySaved.setText(String.format(Locale.getDefault(), "R$ %.0f", user.getMoneySavedBrl()));

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        view.findViewById(R.id.btn_share_impact).setOnClickListener(v -> {
            String shareText = "Demonstração Mottainai — dados fictícios: " + kgAvoided.getText() + " de desperdício evitado.";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Compartilhar meu impacto"));
        });
    }
}
