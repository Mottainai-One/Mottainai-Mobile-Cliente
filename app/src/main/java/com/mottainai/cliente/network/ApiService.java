package com.mottainai.cliente.network;

import com.mottainai.cliente.network.dto.CreateCustomerRequest;
import com.mottainai.cliente.network.dto.CustomerProfileResponse;
import com.mottainai.cliente.network.dto.CustomerTokenResponse;
import com.mottainai.cliente.network.dto.LoginRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * REST contract for the Mottainai API.
 *
 * Endpoint signatures are deliberately added only after the backend payloads are confirmed.
 */
public interface ApiService {
    @POST("api/v1/customers")
    Call<CustomerProfileResponse> register(@Body CreateCustomerRequest request);

    @POST("api/v1/customers/auth/login")
    Call<CustomerTokenResponse> login(@Body LoginRequest request);

    @GET("api/v1/customers/auth/profile")
    Call<CustomerProfileResponse> profile(@Header("Authorization") String authorization);
}
