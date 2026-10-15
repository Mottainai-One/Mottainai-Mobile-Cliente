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
import com.mottainai.cliente.repository.LoadState;

public class CatalogViewModel extends AndroidViewModel {
    private final CatalogRepository repository;
    private final MutableLiveData<LoadState<CatalogPage<CatalogPromotion>>> promotions = new MutableLiveData<>();
    private final MutableLiveData<LoadState<CatalogPage<CatalogStore>>> stores = new MutableLiveData<>();
    private final MutableLiveData<LoadState<CatalogPromotion>> detail = new MutableLiveData<>();
    private int promotionRequest;
    private int storeRequest;

    public CatalogViewModel(@NonNull Application application) {
        super(application);
        repository = new CatalogRepository(application);
    }

    public LiveData<LoadState<CatalogPage<CatalogPromotion>>> promotions() { return promotions; }
    public LiveData<LoadState<CatalogPage<CatalogStore>>> stores() { return stores; }
    public LiveData<LoadState<CatalogPromotion>> detail() { return detail; }

    public void loadPromotions(int page, String storeId, String query) {
        int request = ++promotionRequest;
        promotions.setValue(LoadState.loading());
        repository.promotions(page, storeId, query, state -> {
            if (request == promotionRequest) promotions.setValue(state);
        });
    }

    public void loadStores(int page, String query) {
        int request = ++storeRequest;
        stores.setValue(LoadState.loading());
        repository.stores(page, query, state -> {
            if (request == storeRequest) stores.setValue(state);
        });
    }

    public void loadPromotion(String id) {
        detail.setValue(LoadState.loading());
        repository.promotion(id, detail::setValue);
    }
}
