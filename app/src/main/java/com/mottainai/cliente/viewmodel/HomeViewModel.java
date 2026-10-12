package com.mottainai.cliente.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.cliente.network.dto.CustomerProfileResponse;
import com.mottainai.cliente.network.dto.LoyaltyAccount;
import com.mottainai.cliente.repository.AuthRepository;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.repository.LoyaltyRepository;

public class HomeViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final LoyaltyRepository loyaltyRepository;
    private final MutableLiveData<LoadState<CustomerProfileResponse>> profile = new MutableLiveData<>();
    private final MutableLiveData<LoadState<LoyaltyAccount>> points = new MutableLiveData<>();
    private int profileRequest;
    private int pointsRequest;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        loyaltyRepository = new LoyaltyRepository(application);
    }

    public LiveData<LoadState<CustomerProfileResponse>> profile() { return profile; }
    public LiveData<LoadState<LoyaltyAccount>> points() { return points; }

    public void loadIfNeeded(String clientId) {
        if (profile.getValue() == null) loadProfile();
        if (points.getValue() == null) loadPoints(clientId);
    }

    public void loadProfile() {
        int request = ++profileRequest;
        profile.setValue(LoadState.loading());
        authRepository.loadProfile(new AuthRepository.Result<CustomerProfileResponse>() {
            @Override
            public void success(CustomerProfileResponse value) {
                if (request == profileRequest) profile.setValue(LoadState.success(value));
            }

            @Override
            public void error(String message) {
                if (request == profileRequest) profile.setValue(LoadState.error(message, 0));
            }
        });
    }

    public void loadPoints(String clientId) {
        int request = ++pointsRequest;
        if (clientId == null || clientId.trim().isEmpty()) {
            points.setValue(LoadState.error("Entre na sua conta para ver seus pontos.", 0));
            return;
        }
        points.setValue(LoadState.loading());
        loyaltyRepository.account(clientId, state -> {
            if (request == pointsRequest) points.setValue(state);
        });
    }
}
