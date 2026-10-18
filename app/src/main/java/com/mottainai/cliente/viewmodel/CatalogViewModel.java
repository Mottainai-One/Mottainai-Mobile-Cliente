package com.mottainai.cliente.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.cliente.network.dto.CatalogPage;
import com.mottainai.cliente.network.dto.CatalogPromotion;
import com.mottainai.cliente.network.dto.CatalogStore;
import com.mottainai.cliente.repository.CatalogRepository;
import com.mottainai.cliente.repository.CatalogPages;
import com.mottainai.cliente.repository.LoadState;

import java.util.Objects;

public class CatalogViewModel extends AndroidViewModel {
    private final CatalogRepository repository;
    private final MutableLiveData<LoadState<CatalogPage<CatalogPromotion>>> promotions = new MutableLiveData<>();
    private final MutableLiveData<LoadState<CatalogPage<CatalogStore>>> stores = new MutableLiveData<>();
    private final MutableLiveData<LoadState<CatalogPromotion>> detail = new MutableLiveData<>();
    private int promotionRequest;
    private int storeRequest;
    private CatalogPage<CatalogPromotion> promotionPages;
    private CatalogPage<CatalogStore> storePages;
    private String promotionStoreId;
    private String promotionQuery = "";
    private String storeQuery = "";
    private int promotionPage;
    private int storePage;
    private String detailId;
    private Double storeLatitude;
    private Double storeLongitude;

    public CatalogViewModel(@NonNull Application application) {
        super(application);
        repository = new CatalogRepository(application);
    }

    public LiveData<LoadState<CatalogPage<CatalogPromotion>>> promotions() { return promotions; }
    public LiveData<LoadState<CatalogPage<CatalogStore>>> stores() { return stores; }
    public LiveData<LoadState<CatalogPromotion>> detail() { return detail; }
    public String promotionStoreId() { return promotionStoreId; }
    public String promotionQuery() { return promotionQuery; }
    public int promotionPage() { return promotionPage; }
    public int storePage() { return storePage; }
    public int nextPromotionPage() { return promotionPages == null ? 0 : promotionPages.number + 1; }
    public int nextStorePage() { return storePages == null ? 0 : storePages.number + 1; }
    public Double storeLatitude() { return storeLatitude; }
    public Double storeLongitude() { return storeLongitude; }

    public void useStoreLocation(Double latitude, Double longitude) {
        if (latitude != null && (longitude == null || !Double.isFinite(latitude)
                || !Double.isFinite(longitude) || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180)) return;
        storeLatitude = latitude;
        storeLongitude = latitude == null ? null : longitude;
        storePages = null;
        storeRequest++;
        stores.setValue(null);
        loadStores(0, storeQuery);
    }

    public void loadPromotions(int page, String storeId, String query) {
        String normalized = query == null ? "" : query.trim();
        boolean sameFilter = Objects.equals(storeId, promotionStoreId) && normalized.equals(promotionQuery);
        LoadState<?> current = promotions.getValue();
        if (sameFilter && page == promotionPage && current != null
                && current.status == LoadState.Status.LOADING) return;
        if (page > 0 && (!sameFilter || promotionPages == null
                || page != promotionPages.number + 1)) return;
        if (!sameFilter) promotionPages = null;
        promotionStoreId = storeId;
        promotionQuery = normalized;
        promotionPage = page;
        int request = ++promotionRequest;
        promotions.setValue(LoadState.loading(promotionPages));
        repository.promotions(page, storeId, normalized, state -> {
            if (request != promotionRequest) return;
            if (state.status == LoadState.Status.SUCCESS && state.data.number == page) {
                promotionPages = CatalogPages.merge(promotionPages, state.data, item -> item.id);
                promotions.setValue(LoadState.success(promotionPages));
            } else {
                promotions.setValue(LoadState.error(state.message == null
                        ? "Não foi possível carregar esta página." : state.message,
                        state.httpStatus, promotionPages));
            }
        });
    }

    public void loadStores(int page, String query) {
        String normalized = query == null ? "" : query.trim();
        LoadState<?> current = stores.getValue();
        if (normalized.equals(storeQuery) && page == storePage && current != null
                && current.status == LoadState.Status.LOADING) return;
        if (page > 0 && (storePages == null || page != storePages.number + 1
                || !normalized.equals(storeQuery))) return;
        if (!normalized.equals(storeQuery)) storePages = null;
        storeQuery = normalized;
        storePage = page;
        int request = ++storeRequest;
        stores.setValue(LoadState.loading(storePages));
        repository.stores(page, normalized, storeLatitude, storeLongitude,
                storeLatitude == null ? null : 5.0, state -> {
            if (request != storeRequest) return;
            if (state.status == LoadState.Status.SUCCESS && state.data.number == page) {
                storePages = CatalogPages.merge(storePages, state.data, item -> item.id);
                stores.setValue(LoadState.success(storePages));
            } else {
                stores.setValue(LoadState.error(state.message == null
                        ? "Não foi possível carregar esta página." : state.message,
                        state.httpStatus, storePages));
            }
        });
    }

    public void loadPromotion(String id) {
        if (id.equals(detailId) && detail.getValue() != null
                && detail.getValue().status != LoadState.Status.ERROR) return;
        detailId = id;
        detail.setValue(LoadState.loading());
        repository.promotion(id, detail::setValue);
    }
}
