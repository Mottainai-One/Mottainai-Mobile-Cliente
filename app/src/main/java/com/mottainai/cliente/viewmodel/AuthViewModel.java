package com.mottainai.cliente.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mottainai.cliente.network.dto.CreateCustomerRequest;
import com.mottainai.cliente.network.dto.ViaCepAddress;
import com.mottainai.cliente.repository.AuthRepository;

public final class AuthViewModel extends ViewModel {
    public static final class State {
        public enum Kind { IDLE, LOADING, AUTHENTICATED, REGISTERED, ERROR }
        public final Kind kind;
        public final String message;

        private State(Kind kind, String message) {
            this.kind = kind;
            this.message = message;
        }
    }

    private final MutableLiveData<State> state = new MutableLiveData<>(new State(State.Kind.IDLE, null));
    private final MutableLiveData<ViaCepAddress> address = new MutableLiveData<>();
    private final MutableLiveData<String> cepError = new MutableLiveData<>();

    public LiveData<State> state() { return state; }
    public LiveData<ViaCepAddress> address() { return address; }
    public LiveData<String> cepError() { return cepError; }

    public void login(AuthRepository repository, String email, String password) {
        state.setValue(new State(State.Kind.LOADING, null));
        repository.login(email, password, new AuthRepository.Result<Void>() {
            @Override public void success(Void value) {
                state.setValue(new State(State.Kind.AUTHENTICATED, null));
            }
            @Override public void error(String message) {
                state.setValue(new State(State.Kind.ERROR, message));
            }
        });
    }

    public void register(AuthRepository repository, CreateCustomerRequest request) {
        state.setValue(new State(State.Kind.LOADING, null));
        repository.register(request, new AuthRepository.Result<Void>() {
            @Override public void success(Void value) {
                state.setValue(new State(State.Kind.REGISTERED, null));
            }
            @Override public void error(String message) {
                state.setValue(new State(State.Kind.ERROR, message));
            }
        });
    }

    public void lookupCep(AuthRepository repository, String cep) {
        cepError.setValue(null);
        repository.lookupCep(cep, new AuthRepository.Result<ViaCepAddress>() {
            @Override public void success(ViaCepAddress value) { address.setValue(value); }
            @Override public void error(String message) { cepError.setValue(message); }
        });
    }
}
