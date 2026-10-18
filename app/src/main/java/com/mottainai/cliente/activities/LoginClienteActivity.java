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
        TextView status = findViewById(R.id.text_login_status);
        AuthViewModel model = new ViewModelProvider(this).get(AuthViewModel.class);
        AuthRepository repository = new AuthRepository(this);

        model.state().observe(this, state -> {
            boolean loading = state.kind == AuthViewModel.State.Kind.LOADING;
            submit.setEnabled(!loading);
            google.setEnabled(!loading);
            status.setVisibility(loading || state.kind == AuthViewModel.State.Kind.ERROR
                    ? View.VISIBLE : View.GONE);
            status.setText(loading ? "Entrando…" : state.message);
            if (state.kind == AuthViewModel.State.Kind.AUTHENTICATED) openMain();
            if (state.kind == AuthViewModel.State.Kind.PROFILE_REQUIRED) {
                Intent intent = new Intent(this, RegisterClienteActivity.class);
                intent.putExtra(RegisterClienteActivity.EXTRA_GOOGLE_PROFILE, true);
                intent.putExtra(RegisterClienteActivity.EXTRA_GOOGLE_NAME, state.displayName);
                intent.putExtra(RegisterClienteActivity.EXTRA_GOOGLE_EMAIL, state.email);
                registration.launch(intent);
                model.reset();
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
        findViewById(R.id.btn_open_registration).setOnClickListener(v ->
                registration.launch(new Intent(this, RegisterClienteActivity.class)));
        google.setOnClickListener(v -> {
            google.setEnabled(false);
            status.setText("Conectando ao Google…");
            status.setVisibility(View.VISIBLE);
            GoogleSignInHelper.signIn(this, new GoogleSignInHelper.Callback() {
                @Override public void onToken(String idToken) {
                    model.loginWithGoogle(repository, idToken);
                }

                @Override public void onError(String message) {
                    google.setEnabled(true);
                    status.setText(message);
                    status.setVisibility(View.VISIBLE);
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
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
