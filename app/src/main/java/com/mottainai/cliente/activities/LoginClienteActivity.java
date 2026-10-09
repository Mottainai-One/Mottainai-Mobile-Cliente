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

import com.mottainai.cliente.MainActivity;
import com.mottainai.cliente.R;
import com.mottainai.cliente.repository.AuthRepository;
import com.mottainai.cliente.utils.SessionManager;
import com.mottainai.cliente.viewmodel.AuthViewModel;

/** Protected-area entry point. */
public class LoginClienteActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> registration = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    TextView status = findViewById(R.id.text_login_status);
                    status.setText("Cadastro concluído. Entre com seu e-mail e senha.");
                    status.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (new SessionManager(this).isLoggedIn()) {
            openMain();
            return;
        }

        setContentView(R.layout.activity_login_cliente);
        EditText email = findViewById(R.id.input_login_email);
        EditText password = findViewById(R.id.input_login_password);
        Button submit = findViewById(R.id.btn_login_submit);
        TextView status = findViewById(R.id.text_login_status);
        AuthViewModel model = new ViewModelProvider(this).get(AuthViewModel.class);
        AuthRepository repository = new AuthRepository(this);

        model.state().observe(this, state -> {
            boolean loading = state.kind == AuthViewModel.State.Kind.LOADING;
            submit.setEnabled(!loading);
            status.setVisibility(loading || state.kind == AuthViewModel.State.Kind.ERROR
                    ? View.VISIBLE : View.GONE);
            status.setText(loading ? "Entrando…" : state.message);
            if (state.kind == AuthViewModel.State.Kind.AUTHENTICATED) openMain();
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
    }

    private void openMain() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
