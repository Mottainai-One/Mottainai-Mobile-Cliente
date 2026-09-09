package com.mottainai.cliente.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.models.ChatMessage;
import com.mottainai.cliente.models.Offer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BOT = 0;
    private static final int TYPE_USER = 1;
    private static final int TYPE_OFFER_CARD = 2;

    public interface OnOfferCardClickListener {
        void onOfferCardClick(Offer offer);
    }

    private final List<ChatMessage> messages = new ArrayList<>();
    private final OnOfferCardClickListener listener;

    public ChatAdapter(OnOfferCardClickListener listener) {
        this.listener = listener;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public int getMessageCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.hasAttachedOffer()) {
            return TYPE_OFFER_CARD;
        }
        return message.getSender() == ChatMessage.Sender.USER ? TYPE_USER : TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_USER:
                return new TextViewHolder(inflater.inflate(R.layout.item_chat_user, parent, false));
            case TYPE_OFFER_CARD:
                return new OfferCardViewHolder(inflater.inflate(R.layout.item_chat_offer_card, parent, false));
            default:
                return new TextViewHolder(inflater.inflate(R.layout.item_chat_bot, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof OfferCardViewHolder) {
            bindOfferCard((OfferCardViewHolder) holder, message.getAttachedOffer());
        } else if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).text.setText(message.getText());
        }
    }

    private void bindOfferCard(OfferCardViewHolder holder, Offer offer) {
        holder.title.setText(offer.getTitle());
        holder.meta.setText(String.format(Locale.getDefault(), "%s · %.1f km", offer.getStoreName(), offer.getDistanceKm()));
        holder.discount.setText(String.format(Locale.getDefault(), "-%d%%", offer.getDiscountPercent()));
        int thumbColor = ContextCompat.getColor(holder.itemView.getContext(), offer.getThumbnailColorRes());
        holder.thumb.setBackgroundTintList(ColorStateList.valueOf(thumbColor));
        holder.viewOfferButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOfferCardClick(offer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        final TextView text;

        TextViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_message_text);
        }
    }

    static class OfferCardViewHolder extends RecyclerView.ViewHolder {
        final View thumb;
        final TextView title;
        final TextView meta;
        final TextView discount;
        final View viewOfferButton;

        OfferCardViewHolder(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.view_card_thumb);
            title = itemView.findViewById(R.id.tv_card_title);
            meta = itemView.findViewById(R.id.tv_card_meta);
            discount = itemView.findViewById(R.id.tv_card_discount);
            viewOfferButton = itemView.findViewById(R.id.btn_view_offer);
        }
    }
}
