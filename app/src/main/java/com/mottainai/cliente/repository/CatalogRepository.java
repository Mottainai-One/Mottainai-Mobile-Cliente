package com.mottainai.cliente.repository;

import android.content.Context;

import com.mottainai.cliente.BuildConfig;
import com.mottainai.cliente.network.ApiClient;
import com.mottainai.cliente.network.CustomerCatalogService;
import com.mottainai.cliente.network.dto.CatalogPage;
import com.mottainai.cliente.network.dto.CatalogPromotion;
import com.mottainai.cliente.network.dto.CatalogStore;
import com.mottainai.cliente.utils.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatalogRepository {
    public interface Result<T> {
        void accept(LoadState<T> state);
    }

    private final CustomerCatalogService service;
    private final SessionManager session;

    public CatalogRepository(Context context) {
        session = new SessionManager(context);
        service = BuildConfig.API_BASE_URL.trim().isEmpty() ? null
                : ApiClient.create(context.getApplicationContext(), BuildConfig.API_BASE_URL,
                CustomerCatalogService.class);
    }

    public void stores(int page, String query, Result<CatalogPage<CatalogStore>> result) {
        if (session.isDemoMode()) {
            String normalized = normalize(query);
            List<CatalogStore> stores = new ArrayList<>();
            for (CatalogStore store : DemoData.stores()) {
                String address = store.address == null ? "" : store.address.street + " " + store.address.city;
                if (normalized.isEmpty() || (store.name + " " + address)
                        .toLowerCase(Locale.ROOT).contains(normalized)) stores.add(store);
            }
            result.accept(LoadState.success(page(stores, page, 20)));
            return;
        }
        if (unavailable(result)) return;
        enqueue(service.stores(page, 20, blankToNull(query)), result);
    }

    public void store(String id, Result<CatalogStore> result) {
        if (session.isDemoMode()) {
            for (CatalogStore store : DemoData.stores()) {
                if (store.id.equals(id)) {
                    result.accept(LoadState.success(store));
                    return;
                }
            }
            result.accept(LoadState.error("Conteúdo indisponível.", 404));
            return;
        }
        if (unavailable(result)) return;
        enqueue(service.store(id), result);
    }

    public void promotions(int page, String storeId, String query,
                           Result<CatalogPage<CatalogPromotion>> result) {
        if (session.isDemoMode()) {
            String normalized = normalize(query);
            List<CatalogPromotion> promotions = new ArrayList<>();
            for (CatalogPromotion promotion : DemoData.promotions()) {
                boolean matchesStore = storeId == null || storeId.equals(promotion.store.id);
                String searchable = promotion.name + " " + promotion.description + " "
                        + promotion.store.name + " " + promotion.items.get(0).name;
                boolean matchesQuery = normalized.isEmpty()
                        || searchable.toLowerCase(Locale.ROOT).contains(normalized);
                if (matchesStore && matchesQuery) promotions.add(promotion);
            }
            result.accept(LoadState.success(page(promotions, page, 20)));
            return;
        }
        if (unavailable(result)) return;
        enqueue(service.promotions(page, 20, blankToNull(storeId), blankToNull(query)), result);
    }

    public void promotion(String id, Result<CatalogPromotion> result) {
        if (session.isDemoMode()) {
            CatalogPromotion promotion = DemoData.promotion(id);
            result.accept(promotion == null ? LoadState.error("Conteúdo indisponível.", 404)
                    : LoadState.success(promotion));
            return;
        }
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

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static <T> CatalogPage<T> page(List<T> items, int requestedPage, int size) {
        CatalogPage<T> page = new CatalogPage<>();
        int number = Math.max(0, requestedPage);
        long start = (long) number * size;
        int from = (int) Math.min(start, items.size());
        int to = Math.min(from + size, items.size());
        page.content = new ArrayList<>(items.subList(from, to));
        page.number = number;
        page.size = size;
        page.totalElements = items.size();
        page.totalPages = (items.size() + size - 1) / size;
        return page;
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
