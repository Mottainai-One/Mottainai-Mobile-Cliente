package com.mottainai.cliente.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.cliente.network.dto.CustomerProfileResponse;
import com.mottainai.cliente.repository.AuthRepository;

public final class ProfileViewModel extends AndroidViewModel {
    public static final class State {
        public enum Kind { IDLE, LOADING, READY, ERROR }

        public final Kind kind;
        public final CustomerProfileResponse profile;
        public final String message;

        private State(Kind kind, CustomerProfileResponse profile, String message) {
            this.kind = kind;
            this.profile = profile;
            this.message = message;
        }
    }

    private final AuthRepository repository;
    private final MutableLiveData<State> state = new MutableLiveData<>(
            new State(State.Kind.IDLE, null, null));

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<State> state() {
        return state;
    }

    public void load() {
        State current = state.getValue();
        if (current != null && current.kind == State.Kind.LOADING) return;
        state.setValue(new State(State.Kind.LOADING, null, null));
        repository.loadProfile(new AuthRepository.Result<CustomerProfileResponse>() {
            @Override public void success(CustomerProfileResponse profile) {
                state.setValue(new State(State.Kind.READY, profile, null));
            }

            @Override public void error(String message) {
                state.setValue(new State(State.Kind.ERROR, null, message));
            }
        });
    }
}
