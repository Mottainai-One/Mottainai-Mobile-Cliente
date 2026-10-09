package com.mottainai.cliente.repository;

import android.content.Context;

import com.mottainai.cliente.BuildConfig;
import com.mottainai.cliente.network.ApiClient;
import com.mottainai.cliente.network.CustomerCatalogService;
import com.mottainai.cliente.network.dto.CatalogPage;
import com.mottainai.cliente.network.dto.CatalogPromotion;
import com.mottainai.cliente.network.dto.CatalogStore;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatalogRepository {
    public interface Result<T> {
        void accept(LoadState<T> state);
    }

    private final CustomerCatalogService service;

    public CatalogRepository(Context context) {
        service = BuildConfig.API_BASE_URL.trim().isEmpty() ? null
                : ApiClient.create(context.getApplicationContext(), BuildConfig.API_BASE_URL,
                CustomerCatalogService.class);
    }

    public void stores(int page, String query, Result<CatalogPage<CatalogStore>> result) {
        if (unavailable(result)) return;
        enqueue(service.stores(page, 20, blankToNull(query)), result);
    }

    public void store(String id, Result<CatalogStore> result) {
        if (unavailable(result)) return;
        enqueue(service.store(id), result);
    }

    public void promotions(int page, String storeId, String query,
                           Result<CatalogPage<CatalogPromotion>> result) {
        if (unavailable(result)) return;
        enqueue(service.promotions(page, 20, blankToNull(storeId), blankToNull(query)), result);
    }

    public void promotion(String id, Result<CatalogPromotion> result) {
        if (unavailable(result)) return;
        enqueue(service.promotion(id), result);
    }

    private <T> boolean unavailable(Result<T> result) {
        if (service != null) return false;
        result.accept(LoadState.error("Serviço indisponível. Tente mais tarde.", 0));
        return true;
    }

    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static <T> void enqueue(Call<T> call, Result<T> result) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.accept(LoadState.success(response.body()));
                } else {
                    result.accept(LoadState.error(response.code() == 404
                            ? "Conteúdo indisponível." : "Não foi possível carregar os dados.",
                            response.code()));
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable throwable) {
                result.accept(LoadState.error(throwable instanceof IOException
                        ? "Sem conexão. Verifique sua internet." : "Não foi possível carregar os dados.", 0));
            }
        });
    }
}
