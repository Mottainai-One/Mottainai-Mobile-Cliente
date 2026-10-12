package com.mottainai.cliente.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mottainai.cliente.BuildConfig;
import com.mottainai.cliente.network.ApiClient;
import com.mottainai.cliente.network.ApiService;
import com.mottainai.cliente.network.dto.CreateAddressRequest;
import com.mottainai.cliente.network.dto.CustomerDetailsResponse;
import com.mottainai.cliente.network.dto.UpdateCustomerRequest;
import com.mottainai.cliente.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ProfileRepository {
    private final ApiService api;
    private final SessionManager session;

    public ProfileRepository(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        session = new SessionManager(appContext);
        api = BuildConfig.API_BASE_URL.trim().isEmpty()
                ? null : ApiClient.create(appContext, BuildConfig.API_BASE_URL);
    }

    public void load(AuthRepository.Result<CustomerDetailsResponse> result) {
        String customerId = session.getClientId();
        if (api == null) {
            result.error("Configure MOTTAINAI_API_BASE_URL para consultar seu perfil.");
        } else if (!session.isLoggedIn() || customerId == null) {
            result.error("Sua sessão terminou. Entre novamente.");
        } else {
            api.customer(customerId).enqueue(new Callback<CustomerDetailsResponse>() {
                @Override
                public void onResponse(@NonNull Call<CustomerDetailsResponse> call,
                                       @NonNull Response<CustomerDetailsResponse> response) {
                    CustomerDetailsResponse body = response.body();
                    if (response.isSuccessful() && body != null && body.id != null) {
                        result.success(body);
                    } else {
                        result.error(response.code() == 403
                                ? "Não foi possível acessar este perfil."
                                : "Não foi possível carregar seu perfil (HTTP " + response.code() + ").");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CustomerDetailsResponse> call,
                                      @NonNull Throwable throwable) {
                    result.error("Falha de rede ao carregar seu perfil.");
                }
            });
        }
    }

    public void update(CustomerDetailsResponse current, String fullName, String phone,
                       boolean marketingConsent,
                       AuthRepository.Result<CustomerDetailsResponse> result) {
        String customerId = session.getClientId();
        if (api == null) {
            result.error("Configure MOTTAINAI_API_BASE_URL para salvar seu perfil.");
            return;
        }
        if (fullName == null || fullName.trim().isEmpty() || fullName.trim().length() > 150) {
            result.error("Informe um nome válido.");
            return;
        }
        if (phone != null && phone.trim().length() > 20) {
            result.error("Confira o telefone informado.");
            return;
        }
        if (!session.isLoggedIn() || customerId == null || current == null || current.id == null
                || !customerId.equals(String.valueOf(current.id))) {
            result.error("Sua sessão terminou. Entre novamente.");
            return;
        }

        CreateAddressRequest address = current.address == null ? null
                : new CreateAddressRequest(current.address.zipCode, current.address.street,
                current.address.number, current.address.complement, current.address.neighborhood,
                current.address.city, current.address.state);
        UpdateCustomerRequest request = new UpdateCustomerRequest(fullName.trim(), current.email,
                phone == null || phone.trim().isEmpty() ? null : phone.trim(), current.birthDate,
                marketingConsent, address);
        api.updateCustomer(customerId, request).enqueue(new Callback<CustomerDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<CustomerDetailsResponse> call,
                                   @NonNull Response<CustomerDetailsResponse> response) {
                CustomerDetailsResponse body = response.body();
                if (response.isSuccessful() && body != null && body.id != null) {
                    result.success(body);
                } else if (response.code() == 409) {
                    result.error("Este e-mail já está associado a outra conta.");
                } else if (response.code() == 400) {
                    result.error("Confira os dados informados e tente novamente.");
                } else {
                    result.error("Não foi possível salvar seu perfil (HTTP " + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CustomerDetailsResponse> call,
                                  @NonNull Throwable throwable) {
                result.error("Falha de rede ao salvar seu perfil.");
            }
        });
    }
}
