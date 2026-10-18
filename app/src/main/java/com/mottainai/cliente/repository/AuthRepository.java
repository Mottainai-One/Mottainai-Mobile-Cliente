package com.mottainai.cliente.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.mottainai.cliente.BuildConfig;
import com.mottainai.cliente.network.ApiClient;
import com.mottainai.cliente.network.ApiService;
import com.mottainai.cliente.network.dto.CreateCustomerRequest;
import com.mottainai.cliente.network.dto.CreateFirebaseCustomerProfileRequest;
import com.mottainai.cliente.network.dto.CustomerProfileResponse;
import com.mottainai.cliente.network.dto.UpdateFirebaseCustomerProfileRequest;
import com.mottainai.cliente.network.dto.ViaCepAddress;
import com.mottainai.cliente.utils.FirebaseSupport;
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

    public static final class LoginResult {
        public final boolean profileRequired;
        public final String displayName;
        public final String email;

        private LoginResult(boolean profileRequired, String displayName, String email) {
            this.profileRequired = profileRequired;
            this.displayName = displayName;
            this.email = email;
        }
    }

    private interface ViaCepService {
        @GET("ws/{cep}/json/")
        Call<ViaCepAddress> find(@Path("cep") String cep);
    }

    private final ApiService api;
    private final ViaCepService viaCep;
    private final SessionManager session;
    private final FirebaseAuth firebaseAuth;

    public AuthRepository(Context context) {
        session = new SessionManager(context);
        api = BuildConfig.API_BASE_URL.trim().isEmpty()
                ? null : ApiClient.create(context, BuildConfig.API_BASE_URL);
        firebaseAuth = FirebaseSupport.authIfConfigured(context);
        viaCep = new Retrofit.Builder()
                .baseUrl("https://viacep.com.br/")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ViaCepService.class);
    }

    public void login(String email, String password, Result<LoginResult> result) {
        if (!ready(result)) return;
        session.clearSession();
        firebaseAuth.signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                result.error(firebaseError(task.getException(), "Não foi possível entrar."));
                return;
            }
            loadCurrentProfile(result);
        });
    }

    public void loginWithGoogle(String idToken, Result<LoginResult> result) {
        if (!ready(result)) return;
        session.clearSession();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                result.error(firebaseError(task.getException(), "Não foi possível entrar com Google."));
                return;
            }

            loadCurrentProfile(result);
        });
    }

    @Nullable
    public LoginResult pendingProfile() {
        FirebaseUser user = firebaseAuth == null ? null : firebaseAuth.getCurrentUser();
        return user != null && !session.isLoggedIn()
                ? new LoginResult(true, user.getDisplayName(), user.getEmail()) : null;
    }

    public void retryProfileSync(Result<LoginResult> result) {
        if (ready(result)) loadCurrentProfile(result);
    }

    public void resetPassword(String email, Result<Void> result) {
        if (firebaseAuth == null) {
            result.error("O acesso à sua conta está indisponível no momento.");
            return;
        }
        firebaseAuth.sendPasswordResetEmail(email.trim()).addOnCompleteListener(task -> {
            if (task.isSuccessful() || (task.getException() instanceof FirebaseAuthException
                    && "ERROR_USER_NOT_FOUND".equals(
                    ((FirebaseAuthException) task.getException()).getErrorCode()))) {
                result.success(null);
            } else {
                result.error(firebaseError(task.getException(), "Não foi possível enviar as instruções. Tente novamente."));
            }
        });
    }

    public void register(CreateCustomerRequest request, Result<Void> result) {
        if (!ready(result)) return;
        FirebaseUser current = firebaseAuth.getCurrentUser();
        if (current != null && request.email.equalsIgnoreCase(current.getEmail())) {
            createSqlProfile(request, result);
            return;
        }
        session.clearClientSession();
        if (current != null) firebaseAuth.signOut();
        firebaseAuth.createUserWithEmailAndPassword(request.email.trim(), request.password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        result.error(firebaseError(task.getException(), "Não foi possível criar sua conta."));
                        return;
                    }
                    createSqlProfile(request, result);
                });
    }

    public void loadProfile(Result<CustomerProfileResponse> result) {
        if (session.isDemoMode()) {
            result.success(DemoData.customerProfile());
            return;
        }
        if (api == null) {
            result.error("Configure MOTTAINAI_API_BASE_URL para consultar seu perfil.");
            return;
        }
        if (firebaseAuth == null || firebaseAuth.getCurrentUser() == null || !session.isLoggedIn()) {
            result.error("Sua sessão terminou. Entre novamente.");
            return;
        }
        String uid = firebaseAuth.getCurrentUser().getUid();
        api.profile(uid).enqueue(new Callback<CustomerProfileResponse>() {
            @Override public void onResponse(@NonNull Call<CustomerProfileResponse> call,
                                             @NonNull Response<CustomerProfileResponse> response) {
                CustomerProfileResponse body = response.body();
                if (!isCurrentUser(uid)) {
                    result.error("Sua conta mudou. Entre novamente.");
                    return;
                }
                if (response.isSuccessful() && body != null && body.id != null) {
                    populateDisplayIdentity(body);
                    result.success(body);
                } else {
                    result.error(response.code() == 404
                            ? "Seu perfil ainda não foi sincronizado. Conclua o cadastro novamente."
                            : "Não foi possível carregar seu perfil (HTTP " + response.code() + ").");
                }
            }
            @Override public void onFailure(@NonNull Call<CustomerProfileResponse> call,
                                            @NonNull Throwable throwable) {
                result.error("Falha de rede ao carregar seu perfil.");
            }
        });
    }

    public void updateProfile(UpdateFirebaseCustomerProfileRequest request,
                              Result<CustomerProfileResponse> result) {
        if (session.isDemoMode()) {
            result.error("A edição de perfil fica indisponível na demonstração.");
            return;
        }
        if (api == null) {
            result.error("Configure MOTTAINAI_API_BASE_URL para salvar seu perfil.");
            return;
        }
        if (firebaseAuth == null || firebaseAuth.getCurrentUser() == null || !session.isLoggedIn()) {
            result.error("Sua sessão terminou. Entre novamente.");
            return;
        }
        String uid = firebaseAuth.getCurrentUser().getUid();
        api.updateProfile(request, uid).enqueue(new Callback<CustomerProfileResponse>() {
            @Override public void onResponse(@NonNull Call<CustomerProfileResponse> call,
                                             @NonNull Response<CustomerProfileResponse> response) {
                if (!isCurrentUser(uid)) {
                    result.error("Sua conta mudou. Entre novamente.");
                } else if (response.isSuccessful() && response.body() != null) {
                    saveDisplayName(uid, request.fullName, new Result<Void>() {
                        @Override public void success(Void ignored) {
                            populateDisplayIdentity(response.body());
                            result.success(response.body());
                        }
                        @Override public void error(String message) {
                            result.error("Dados salvos no sistema, mas o nome de exibição não foi atualizado. " + message);
                        }
                    });
                } else {
                    result.error(response.code() == 404
                            ? "Perfil não encontrado. Entre novamente para sincronizar sua conta."
                            : "Não foi possível salvar seu perfil (HTTP " + response.code() + ").");
                }
            }
            @Override public void onFailure(@NonNull Call<CustomerProfileResponse> call,
                                            @NonNull Throwable throwable) {
                result.error("Falha de rede ao salvar seu perfil.");
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

    private boolean ready(Result<?> result) {
        if (firebaseAuth == null) {
            result.error("Configure o Firebase Authentication e adicione google-services.json ao app.");
            return false;
        }
        if (api == null) {
            result.error("Configure MOTTAINAI_API_BASE_URL para conectar o aplicativo à API.");
            return false;
        }
        return true;
    }

    private void loadCurrentProfile(Result<LoginResult> result) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            result.error("Sua sessão terminou. Entre novamente.");
            return;
        }
        String uid = user.getUid();
        api.profile(uid).enqueue(new Callback<CustomerProfileResponse>() {
            @Override public void onResponse(@NonNull Call<CustomerProfileResponse> call,
                                             @NonNull Response<CustomerProfileResponse> response) {
                CustomerProfileResponse profile = response.body();
                if (!isCurrentUser(uid)) {
                    result.error("A conta mudou durante o acesso. Entre novamente.");
                    return;
                }
                ProfileSyncStatus status = ProfileSyncStatus.from(response.code(), profile == null ? null : profile.id);
                if (status == ProfileSyncStatus.READY) {
                    session.saveClientSession(String.valueOf(profile.id), uid);
                    result.success(new LoginResult(false, null, null));
                } else if (status == ProfileSyncStatus.PROFILE_REQUIRED) {
                    result.success(new LoginResult(true, user.getDisplayName(), user.getEmail()));
                } else if (status == ProfileSyncStatus.INVALID_RESPONSE) {
                    result.error("Seu perfil retornou incompleto. Tente sincronizar novamente.");
                } else {
                    result.error("Não foi possível sincronizar seu perfil (HTTP " + response.code() + "). Tente novamente.");
                }
            }
            @Override public void onFailure(@NonNull Call<CustomerProfileResponse> call,
                                            @NonNull Throwable throwable) {
                result.error("Falha de rede ao sincronizar seu perfil. Tente novamente.");
            }
        });
    }

    private void createSqlProfile(CreateCustomerRequest request, Result<Void> result) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || !request.email.trim().equalsIgnoreCase(user.getEmail())) {
            result.error("Sua conta mudou. Entre novamente para concluir o cadastro.");
            return;
        }
        String uid = user.getUid();
        CreateFirebaseCustomerProfileRequest payload = new CreateFirebaseCustomerProfileRequest(
                request.fullName, request.cpf, request.phone, request.birthDate,
                request.marketingConsent, request.address);
        api.createProfile(payload, uid).enqueue(new Callback<CustomerProfileResponse>() {
            @Override public void onResponse(@NonNull Call<CustomerProfileResponse> call,
                                             @NonNull Response<CustomerProfileResponse> response) {
                CustomerProfileResponse profile = response.body();
                if (!isCurrentUser(uid)) {
                    result.error("Sua conta mudou. Entre novamente para concluir o cadastro.");
                    return;
                }
                if (ProfileSyncStatus.from(response.code(), profile == null ? null : profile.id)
                        == ProfileSyncStatus.READY) {
                    saveDisplayName(uid, request.fullName, new Result<Void>() {
                        @Override public void success(Void ignored) {
                            session.saveClientSession(String.valueOf(profile.id), uid);
                            result.success(null);
                        }
                        @Override public void error(String message) {
                            result.error("Perfil criado no sistema. Tente novamente para concluir seu nome de exibição. " + message);
                        }
                    });
                } else if (response.code() == 409) {
                    result.error("CPF ou e-mail já vinculado a outro cadastro.");
                } else {
                    result.error("Conta Firebase criada, mas não foi possível salvar seu perfil no sistema (HTTP "
                            + response.code() + "). Corrija os dados ou tente novamente; não crie outra conta.");
                }
            }
            @Override public void onFailure(@NonNull Call<CustomerProfileResponse> call,
                                            @NonNull Throwable throwable) {
                result.error("Conta Firebase criada, mas não foi possível sincronizar seu perfil. Tente novamente sem criar outra conta.");
            }
        });
    }

    private boolean isCurrentUser(String uid) {
        FirebaseUser current = firebaseAuth.getCurrentUser();
        return current != null && current.getUid().equals(uid);
    }

    private void populateDisplayIdentity(CustomerProfileResponse profile) {
        FirebaseUser current = firebaseAuth.getCurrentUser();
        if (current == null) return;
        profile.fullName = current.getDisplayName();
        profile.email = current.getEmail();
    }

    private void saveDisplayName(String uid, String name, Result<Void> result) {
        if (!isCurrentUser(uid)) {
            result.error("Sua conta mudou. Entre novamente.");
            return;
        }
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (name.trim().equals(user.getDisplayName())) {
            result.success(null);
            return;
        }
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(name.trim()).build();
        user.updateProfile(request).addOnCompleteListener(task -> {
            if (!isCurrentUser(uid)) result.error("Sua conta mudou. Entre novamente.");
            else if (task.isSuccessful()) result.success(null);
            else result.error(firebaseError(task.getException(), "Não foi possível atualizar o nome. Tente novamente."));
        });
    }

    private static String firebaseError(Exception exception, String fallback) {
        if (exception instanceof FirebaseNetworkException) return "Sem conexão. Verifique sua internet.";
        if (!(exception instanceof FirebaseAuthException)) return fallback;
        String code = ((FirebaseAuthException) exception).getErrorCode();
        switch (code) {
            case "ERROR_INVALID_EMAIL": return "Informe um e-mail válido.";
            case "ERROR_INVALID_CREDENTIAL":
            case "ERROR_WRONG_PASSWORD":
            case "ERROR_USER_NOT_FOUND": return "E-mail ou senha incorretos.";
            case "ERROR_EMAIL_ALREADY_IN_USE": return "Este e-mail já possui conta. Entre ou conclua o cadastro existente.";
            case "ERROR_WEAK_PASSWORD": return "Use uma senha mais forte (mínimo de 8 caracteres).";
            case "ERROR_TOO_MANY_REQUESTS": return "Muitas tentativas. Aguarde um pouco e tente novamente.";
            case "ERROR_OPERATION_NOT_ALLOWED":
                return "O provedor de login utilizado ainda não foi habilitado no Firebase.";
            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                return "Este e-mail já usa outro método de acesso. Entre com o método original.";
            default: return fallback;
        }
    }
}
