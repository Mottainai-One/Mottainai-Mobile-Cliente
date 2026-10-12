package com.mottainai.cliente.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.ChatAdapter;
import com.mottainai.cliente.models.ChatMessage;

public class AssistantActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        RecyclerView recyclerView = findViewById(R.id.rv_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ChatAdapter adapter = new ChatAdapter(offer -> {
            Intent intent = new Intent(this, OfferDetailActivity.class);
            intent.putExtra("offer_id", offer.getId());
            startActivity(intent);
        });
        adapter.addMessage(new ChatMessage(ChatMessage.Sender.BOT,
                "O chat ainda não está conectado à API. Suas mensagens só poderão ser enviadas "
                        + "quando o endpoint oficial estiver disponível."));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
