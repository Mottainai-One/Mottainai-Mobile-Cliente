package com.mottainai.cliente.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.cliente.network.dto.CustomerDetailsResponse;
import com.mottainai.cliente.repository.AuthRepository;
import com.mottainai.cliente.repository.ProfileRepository;

public final class ProfileViewModel extends AndroidViewModel {
    public static final class State {
        public enum Kind { IDLE, LOADING, READY, SAVING, ERROR }

        public final Kind kind;
        public final CustomerDetailsResponse profile;
        public final String message;

        private State(Kind kind, CustomerDetailsResponse profile, String message) {
            this.kind = kind;
            this.profile = profile;
            this.message = message;
        }
    }

    private final ProfileRepository repository;
    private final MutableLiveData<State> state = new MutableLiveData<>(
            new State(State.Kind.IDLE, null, null));

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new ProfileRepository(application);
    }

    public LiveData<State> state() {
        return state;
    }

    public CustomerDetailsResponse currentProfile() {
        State current = state.getValue();
        return current == null ? null : current.profile;
    }

    public void load() {
        State current = state.getValue();
        if (current != null && (current.kind == State.Kind.LOADING
                || current.kind == State.Kind.SAVING)) return;
        state.setValue(new State(State.Kind.LOADING, null, null));
        repository.load(new AuthRepository.Result<CustomerDetailsResponse>() {
            @Override public void success(CustomerDetailsResponse profile) {
                state.setValue(new State(State.Kind.READY, profile, null));
            }

            @Override public void error(String message) {
                state.setValue(new State(State.Kind.ERROR, null, message));
            }
        });
    }

    public void update(String fullName, String phone, boolean marketingConsent) {
        CustomerDetailsResponse profile = currentProfile();
        if (profile == null) return;
        state.setValue(new State(State.Kind.SAVING, profile, null));
        repository.update(profile, fullName, phone, marketingConsent,
                new AuthRepository.Result<CustomerDetailsResponse>() {
                    @Override public void success(CustomerDetailsResponse updated) {
                        state.setValue(new State(State.Kind.READY, updated, null));
                    }

                    @Override public void error(String message) {
                        state.setValue(new State(State.Kind.ERROR, profile, message));
                    }
                });
    }
}
