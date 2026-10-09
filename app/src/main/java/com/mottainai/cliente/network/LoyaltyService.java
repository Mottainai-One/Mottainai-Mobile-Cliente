package com.mottainai.cliente.network;

import com.mottainai.cliente.network.dto.LoyaltyAccount;
import com.mottainai.cliente.network.dto.LoyaltyTransaction;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LoyaltyService {
    @GET("api/v1/customers/{id}/loyalty")
    Call<LoyaltyAccount> account(@Path("id") String customerId);

    @GET("api/v1/customers/{id}/loyalty/transactions")
    Call<List<LoyaltyTransaction>> transactions(@Path("id") String customerId,
                                                  @Query("from") String from,
                                                  @Query("to") String to);
}
