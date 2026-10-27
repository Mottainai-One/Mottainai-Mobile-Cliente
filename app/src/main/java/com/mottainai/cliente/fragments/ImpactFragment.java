package com.mottainai.cliente.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.mottainai.cliente.R;
import com.mottainai.cliente.network.dto.LoyaltyAccount;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.utils.LoyaltyPresentation;
import com.mottainai.cliente.utils.SessionManager;
import com.mottainai.cliente.viewmodel.LoyaltyViewModel;

public class ImpactFragment extends Fragment {
    private LoyaltyViewModel viewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_impact, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(LoyaltyViewModel.class);
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());
        view.findViewById(R.id.impact_retry).setOnClickListener(v -> viewModel.loadAccount());
        viewModel.account().observe(getViewLifecycleOwner(), state -> render(view, state));
        if (viewModel.account().getValue() == null) viewModel.loadAccount();
    }

    private void render(View view, LoadState<LoyaltyAccount> state) {
        if (state == null) return;
        LoyaltyAccount account = state.data;
        boolean demo = new SessionManager(requireContext()).isDemoMode();
        ((TextView) view.findViewById(R.id.impact_points)).setText(
                LoyaltyPresentation.points(account == null ? null : account.pointsBalance));
        ((TextView) view.findViewById(R.id.impact_level)).setText(LoyaltyPresentation.status(account));
        ((TextView) view.findViewById(R.id.impact_next_level)).setText(LoyaltyPresentation.nextStatus(account));
        String status = state.status == LoadState.Status.LOADING ? "Carregando sua jornada…"
                : state.status == LoadState.Status.ERROR ? state.message
                : account == null || account.pointsBalance == null ? "Saldo não informado pela API."
                : "Pontuação recebida do sistema; o aplicativo não concede pontos nem calcula níveis.";
        ((TextView) view.findViewById(R.id.impact_availability)).setText(
                (demo ? "Demonstração · dados fictícios. " : "") + status);
        view.findViewById(R.id.impact_retry).setEnabled(state.status != LoadState.Status.LOADING);
        View share = view.findViewById(R.id.btn_share_impact);
        boolean confirmed = state.status == LoadState.Status.SUCCESS && account != null
                && account.pointsBalance != null;
        share.setEnabled(confirmed);
        share.setOnClickListener(v -> {
            if (!confirmed) return;
            String text = (demo ? "Demonstração Mottainai — dados fictícios: " : "Minha jornada Mottainai: ")
                    + LoyaltyPresentation.points(account.pointsBalance)
                    + " de fidelidade em compras confirmadas. Um incentivo ao consumo consciente, não uma medição ambiental.";
            Intent intent = new Intent(Intent.ACTION_SEND).setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, text);
            try {
                startActivity(Intent.createChooser(intent, "Compartilhar minha jornada"));
            } catch (ActivityNotFoundException unavailable) {
                Toast.makeText(requireContext(), "Nenhum aplicativo disponível para compartilhar.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
