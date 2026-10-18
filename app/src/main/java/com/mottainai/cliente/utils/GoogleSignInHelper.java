package com.mottainai.cliente.utils;

import android.app.Activity;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.core.content.ContextCompat;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

public final class GoogleSignInHelper {
    public interface Callback {
        void onToken(String idToken);
        void onError(String message);
    }

    private GoogleSignInHelper() { }

    public static void signIn(Activity activity, Callback callback) {
        int clientIdResource = activity.getResources().getIdentifier(
                "default_web_client_id", "string", activity.getPackageName());
        if (clientIdResource == 0) {
            callback.onError("Atualize o google-services.json depois de habilitar o Google no Firebase.");
            return;
        }

        try {
            GetGoogleIdOption googleOption = new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(activity.getString(clientIdResource))
                    .build();
            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build();

            CredentialManager.create(activity).getCredentialAsync(
                    activity,
                    request,
                    new CancellationSignal(),
                    ContextCompat.getMainExecutor(activity),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override public void onResult(@NonNull GetCredentialResponse response) {
                            Credential credential = response.getCredential();
                            if (!(credential instanceof CustomCredential)
                                    || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                    .equals(credential.getType())) {
                                callback.onError("O Google não retornou uma credencial compatível. Tente novamente.");
                                return;
                            }
                            try {
                                CustomCredential customCredential = (CustomCredential) credential;
                                GoogleIdTokenCredential googleCredential =
                                        GoogleIdTokenCredential.createFrom(customCredential.getData());
                                callback.onToken(googleCredential.getIdToken());
                            } catch (Exception exception) {
                                callback.onError("Não foi possível validar a conta Google. Tente novamente.");
                            }
                        }

                        @Override public void onError(@NonNull GetCredentialException exception) {
                            callback.onError("Google Sign-In cancelado ou indisponível. Tente novamente.");
                        }
                    });
        } catch (Exception exception) {
            callback.onError("Não foi possível iniciar o Google Sign-In. Tente novamente.");
        }
    }
}
