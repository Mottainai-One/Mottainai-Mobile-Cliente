package com.mottainai.cliente.network;

import com.mottainai.cliente.network.dto.CreateFirebaseCustomerProfileRequest;
import com.mottainai.cliente.network.dto.CustomerProfileResponse;
import com.mottainai.cliente.network.dto.UpdateFirebaseCustomerProfileRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * REST contract for the Mottainai API.
 *
 * Client endpoints use Firebase ID tokens injected by ApiClient.
 */
public interface ApiService {
    @GET("api/v1/client/auth/profile")
    Call<CustomerProfileResponse> profile();

    @POST("api/v1/client/auth/profile")
    Call<CustomerProfileResponse> createProfile(@Body CreateFirebaseCustomerProfileRequest request);

    @PUT("api/v1/client/auth/profile")
    Call<CustomerProfileResponse> updateProfile(@Body UpdateFirebaseCustomerProfileRequest request);
}
