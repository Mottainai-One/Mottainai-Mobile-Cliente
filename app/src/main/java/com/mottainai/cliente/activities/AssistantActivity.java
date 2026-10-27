package com.mottainai.cliente.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.ChatAdapter;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.utils.ClientAccessGuard;
import com.mottainai.cliente.viewmodel.AssistantViewModel;

public class AssistantActivity extends AppCompatActivity {
    private AssistantViewModel viewModel;
    private ChatAdapter adapter;
    private RecyclerView recycler;
    private EditText input;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ClientAccessGuard.redirectIfRequired(this)) return;
        setContentView(R.layout.activity_assistant);
        viewModel = new ViewModelProvider(this).get(AssistantViewModel.class);
        input = findViewById(R.id.et_message);
        recycler = findViewById(R.id.rv_messages);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(offer -> {
            Intent intent = new Intent(this, OfferDetailActivity.class);
            intent.putExtra("offer_id", offer.getId());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);
        findViewById(R.id.btn_back).setOnClickListener(view -> finish());
        findViewById(R.id.btn_send).setOnClickListener(view -> {
            if (viewModel.send(input.getText().toString())) input.setText("");
        });
        input.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId != android.view.inputmethod.EditorInfo.IME_ACTION_SEND) return false;
            if (viewModel.send(input.getText().toString())) input.setText("");
            return true;
        });
        findViewById(R.id.chip_offers).setOnClickListener(view ->
                viewModel.send("Quais ofertas posso consultar?"));
        findViewById(R.id.chip_points).setOnClickListener(view ->
                viewModel.send("Como funcionam os pontos de fidelidade?"));
        findViewById(R.id.chip_impact).setOnClickListener(view ->
                viewModel.send("O que representa minha jornada no Mottainai?"));
        findViewById(R.id.btn_more).setContentDescription("Conversas e histórico");
        findViewById(R.id.btn_more).setOnClickListener(view -> showActions());
        viewModel.state().observe(this, state -> {
            if (state == null) return;
            adapter.setMessages(state.data);
            boolean available = viewModel.isAvailable() && !viewModel.isBusy();
            boolean canSend = available && !viewModel.isClosed();
            input.setEnabled(canSend);
            for (int id : new int[]{R.id.btn_send, R.id.chip_offers, R.id.chip_points, R.id.chip_impact}) {
                findViewById(id).setEnabled(canSend);
            }
            findViewById(R.id.btn_more).setEnabled(available);
            String prefix = viewModel.isDemo() ? "Demonstração · respostas locais. " : "";
            ((TextView) findViewById(R.id.assistant_availability)).setText(prefix
                    + (state.status == LoadState.Status.LOADING ? "Consultando o assistente…"
                    : state.status == LoadState.Status.ERROR ? state.message
                    : viewModel.isClosed() ? "Conversa encerrada · histórico somente leitura"
                    : "Escreva para conversar"));
            if (adapter.getMessageCount() > 0) {
                recycler.post(() -> recycler.smoothScrollToPosition(adapter.getMessageCount() - 1));
            }
        });
        viewModel.sessions().observe(this, sessions -> {
            if (sessions == null) return;
            viewModel.consumeSessionChoices();
            if (isFinishing()) return;
            if (sessions.isEmpty()) {
                new MaterialAlertDialogBuilder(this).setMessage("Nenhuma conversa anterior encontrada.")
                        .setPositiveButton("OK", null).show();
                return;
            }
            String[] labels = new String[sessions.size()];
            for (int index = 0; index < sessions.size(); index++) {
                String title = sessions.get(index).title;
                labels[index] = title == null || title.trim().isEmpty()
                        ? "Conversa " + sessions.get(index).sessionId.substring(0,
                        Math.min(8, sessions.get(index).sessionId.length())) : title;
            }
            new MaterialAlertDialogBuilder(this).setTitle("Conversas anteriores")
                    .setItems(labels, (dialog, which) -> viewModel.openConversation(sessions.get(which).sessionId))
                    .setNegativeButton("Cancelar", null).show();
        });
    }

    private void showActions() {
        if (viewModel.isDemo()) {
            new MaterialAlertDialogBuilder(this).setMessage("Iniciar outra conversa de demonstração?")
                    .setPositiveButton("Nova conversa", (dialog, which) -> viewModel.newConversation())
                    .setNegativeButton("Cancelar", null).show();
            return;
        }
        new MaterialAlertDialogBuilder(this).setTitle("Conversas")
                .setItems(new String[]{"Conversas anteriores", "Atualizar esta conversa", "Nova conversa"},
                        (dialog, which) -> {
                            if (which == 0) viewModel.loadSessions();
                            else if (which == 1) viewModel.loadHistory();
                            else viewModel.newConversation();
                        }).show();
    }

    @Override protected void onResume() {
        super.onResume();
        ClientAccessGuard.redirectIfRequired(this);
    }
}
