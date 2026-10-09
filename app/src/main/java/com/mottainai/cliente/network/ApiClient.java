package com.mottainai.cliente.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mottainai.cliente.activities.LoginClienteActivity;
import com.mottainai.cliente.utils.SessionManager;

import java.io.IOException;

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
                .addInterceptor(new JwtInterceptor(context.getApplicationContext(), sessionManager))
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

    private static final class JwtInterceptor implements Interceptor {

        private final Context context;
        private final SessionManager sessionManager;

        private JwtInterceptor(Context context, SessionManager sessionManager) {
            this.context = context;
            this.sessionManager = sessionManager;
        }

        @Override
        @NonNull
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            String token = sessionManager.isLoggedIn() ? sessionManager.getAuthToken() : null;
            Request authenticatedRequest = request;
            if (request.header("Authorization") == null && token != null && !token.trim().isEmpty()) {
                authenticatedRequest = request.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
            }
            Response response = chain.proceed(authenticatedRequest);
            if (response.code() == 401 && token != null
                    && ("Bearer " + token).equals(authenticatedRequest.header("Authorization"))
                    && sessionManager.clearSessionIfTokenMatches(token)) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (sessionManager.isLoggedIn()) return;
                    Intent login = new Intent(context, LoginClienteActivity.class);
                    login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(login);
                });
            }
            return response;
        }
    }
}
