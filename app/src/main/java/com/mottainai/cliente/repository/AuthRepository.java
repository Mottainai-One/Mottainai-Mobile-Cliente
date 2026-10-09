package com.mottainai.cliente.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mottainai.cliente.BuildConfig;
import com.mottainai.cliente.network.ApiClient;
import com.mottainai.cliente.network.ApiService;
import com.mottainai.cliente.network.dto.CreateCustomerRequest;
import com.mottainai.cliente.network.dto.CustomerProfileResponse;
import com.mottainai.cliente.network.dto.CustomerTokenResponse;
import com.mottainai.cliente.network.dto.LoginRequest;
import com.mottainai.cliente.network.dto.ViaCepAddress;
import com.mottainai.cliente.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public final class AuthRepository {
    public interface Result<T> {
        void success(T value);
        void error(String message);
    }

    private interface ViaCepService {
        @GET("ws/{cep}/json/")
        Call<ViaCepAddress> find(@Path("cep") String cep);
    }

    private final ApiService api;
    private final ViaCepService viaCep;
    private final SessionManager session;

    public AuthRepository(Context context) {
        session = new SessionManager(context);
        api = BuildConfig.API_BASE_URL.trim().isEmpty()
                ? null : ApiClient.create(context, BuildConfig.API_BASE_URL);
        viaCep = new Retrofit.Builder()
                .baseUrl("https://viacep.com.br/")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ViaCepService.class);
    }

    public void login(String email, String password, Result<Void> result) {
        if (api == null) {
            result.error("Configure MOTTAINAI_API_BASE_URL para conectar o aplicativo à API.");
            return;
        }
        api.login(new LoginRequest(email, password)).enqueue(new Callback<CustomerTokenResponse>() {
            @Override public void onResponse(@NonNull Call<CustomerTokenResponse> call,
                                             @NonNull Response<CustomerTokenResponse> response) {
                CustomerTokenResponse body = response.body();
                if (!response.isSuccessful() || body == null || body.accessToken == null
                        || body.accessToken.trim().isEmpty()) {
                    result.error(response.code() == 401 ? "E-mail ou senha incorretos."
                            : "Não foi possível entrar (HTTP " + response.code() + ").");
                    return;
                }
                api.profile("Bearer " + body.accessToken).enqueue(new Callback<CustomerProfileResponse>() {
                    @Override public void onResponse(@NonNull Call<CustomerProfileResponse> profileCall,
                                                     @NonNull Response<CustomerProfileResponse> profileResponse) {
                        CustomerProfileResponse profile = profileResponse.body();
                        if (!profileResponse.isSuccessful() || profile == null || profile.id == null) {
                            result.error("Não foi possível carregar seu perfil. Tente entrar novamente.");
                            return;
                        }
                        session.saveSession(body.accessToken, String.valueOf(profile.id));
                        result.success(null);
                    }
                    @Override public void onFailure(@NonNull Call<CustomerProfileResponse> profileCall,
                                                    @NonNull Throwable throwable) {
                        result.error("Falha de rede ao carregar seu perfil. Tente novamente.");
                    }
                });
            }
            @Override public void onFailure(@NonNull Call<CustomerTokenResponse> call,
                                            @NonNull Throwable throwable) {
                result.error("Falha de rede ao entrar. Confira sua conexão e tente novamente.");
            }
        });
    }

    public void register(CreateCustomerRequest request, Result<Void> result) {
        if (api == null) {
            result.error("Configure MOTTAINAI_API_BASE_URL para conectar o aplicativo à API.");
            return;
        }
        api.register(request).enqueue(new Callback<CustomerProfileResponse>() {
            @Override public void onResponse(@NonNull Call<CustomerProfileResponse> call,
                                             @NonNull Response<CustomerProfileResponse> response) {
                if (response.code() == 201) {
                    result.success(null);
                } else if (response.code() == 409) {
                    result.error("CPF ou e-mail já cadastrado.");
                } else {
                    result.error("Não foi possível cadastrar (HTTP " + response.code() + "). Confira os dados.");
                }
            }
            @Override public void onFailure(@NonNull Call<CustomerProfileResponse> call,
                                            @NonNull Throwable throwable) {
                result.error("Falha de rede ao cadastrar. Tente novamente.");
            }
        });
    }

    public void loadProfile(Result<CustomerProfileResponse> result) {
        if (api == null) {
            result.error("Configure MOTTAINAI_API_BASE_URL para consultar seu perfil.");
            return;
        }
        String token = session.getAuthToken();
        if (!session.isLoggedIn() || token == null) {
            result.error("Sua sessão terminou. Entre novamente.");
            return;
        }
        api.profile("Bearer " + token).enqueue(new Callback<CustomerProfileResponse>() {
            @Override public void onResponse(@NonNull Call<CustomerProfileResponse> call,
                                             @NonNull Response<CustomerProfileResponse> response) {
                CustomerProfileResponse body = response.body();
                if (response.isSuccessful() && body != null && body.id != null) {
                    result.success(body);
                } else {
                    result.error("Não foi possível carregar seu perfil (HTTP " + response.code() + ").");
                }
            }
            @Override public void onFailure(@NonNull Call<CustomerProfileResponse> call,
                                            @NonNull Throwable throwable) {
                result.error("Falha de rede ao carregar seu perfil.");
            }
        });
    }

    public void lookupCep(String cep, Result<ViaCepAddress> result) {
        viaCep.find(cep).enqueue(new Callback<ViaCepAddress>() {
            @Override public void onResponse(@NonNull Call<ViaCepAddress> call,
                                             @NonNull Response<ViaCepAddress> response) {
                ViaCepAddress body = response.body();
                if (!response.isSuccessful() || body == null || Boolean.TRUE.equals(body.error)
                        || body.logradouro == null || body.logradouro.trim().isEmpty()) {
                    result.error("CEP não encontrado. Confira o número ou preencha o endereço.");
                } else {
                    result.success(body);
                }
            }
            @Override public void onFailure(@NonNull Call<ViaCepAddress> call,
                                            @NonNull Throwable throwable) {
                result.error("Não foi possível consultar o CEP. Preencha o endereço manualmente.");
            }
        });
    }
}
