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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.PointsActivityAdapter;
import com.mottainai.cliente.models.PointsActivityEntry;
import com.mottainai.cliente.network.dto.LoyaltyAccount;
import com.mottainai.cliente.network.dto.LoyaltyTransaction;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.utils.SessionManager;
import com.mottainai.cliente.viewmodel.LoyaltyViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PointsFragment extends Fragment {

    private LoyaltyViewModel viewModel;
    private PointsActivityAdapter adapter;
    private String customerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_points, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LoyaltyViewModel.class);
        customerId = new SessionManager(requireContext()).getClientId();
        RecyclerView recyclerView = view.findViewById(R.id.rv_activity);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PointsActivityAdapter();
        recyclerView.setAdapter(adapter);
        view.findViewById(R.id.points_balance_retry).setOnClickListener(v ->
                viewModel.loadAccount(customerId));
        view.findViewById(R.id.points_history_retry).setOnClickListener(v ->
                viewModel.loadRecentTransactions(customerId));
        viewModel.account().observe(getViewLifecycleOwner(), state -> renderBalance(view, state));
        viewModel.transactions().observe(getViewLifecycleOwner(), state -> renderHistory(view, state));
        if (customerId != null && !customerId.isEmpty()) {
            viewModel.loadAccount(customerId);
            viewModel.loadRecentTransactions(customerId);
        } else {
            ((TextView) view.findViewById(R.id.points_balance_status))
                    .setText("Entre na sua conta para ver seus pontos.");
            ((TextView) view.findViewById(R.id.points_history_status))
                    .setText("Entre na sua conta para ver seus lançamentos.");
        }

        view.findViewById(R.id.btn_see_impact).setVisibility(View.GONE);
    }

    private void renderBalance(View view, LoadState<LoyaltyAccount> state) {
        if (state == null) return;
        TextView balance = view.findViewById(R.id.tv_points_balance);
        TextView status = view.findViewById(R.id.points_balance_status);
        View retry = view.findViewById(R.id.points_balance_retry);
        if (state.status == LoadState.Status.LOADING) {
            status.setText("Carregando saldo...");
            retry.setVisibility(View.GONE);
        } else if (state.status == LoadState.Status.ERROR) {
            balance.setText("— pts");
            status.setText(state.message);
            retry.setVisibility(View.VISIBLE);
        } else {
            if (state.data.pointsBalance == null) {
                balance.setText("— pts");
                status.setText("Saldo indisponível no momento.");
            } else {
                balance.setText(String.format(Locale.forLanguageTag("pt-BR"),
                        "%,d pts", state.data.pointsBalance));
                status.setText("");
            }
            retry.setVisibility(View.GONE);
        }
    }

    private void renderHistory(View view, LoadState<List<LoyaltyTransaction>> state) {
        if (state == null) return;
        TextView status = view.findViewById(R.id.points_history_status);
        View retry = view.findViewById(R.id.points_history_retry);
        if (state.status == LoadState.Status.LOADING) {
            status.setText("Carregando lançamentos...");
            retry.setVisibility(View.GONE);
        } else if (state.status == LoadState.Status.ERROR) {
            status.setText(state.message);
            retry.setVisibility(View.VISIBLE);
        } else {
            List<PointsActivityEntry> entries = new ArrayList<>();
            for (LoyaltyTransaction transaction : state.data) {
                String label = transaction.description == null || transaction.description.isEmpty()
                        ? transaction.transactionType : transaction.description;
                if (transaction.createdAt != null && transaction.createdAt.length() >= 10) {
                    String date = transaction.createdAt.substring(8, 10) + "/"
                            + transaction.createdAt.substring(5, 7);
                    label = (label == null ? "Lançamento" : label) + " · " + date;
                }
                entries.add(new PointsActivityEntry(label == null ? "Lançamento" : label,
                        transaction.points == null ? 0 : transaction.points,
                        R.color.card_light_green));
            }
            adapter.setEntries(entries);
            status.setText(entries.isEmpty() ? "Nenhum lançamento nos últimos seis meses." : "");
            retry.setVisibility(View.GONE);
        }
    }
}
