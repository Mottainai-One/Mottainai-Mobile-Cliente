package com.mottainai.cliente.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.mottainai.cliente.models.PartnerStore;

public final class MapNavigation {
    private MapNavigation() { }

    public static void openRoute(Context context, PartnerStore store) {
        String destination = store.hasValidLocation()
                ? store.getLatitude() + "," + store.getLongitude() + " (" + store.getName() + ")"
                : store.getAddressLabel();
        if (destination.isEmpty() || "Endereço indisponível".equals(destination)) {
            Toast.makeText(context, "Esta loja ainda não possui endereço para traçar a rota.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=" + Uri.encode(destination))));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(context, "Nenhum app de mapas disponível.", Toast.LENGTH_SHORT).show();
        }
    }
}
