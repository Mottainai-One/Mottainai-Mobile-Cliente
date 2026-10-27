package com.mottainai.cliente.activities;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.mottainai.cliente.BuildConfig;
import com.mottainai.cliente.MainActivity;
import com.mottainai.cliente.R;
import com.mottainai.cliente.repository.AuthRepository;
import com.mottainai.cliente.utils.GoogleSignInHelper;
import com.mottainai.cliente.utils.SessionManager;
import com.mottainai.cliente.viewmodel.AuthViewModel;

/** Protected-area entry point. */
public class LoginClienteActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> registration = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (new SessionManager(this).isLoggedIn()) {
                        openMain();
                        return;
                    }
                    TextView status = findViewById(R.id.text_login_status);
                    status.setText("Cadastro concluído. Entre com seu e-mail e senha.");
                    status.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        if (session.canBrowse()) {
            openMain();
            return;
        }

        setContentView(R.layout.activity_login_cliente);
        EditText email = findViewById(R.id.input_login_email);
        EditText password = findViewById(R.id.input_login_password);
        Button submit = findViewById(R.id.btn_login_submit);
        Button google = findViewById(R.id.btn_login_google);
        Button retrySync = findViewById(R.id.btn_retry_profile_sync);
        View registrationButton = findViewById(R.id.btn_open_registration);
        View resetPassword = findViewById(R.id.btn_reset_password);
        TextView status = findViewById(R.id.text_login_status);
        AuthViewModel model = new ViewModelProvider(this).get(AuthViewModel.class);
        AuthRepository repository = new AuthRepository(this);

        model.state().observe(this, state -> {
            boolean loading = model.isBusy();
            email.setEnabled(!loading);
            password.setEnabled(!loading);
            submit.setEnabled(!loading);
            google.setEnabled(!loading);
            registrationButton.setEnabled(!loading);
            resetPassword.setEnabled(!loading);
            findViewById(R.id.btn_explore_demo).setEnabled(!loading);
            AuthRepository.LoginResult pending = repository.pendingProfile();
            boolean canRetry = pending != null && (state.kind == AuthViewModel.State.Kind.PROFILE_SYNC_ERROR
                    || state.kind == AuthViewModel.State.Kind.IDLE);
            retrySync.setVisibility(canRetry ? View.VISIBLE : View.GONE);
            status.setVisibility(loading || canRetry || state.kind == AuthViewModel.State.Kind.ERROR
                    || state.kind == AuthViewModel.State.Kind.RESET_SENT
                    ? View.VISIBLE : View.GONE);
            status.setText(loading ? state.message == null ? "Aguarde…" : state.message : state.message != null ? state.message
                    : canRetry ? "Sua conta está autenticada. Tente concluir o acesso novamente." : "");
            if (state.kind == AuthViewModel.State.Kind.AUTHENTICATED) openMain();
            if (state.kind == AuthViewModel.State.Kind.PROFILE_REQUIRED) {
                Intent intent = new Intent(this, RegisterClienteActivity.class);
                intent.putExtra(RegisterClienteActivity.EXTRA_PROFILE_REQUIRED, true);
                intent.putExtra(RegisterClienteActivity.EXTRA_PROFILE_EMAIL, state.email);
                model.reset();
                registration.launch(intent);
            }
        });
        submit.setOnClickListener(v -> {
            String emailValue = email.getText().toString().trim();
            String passwordValue = password.getText().toString();
            if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                email.setError("Informe um e-mail válido.");
                return;
            }
            if (passwordValue.isEmpty()) {
                password.setError("Informe sua senha.");
                return;
            }
            model.login(repository, emailValue, passwordValue);
        });
        retrySync.setOnClickListener(v -> model.retryProfileSync(repository));
        resetPassword.setOnClickListener(v -> {
            String emailValue = email.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                email.setError("Informe seu e-mail para receber as instruções.");
                return;
            }
            model.resetPassword(repository, emailValue);
        });
        registrationButton.setOnClickListener(v ->
                registration.launch(new Intent(this, RegisterClienteActivity.class)));
        google.setOnClickListener(v -> {
            if (!model.beginGoogleSignIn()) return;
            GoogleSignInHelper.signIn(this, new GoogleSignInHelper.Callback() {
                @Override public void onToken(String idToken) {
                    model.loginWithGoogle(repository, idToken);
                }

                @Override public void onError(String message) {
                    model.googleSignInFailed(message);
                }
            });
        });

        Button demo = findViewById(R.id.btn_explore_demo);
        if (BuildConfig.DEBUG) {
            demo.setVisibility(View.VISIBLE);
            demo.setOnClickListener(v -> {
                session.enterDemoMode();
                openMain();
            });
        }
    }

    private void openMain() {
        if (isFinishing()) return;
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
