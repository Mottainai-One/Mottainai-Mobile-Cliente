package com.mottainai.cliente.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mottainai.cliente.network.dto.CreateCustomerRequest;
import com.mottainai.cliente.network.dto.ViaCepAddress;
import com.mottainai.cliente.repository.AuthRepository;

public final class AuthViewModel extends ViewModel {
    public static final class State {
        public enum Kind { IDLE, SELECTING_GOOGLE, LOADING, AUTHENTICATED, PROFILE_REQUIRED,
            PROFILE_SYNC_ERROR, REGISTERED, RESET_SENT, ERROR }
        public final Kind kind;
        public final String message;
        public final String displayName;
        public final String email;

        private State(Kind kind, String message) {
            this(kind, message, null, null);
        }

        private State(Kind kind, String message, String displayName, String email) {
            this.kind = kind;
            this.message = message;
            this.displayName = displayName;
            this.email = email;
        }
    }

    private final MutableLiveData<State> state = new MutableLiveData<>(new State(State.Kind.IDLE, null));
    private final MutableLiveData<ViaCepAddress> address = new MutableLiveData<>();
    private final MutableLiveData<String> cepError = new MutableLiveData<>();
    private int cepRequest;

    public LiveData<State> state() { return state; }
    public LiveData<ViaCepAddress> address() { return address; }
    public LiveData<String> cepError() { return cepError; }

    public void login(AuthRepository repository, String email, String password) {
        if (isBusy()) return;
        state.setValue(new State(State.Kind.LOADING, null));
        repository.login(email, password, loginResult(repository));
    }

    public void loginWithGoogle(AuthRepository repository, String idToken) {
        if (state.getValue().kind == State.Kind.LOADING) return;
        state.setValue(new State(State.Kind.LOADING, null));
        repository.loginWithGoogle(idToken, loginResult(repository));
    }

    public boolean isBusy() {
        return state.getValue().kind == State.Kind.LOADING
                || state.getValue().kind == State.Kind.SELECTING_GOOGLE;
    }

    public boolean beginGoogleSignIn() {
        if (isBusy()) return false;
        state.setValue(new State(State.Kind.SELECTING_GOOGLE, null));
        return true;
    }

    public void googleSignInFailed(String message) {
        if (state.getValue().kind == State.Kind.SELECTING_GOOGLE) {
            state.setValue(new State(State.Kind.ERROR, message));
        }
    }

    public void retryProfileSync(AuthRepository repository) {
        if (isBusy()) return;
        state.setValue(new State(State.Kind.LOADING, null));
        repository.retryProfileSync(loginResult(repository));
    }

    private AuthRepository.Result<AuthRepository.LoginResult> loginResult(AuthRepository repository) {
        return new AuthRepository.Result<AuthRepository.LoginResult>() {
            @Override public void success(AuthRepository.LoginResult result) {
                state.setValue(result.profileRequired
                        ? new State(State.Kind.PROFILE_REQUIRED, null, result.displayName, result.email)
                        : new State(State.Kind.AUTHENTICATED, null));
            }
            @Override public void error(String message) {
                state.setValue(new State(repository.pendingProfile() != null
                        ? State.Kind.PROFILE_SYNC_ERROR : State.Kind.ERROR, message));
            }
        };
    }

    public void resetPassword(AuthRepository repository, String email) {
        if (isBusy()) return;
        state.setValue(new State(State.Kind.LOADING, null));
        repository.resetPassword(email, new AuthRepository.Result<Void>() {
            @Override public void success(Void value) {
                state.setValue(new State(State.Kind.RESET_SENT,
                        "Se houver uma conta para esse e-mail, você receberá as instruções para redefinir a senha."));
            }
            @Override public void error(String message) {
                state.setValue(new State(State.Kind.ERROR, message));
            }
        });
    }

    public void reset() {
        state.setValue(new State(State.Kind.IDLE, null));
    }

    public void register(AuthRepository repository, CreateCustomerRequest request) {
        if (isBusy()) return;
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
        int request = ++cepRequest;
        cepError.setValue(null);
        repository.lookupCep(cep, new AuthRepository.Result<ViaCepAddress>() {
            @Override public void success(ViaCepAddress value) {
                if (request == cepRequest) address.setValue(value);
            }
            @Override public void error(String message) {
                if (request == cepRequest) cepError.setValue(message);
            }
        });
    }
}
