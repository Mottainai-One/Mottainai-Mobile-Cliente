package com.mottainai.cliente.network;

import com.mottainai.cliente.network.dto.CatalogPage;
import com.mottainai.cliente.network.dto.CatalogPromotion;
import com.mottainai.cliente.network.dto.CatalogStore;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CustomerCatalogService {
    @GET("api/v1/customer-catalog/stores")
    Call<CatalogPage<CatalogStore>> stores(@Query("page") int page, @Query("size") int size,
                                           @Query("query") String query);

    @GET("api/v1/customer-catalog/stores/{id}")
    Call<CatalogStore> store(@Path("id") String id);

    @GET("api/v1/customer-catalog/promotions")
    Call<CatalogPage<CatalogPromotion>> promotions(@Query("page") int page, @Query("size") int size,
                                                     @Query("storeId") String storeId,
                                                     @Query("query") String query);

    @GET("api/v1/customer-catalog/promotions/{id}")
    Call<CatalogPromotion> promotion(@Path("id") String id);
}
