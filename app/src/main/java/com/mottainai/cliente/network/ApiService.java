package com.mottainai.cliente.network;

import com.mottainai.cliente.network.dto.CreateCustomerRequest;
import com.mottainai.cliente.network.dto.CustomerDetailsResponse;
import com.mottainai.cliente.network.dto.CustomerProfileResponse;
import com.mottainai.cliente.network.dto.CustomerTokenResponse;
import com.mottainai.cliente.network.dto.LoginRequest;
import com.mottainai.cliente.network.dto.UpdateCustomerRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

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

    @GET("api/v1/customers/{id}")
    Call<CustomerDetailsResponse> customer(@Path("id") String customerId);

    @PUT("api/v1/customers/{id}")
    Call<CustomerDetailsResponse> updateCustomer(@Path("id") String customerId,
                                                   @Body UpdateCustomerRequest request);
}
