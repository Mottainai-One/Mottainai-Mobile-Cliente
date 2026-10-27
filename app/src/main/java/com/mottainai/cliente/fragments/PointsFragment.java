package com.mottainai.cliente.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.PointsActivityAdapter;
import com.mottainai.cliente.models.PointsActivityEntry;
import com.mottainai.cliente.network.dto.LoyaltyAccount;
import com.mottainai.cliente.network.dto.LoyaltyTransaction;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.viewmodel.LoyaltyViewModel;
import com.mottainai.cliente.utils.LoyaltyPresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PointsFragment extends Fragment {

    private LoyaltyViewModel viewModel;
    private PointsActivityAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_points, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(LoyaltyViewModel.class);
        RecyclerView recyclerView = view.findViewById(R.id.rv_activity);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PointsActivityAdapter();
        recyclerView.setAdapter(adapter);
        view.findViewById(R.id.points_balance_retry).setOnClickListener(v ->
                viewModel.loadAccount());
        view.findViewById(R.id.points_history_retry).setOnClickListener(v ->
                viewModel.loadRecentTransactions());
        viewModel.account().observe(getViewLifecycleOwner(), state -> renderBalance(view, state));
        viewModel.transactions().observe(getViewLifecycleOwner(), state -> renderHistory(view, state));
        if (viewModel.account().getValue() == null) viewModel.loadAccount();
        if (viewModel.transactions().getValue() == null) viewModel.loadRecentTransactions();

        View impactButton = view.findViewById(R.id.btn_see_impact);
        impactButton.setVisibility(View.VISIBLE);
        impactButton.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_pointsFragment_to_impactFragment));
    }

    private void renderBalance(View view, LoadState<LoyaltyAccount> state) {
        if (state == null) return;
        TextView balance = view.findViewById(R.id.tv_points_balance);
        TextView status = view.findViewById(R.id.points_balance_status);
        View retry = view.findViewById(R.id.points_balance_retry);
        balance.setText(LoyaltyPresentation.points(state.data == null ? null : state.data.pointsBalance));
        if (state.status == LoadState.Status.LOADING) {
            status.setText("Carregando saldo...");
            retry.setVisibility(View.GONE);
        } else if (state.status == LoadState.Status.ERROR) {
            status.setText(state.message);
            retry.setVisibility(View.VISIBLE);
        } else {
            if (state.data == null || state.data.pointsBalance == null) {
                balance.setText("— pts");
                status.setText("Saldo indisponível no momento.");
            } else {
                balance.setText(LoyaltyPresentation.points(state.data.pointsBalance));
                status.setText("");
            }
            retry.setVisibility(View.GONE);
        }
    }

    private void renderHistory(View view, LoadState<List<LoyaltyTransaction>> state) {
        if (state == null) return;
        TextView status = view.findViewById(R.id.points_history_status);
        View retry = view.findViewById(R.id.points_history_retry);
        boolean complete = state.data == null || showTransactions(state.data);
        if (state.status == LoadState.Status.LOADING) {
            status.setText("Carregando lançamentos...");
            retry.setVisibility(View.GONE);
        } else if (state.status == LoadState.Status.ERROR) {
            status.setText(state.message);
            retry.setVisibility(View.VISIBLE);
        } else {
            status.setText(!complete ? "Alguns lançamentos retornaram incompletos. Tente atualizar o histórico."
                    : state.data.isEmpty() ? "Nenhum lançamento nos últimos seis meses." : "");
            retry.setVisibility(View.GONE);
        }
    }

    private boolean showTransactions(List<LoyaltyTransaction> transactions) {
        List<PointsActivityEntry> entries = new ArrayList<>();
        boolean complete = true;
        for (LoyaltyTransaction transaction : transactions) {
            if (transaction == null || transaction.points == null) {
                complete = false;
                continue;
            }
            String label = transaction.description == null || transaction.description.isEmpty()
                    ? transaction.transactionType : transaction.description;
            if (transaction.createdAt != null && transaction.createdAt.length() >= 10) {
                try {
                    String date = LocalDate.parse(transaction.createdAt.substring(0, 10))
                            .format(DateTimeFormatter.ofPattern("dd/MM", Locale.forLanguageTag("pt-BR")));
                    label = (label == null ? "Lançamento" : label) + " · " + date;
                } catch (DateTimeParseException exception) {
                    complete = false;
                }
            }
            entries.add(new PointsActivityEntry(label == null ? "Lançamento" : label,
                    transaction.points, R.color.card_light_green));
        }
        adapter.setEntries(entries);
        return complete;
    }
}
