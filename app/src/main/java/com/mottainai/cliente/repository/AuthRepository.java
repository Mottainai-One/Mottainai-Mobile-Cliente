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
        default void progress(String message) {}
    }

    public static final class LoginResult {
        public final boolean profileRequired;
        public final String email;

        private LoginResult(boolean profileRequired, String email) {
            this.profileRequired = profileRequired;
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
        result.progress("Autenticando sua conta no Firebase…");
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
        result.progress("Autenticando sua conta Google no Firebase…");
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
                // A Firebase identity without a local session does not prove a SQL profile is absent.
                ? new LoginResult(false, user.getEmail()) : null;
    }

    public void signOut() { session.clearSession(); }

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
            ensureSqlProfile(request, result, true);
            return;
        }
        session.clearClientSession();
        if (current != null) firebaseAuth.signOut();
        result.progress("Criando sua identidade no Firebase…");
        firebaseAuth.createUserWithEmailAndPassword(request.email.trim(), request.password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        result.error(firebaseError(task.getException(), "Não foi possível criar sua conta."));
                        return;
                    }
                    ensureSqlProfile(request, result, true);
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
                ProfileSyncStatus status = ProfileSyncStatus.from(response.code(), body);
                if (status == ProfileSyncStatus.READY) {
                    populateLoginEmail(body);
                    result.success(body);
                } else {
                    result.error(response.code() == 404
                            ? "Seu perfil ainda não foi sincronizado. Conclua o cadastro novamente."
                            : profileFailure(status, response.code()));
                }
            }
            @Override public void onFailure(@NonNull Call<CustomerProfileResponse> call,
                                            @NonNull Throwable throwable) {
                result.error(syncError(throwable));
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
                } else {
                    CustomerProfileResponse profile = response.body();
                    ProfileSyncStatus status = ProfileSyncStatus.from(response.code(), profile);
                    if (status == ProfileSyncStatus.READY) {
                        // The SQL update confirmed this input; retain it only in this in-memory response.
                        profile.fullName = request.fullName;
                        populateLoginEmail(profile);
                        result.success(profile);
                    } else {
                        result.error(response.code() == 404
                                ? "Perfil não encontrado. Entre novamente para sincronizar sua conta."
                                : profileFailure(status, response.code()));
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<CustomerProfileResponse> call,
                                            @NonNull Throwable throwable) {
                result.error(syncError(throwable));
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
        result.progress("Conta autenticada. Consultando seu perfil na API…");
        api.profile(uid).enqueue(new Callback<CustomerProfileResponse>() {
            @Override public void onResponse(@NonNull Call<CustomerProfileResponse> call,
                                             @NonNull Response<CustomerProfileResponse> response) {
                CustomerProfileResponse profile = response.body();
                if (!isCurrentUser(uid)) {
                    result.error("A conta mudou durante o acesso. Entre novamente.");
                    return;
                }
                ProfileSyncStatus status = ProfileSyncStatus.from(response.code(), profile);
                if (status == ProfileSyncStatus.READY) {
                    session.saveClientSession(String.valueOf(profile.id), uid);
                    result.success(new LoginResult(false, null));
                } else if (status == ProfileSyncStatus.PROFILE_REQUIRED) {
                    result.success(new LoginResult(true, user.getEmail()));
                } else {
                    result.error(profileFailure(status, response.code()));
                }
            }
            @Override public void onFailure(@NonNull Call<CustomerProfileResponse> call,
                                            @NonNull Throwable throwable) {
                result.error(syncError(throwable));
            }
        });
    }

    private void ensureSqlProfile(CreateCustomerRequest request, Result<Void> result, boolean createIfMissing) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || !request.email.trim().equalsIgnoreCase(user.getEmail())) {
            result.error("Sua conta mudou. Entre novamente para concluir o cadastro.");
            return;
        }
        String uid = user.getUid();
        result.progress("Conta autenticada. Verificando se o perfil já foi salvo…");
        api.profile(uid).enqueue(new Callback<CustomerProfileResponse>() {
            @Override public void onResponse(@NonNull Call<CustomerProfileResponse> call,
                                             @NonNull Response<CustomerProfileResponse> response) {
                if (!isCurrentUser(uid)) {
                    result.error("Sua conta mudou. Entre novamente.");
                    return;
                }
                ProfileSyncStatus status = ProfileSyncStatus.from(response.code(), response.body());
                if (status == ProfileSyncStatus.READY) {
                    session.saveClientSession(String.valueOf(response.body().id), uid);
                    result.success(null);
                } else if (status.allowsProfileCreation(createIfMissing)) {
                    createSqlProfile(request, result);
                } else if (status == ProfileSyncStatus.PROFILE_REQUIRED) {
                    result.error("A API informou conflito, mas não retornou um perfil desta conta. "
                            + "Confira os dados ou contate o suporte; não crie outra conta Firebase.");
                } else {
                    result.error(profileFailure(status, response.code()));
                }
            }
            @Override public void onFailure(@NonNull Call<CustomerProfileResponse> call,
                                            @NonNull Throwable throwable) {
                result.error(syncError(throwable));
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
        result.progress("Salvando seu cadastro na API…");
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
                ProfileSyncStatus status = ProfileSyncStatus.from(response.code(), profile);
                if (status == ProfileSyncStatus.READY) {
                    session.saveClientSession(String.valueOf(profile.id), uid);
                    result.success(null);
                } else if (status == ProfileSyncStatus.INACTIVE
                        || status == ProfileSyncStatus.INVALID_RESPONSE) {
                    result.error(profileFailure(status, response.code()));
                } else if (response.code() == 409) {
                    // A previous timed-out POST may already have committed. Read once, never loop POST.
                    ensureSqlProfile(request, result, false);
                } else {
                    result.error("Conta Firebase criada, mas não foi possível salvar seu perfil no sistema (HTTP "
                            + response.code() + "). Corrija os dados ou tente novamente; não crie outra conta.");
                }
            }
            @Override public void onFailure(@NonNull Call<CustomerProfileResponse> call,
                                            @NonNull Throwable throwable) {
                result.error("Não foi possível confirmar se o perfil foi salvo. " + syncError(throwable));
            }
        });
    }

    private boolean isCurrentUser(String uid) {
        FirebaseUser current = firebaseAuth.getCurrentUser();
        return current != null && current.getUid().equals(uid);
    }

    private void populateLoginEmail(CustomerProfileResponse profile) {
        FirebaseUser current = firebaseAuth.getCurrentUser();
        if (current == null) return;
        profile.email = current.getEmail();
    }

    private String profileFailure(ProfileSyncStatus status, int httpStatus) {
        if (status == ProfileSyncStatus.INACTIVE) {
            session.clearClientSession();
            return "Seu cadastro está inativo. Entre em contato com o suporte.";
        }
        if (status == ProfileSyncStatus.INVALID_RESPONSE) {
            return "A API retornou um perfil incompleto. Tente sincronizar novamente.";
        }
        if (httpStatus == 401) return "A API não aceitou a autenticação Firebase. Entre novamente ou confira a configuração do servidor.";
        if (httpStatus == 403) return "A API não autorizou o acesso ao perfil desta conta.";
        if (httpStatus >= 500) return "A API está indisponível. Sua conta Firebase não precisa ser recriada.";
        return "Não foi possível confirmar seu perfil (HTTP " + httpStatus + "). Tente novamente.";
    }

    private String syncError(Throwable failure) {
        String host = null;
        try { host = java.net.URI.create(BuildConfig.API_BASE_URL.trim()).getHost(); }
        catch (IllegalArgumentException ignored) {}
        return ProfileSyncError.message(failure, "10.0.2.2".equals(host));
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
