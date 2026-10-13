package com.mottainai.cliente.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mottainai.cliente.activities.LoginClienteActivity;
import com.mottainai.cliente.utils.FirebaseSupport;
import com.mottainai.cliente.utils.SessionManager;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Factory for authenticated REST clients. Base URLs are supplied by environment configuration, never hard-coded. */
public final class ApiClient {

    private ApiClient() {
    }

    @NonNull
    public static ApiService create(@NonNull Context context, @NonNull String baseUrl) {
        return create(context, baseUrl, ApiService.class);
    }

    @NonNull
    public static <T> T create(@NonNull Context context, @NonNull String baseUrl,
                               @NonNull Class<T> serviceClass) {
        if (baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("A base URL da API deve ser configurada pelo ambiente.");
        }

        SessionManager sessionManager = new SessionManager(context);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new FirebaseTokenInterceptor(context.getApplicationContext()))
                .authenticator(new FirebaseTokenAuthenticator(context.getApplicationContext(), sessionManager))
                .build();
        Gson gson = new GsonBuilder().create();

        return new Retrofit.Builder()
                .baseUrl(normalizeBaseUrl(baseUrl))
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(serviceClass);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl.trim();
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }

    private static final class FirebaseTokenInterceptor implements Interceptor {

        private final Context context;

        private FirebaseTokenInterceptor(Context context) {
            this.context = context;
        }

        @Override
        @NonNull
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            FirebaseAuth auth = FirebaseSupport.authIfConfigured(context);
            FirebaseUser user = auth == null ? null : auth.getCurrentUser();
            if (user == null || request.header("Authorization") != null) return chain.proceed(request);
            try {
                String token = Tasks.await(user.getIdToken(false), 15, TimeUnit.SECONDS).getToken();
                return chain.proceed(token == null || token.isEmpty() ? request
                        : request.newBuilder().header("Authorization", "Bearer " + token).build());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IOException("Firebase session token unavailable", exception);
            } catch (ExecutionException | TimeoutException exception) {
                throw new IOException("Firebase session token unavailable", exception);
            }
        }
    }

    private static final class FirebaseTokenAuthenticator implements Authenticator {

        private final Context context;
        private final SessionManager sessionManager;

        private FirebaseTokenAuthenticator(Context context, SessionManager sessionManager) {
            this.context = context;
            this.sessionManager = sessionManager;
        }

        @Override
        public Request authenticate(okhttp3.Route route, Response response) {
            if (responseCount(response) >= 2) {
                expireSession();
                return null;
            }
            FirebaseAuth auth = FirebaseSupport.authIfConfigured(context);
            FirebaseUser user = auth == null ? null : auth.getCurrentUser();
            if (user == null) {
                expireSession();
                return null;
            }
            try {
                String token = Tasks.await(user.getIdToken(true), 15, TimeUnit.SECONDS).getToken();
                if (token == null || token.isEmpty()) return null;
                return response.request().newBuilder().header("Authorization", "Bearer " + token).build();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return null;
            } catch (ExecutionException exception) {
                if (exception.getCause() instanceof FirebaseAuthInvalidUserException) expireSession();
                return null;
            } catch (TimeoutException exception) {
                return null;
            }
        }

        private void expireSession() {
            if (sessionManager.getClientId() == null) return;
            sessionManager.clearSession();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (sessionManager.isLoggedIn()) return;
                Intent login = new Intent(context, LoginClienteActivity.class);
                login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(login);
            });
        }

        private static int responseCount(Response response) {
            int count = 1;
            while ((response = response.priorResponse()) != null) count++;
            return count;
        }
    }
}
