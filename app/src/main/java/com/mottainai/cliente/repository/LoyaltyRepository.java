package com.mottainai.cliente.repository;

import android.content.Context;

import com.mottainai.cliente.BuildConfig;
import com.mottainai.cliente.network.ApiClient;
import com.mottainai.cliente.network.LoyaltyService;
import com.mottainai.cliente.network.dto.LoyaltyAccount;
import com.mottainai.cliente.network.dto.LoyaltyTransaction;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoyaltyRepository {
    private final LoyaltyService service;

    public LoyaltyRepository(Context context) {
        service = BuildConfig.API_BASE_URL.trim().isEmpty() ? null
                : ApiClient.create(context.getApplicationContext(), BuildConfig.API_BASE_URL,
                LoyaltyService.class);
    }

    public void account(String customerId, CatalogRepository.Result<LoyaltyAccount> result) {
        if (unavailable(result)) return;
        enqueue(service.account(customerId), result);
    }

    public void transactions(String customerId, LocalDate month,
                             CatalogRepository.Result<List<LoyaltyTransaction>> result) {
        if (unavailable(result)) return;
        LocalDate from = month.withDayOfMonth(1);
        LocalDateTime start = from.atStartOfDay();
        // The API's Between query includes both bounds; avoid duplicating midnight in the next window.
        LocalDateTime end = from.plusMonths(1).atStartOfDay().minusNanos(1);
        enqueue(service.transactions(customerId, start.toString(), end.toString()), result);
    }

    private <T> boolean unavailable(CatalogRepository.Result<T> result) {
        if (service != null) return false;
        result.accept(LoadState.error("Serviço indisponível. Tente mais tarde.", 0));
        return true;
    }

    private static <T> void enqueue(Call<T> call, CatalogRepository.Result<T> result) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.accept(LoadState.success(response.body()));
                } else {
                    result.accept(LoadState.error("Não foi possível carregar seus pontos.", response.code()));
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable throwable) {
                result.accept(LoadState.error(throwable instanceof IOException
                        ? "Sem conexão. Verifique sua internet."
                        : "Não foi possível carregar seus pontos.", 0));
            }
        });
    }
}
