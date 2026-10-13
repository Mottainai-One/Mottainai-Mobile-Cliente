package com.mottainai.cliente.network;

import com.mottainai.cliente.network.dto.LoyaltyAccount;
import com.mottainai.cliente.network.dto.LoyaltyTransaction;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LoyaltyService {
    @GET("api/v1/client/loyalty/balance")
    Call<LoyaltyAccount> account();

    @GET("api/v1/client/loyalty/transactions")
    Call<List<LoyaltyTransaction>> transactions(@Query("from") String from,
                                                  @Query("to") String to);
}
