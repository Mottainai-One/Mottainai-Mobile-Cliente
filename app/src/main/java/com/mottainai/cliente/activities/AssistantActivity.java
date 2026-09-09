package com.mottainai.cliente.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.ChatAdapter;
import com.mottainai.cliente.models.ChatMessage;
import com.mottainai.cliente.repository.AssistantReplyEngine;
import com.mottainai.cliente.repository.MockOfferRepository;
import com.mottainai.cliente.repository.MockUserRepository;

/** The "Lô" assistant chat. Replies are generated locally by {@link AssistantReplyEngine}. */
public class AssistantActivity extends AppCompatActivity {

    private static final long BOT_REPLY_DELAY_MS = 600;

    private final AssistantReplyEngine replyEngine = new AssistantReplyEngine(
            new MockOfferRepository(), new MockUserRepository());
    private final Handler handler = new Handler(Looper.getMainLooper());

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        messageField = findViewById(R.id.et_message);
        recyclerView = findViewById(R.id.rv_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatAdapter = new ChatAdapter(offer -> {
            Intent intent = new Intent(this, OfferDetailActivity.class);
            intent.putExtra("offer_id", offer.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(chatAdapter);

        chatAdapter.addMessage(new ChatMessage(ChatMessage.Sender.BOT,
                "Oi, Maria! Como posso ajudar? Posso buscar ofertas, consultar seus pontos ou explicar seu impacto."));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_send).setOnClickListener(v -> sendTypedMessage());

        findViewById(R.id.chip_offers).setOnClickListener(v ->
                sendUserMessage("Quero uma oferta de café perto de mim"));
        findViewById(R.id.chip_points).setOnClickListener(v ->
                sendUserMessage("Quantos pontos eu tenho?"));
        findViewById(R.id.chip_impact).setOnClickListener(v ->
                sendUserMessage("Qual é o meu impacto até agora?"));
    }

    private void sendTypedMessage() {
        String text = messageField.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        messageField.setText("");
        sendUserMessage(text);
    }

    private void sendUserMessage(String text) {
        chatAdapter.addMessage(new ChatMessage(ChatMessage.Sender.USER, text));
        scrollToBottom();
        handler.postDelayed(() -> {
            chatAdapter.addMessage(replyEngine.reply(text));
            scrollToBottom();
        }, BOT_REPLY_DELAY_MS);
    }

    private void scrollToBottom() {
        recyclerView.post(() -> recyclerView.smoothScrollToPosition(chatAdapter.getMessageCount() - 1));
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
