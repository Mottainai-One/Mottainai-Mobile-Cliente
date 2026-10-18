package com.mottainai.cliente.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.mottainai.cliente.R;
import com.mottainai.cliente.network.dto.CreateAddressRequest;
import com.mottainai.cliente.network.dto.CreateCustomerRequest;
import com.mottainai.cliente.repository.AuthRepository;
import com.mottainai.cliente.utils.GoogleSignInHelper;
import com.mottainai.cliente.viewmodel.AuthViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class RegisterClienteActivity extends AppCompatActivity {
    public static final String EXTRA_GOOGLE_PROFILE = "google_profile_required";
    public static final String EXTRA_GOOGLE_NAME = "google_profile_name";
    public static final String EXTRA_GOOGLE_EMAIL = "google_profile_email";

    private EditText name, cpf, email, password, phone, birthDate;
    private EditText cep, street, number, complement, neighborhood, city, state;
    private CheckBox marketing;
    private TextView status, stepLabel;
    private ViewFlipper pages;
    private Button googleButton, nextButton, backButton, submitButton;
    private boolean googleProfile;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_cliente);

        pages = findViewById(R.id.register_pages);
        stepLabel = findViewById(R.id.text_register_step);
        name = findViewById(R.id.input_register_name);
        cpf = findViewById(R.id.input_register_cpf);
        email = findViewById(R.id.input_register_email);
        password = findViewById(R.id.input_register_password);
        phone = findViewById(R.id.input_register_phone);
        birthDate = findViewById(R.id.input_register_birth_date);
        cep = findViewById(R.id.input_register_cep);
        street = findViewById(R.id.input_register_street);
        number = findViewById(R.id.input_register_number);
        complement = findViewById(R.id.input_register_complement);
        neighborhood = findViewById(R.id.input_register_neighborhood);
        city = findViewById(R.id.input_register_city);
        state = findViewById(R.id.input_register_state);
        marketing = findViewById(R.id.check_register_marketing);
        status = findViewById(R.id.text_register_status);
        googleButton = findViewById(R.id.btn_register_google);
        nextButton = findViewById(R.id.btn_register_next);
        backButton = findViewById(R.id.btn_register_back);
        submitButton = findViewById(R.id.btn_register_submit);

        AuthRepository repository = new AuthRepository(this);
        AuthViewModel model = new ViewModelProvider(this).get(AuthViewModel.class);

        if (getIntent().getBooleanExtra(EXTRA_GOOGLE_PROFILE, false)) {
            enableGoogleProfile(getIntent().getStringExtra(EXTRA_GOOGLE_NAME),
                    getIntent().getStringExtra(EXTRA_GOOGLE_EMAIL));
        }

        model.address().observe(this, address -> {
            if (address == null || !digits(cep).equals(digits(address.cep))) return;
            street.setText(address.logradouro);
            neighborhood.setText(address.bairro);
            city.setText(address.localidade);
            state.setText(address.uf);
            hideStatus();
        });
        model.cepError().observe(this, message -> {
            if (message != null) showStatus(message);
        });
        model.state().observe(this, current -> {
            boolean loading = current.kind == AuthViewModel.State.Kind.LOADING;
            googleButton.setEnabled(!loading);
            nextButton.setEnabled(!loading);
            backButton.setEnabled(!loading);
            submitButton.setEnabled(!loading);
            if (loading) showStatus("Aguarde…");
            if (current.kind == AuthViewModel.State.Kind.ERROR) showStatus(current.message);
            if (current.kind == AuthViewModel.State.Kind.PROFILE_REQUIRED) {
                enableGoogleProfile(current.displayName, current.email);
                hideStatus();
            }
            if (current.kind == AuthViewModel.State.Kind.AUTHENTICATED
                    || current.kind == AuthViewModel.State.Kind.REGISTERED) {
                setResult(RESULT_OK);
                finish();
            }
        });

        googleButton.setOnClickListener(v -> {
            googleButton.setEnabled(false);
            showStatus("Conectando ao Google…");
            GoogleSignInHelper.signIn(this, new GoogleSignInHelper.Callback() {
                @Override public void onToken(String idToken) {
                    model.loginWithGoogle(repository, idToken);
                }

                @Override public void onError(String message) {
                    googleButton.setEnabled(true);
                    showStatus(message);
                }
            });
        });
        nextButton.setOnClickListener(v -> {
            if (!validatePersonalDetails()) return;
            pages.showNext();
            stepLabel.setText("Etapa 2 de 2 · Endereço");
            hideStatus();
        });
        backButton.setOnClickListener(v -> {
            pages.showPrevious();
            stepLabel.setText("Etapa 1 de 2 · Seus dados");
            hideStatus();
        });
        findViewById(R.id.btn_register_find_cep).setOnClickListener(v -> {
            String zip = digits(cep);
            if (zip.length() != 8) {
                cep.setError("Informe os 8 dígitos do CEP.");
                return;
            }
            showStatus("Consultando CEP…");
            model.lookupCep(repository, zip);
        });
        submitButton.setOnClickListener(v -> {
            CreateCustomerRequest request = validateAndBuild();
            if (request != null) model.register(repository, request);
        });
    }

    private boolean validatePersonalDetails() {
        String fullName = value(name);
        String cpfDigits = digits(cpf);
        String emailValue = value(email);
        String phoneValue = value(phone);
        String birthValue = value(birthDate);

        if (fullName.isEmpty() || fullName.length() > 150) return invalid(name, "Informe seu nome (até 150 caracteres).");
        if (cpfDigits.length() != 11) return invalid(cpf, "Informe os 11 dígitos do CPF.");
        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() || emailValue.length() > 150)
            return invalid(email, "Informe um e-mail válido.");
        if (!googleProfile) {
            String passwordValue = password.getText().toString();
            if (passwordValue.length() < 8 || passwordValue.length() > 100)
                return invalid(password, "Use uma senha de 8 a 100 caracteres.");
        }
        if (phoneValue.isEmpty() || phoneValue.length() > 20) return invalid(phone, "Informe seu telefone.");
        if (!birthValue.isEmpty()) {
            try {
                if (LocalDate.parse(birthValue).isAfter(LocalDate.now()))
                    return invalid(birthDate, "A data de nascimento não pode ser futura.");
            } catch (DateTimeParseException exception) {
                return invalid(birthDate, "Use o formato AAAA-MM-DD.");
            }
        }
        return true;
    }

    private CreateCustomerRequest validateAndBuild() {
        if (!validatePersonalDetails()) {
            pages.setDisplayedChild(0);
            stepLabel.setText("Etapa 1 de 2 · Seus dados");
            return null;
        }

        String zip = digits(cep);
        String streetValue = value(street);
        String numberValue = value(number);
        String neighborhoodValue = value(neighborhood);
        String cityValue = value(city);
        String stateValue = value(state).toUpperCase(Locale.ROOT);

        if (zip.length() != 8) return invalidRequest(cep, "Informe os 8 dígitos do CEP.");
        if (streetValue.isEmpty() || streetValue.length() > 150) return invalidRequest(street, "Informe a rua.");
        if (numberValue.isEmpty() || numberValue.length() > 10) return invalidRequest(number, "Informe o número.");
        if (value(complement).length() > 100) return invalidRequest(complement, "Complemento muito longo.");
        if (neighborhoodValue.isEmpty() || neighborhoodValue.length() > 100)
            return invalidRequest(neighborhood, "Informe o bairro.");
        if (cityValue.isEmpty() || cityValue.length() > 100) return invalidRequest(city, "Informe a cidade.");
        if (!stateValue.matches("[A-Z]{2}")) return invalidRequest(state, "Use a sigla do estado (UF).");

        String passwordValue = googleProfile ? "" : password.getText().toString();
        String birthValue = value(birthDate);
        return new CreateCustomerRequest(value(name), digits(cpf), value(email), passwordValue,
                value(phone), birthValue.isEmpty() ? null : birthValue, marketing.isChecked(),
                new CreateAddressRequest(zip, streetValue, numberValue, value(complement),
                        neighborhoodValue, cityValue, stateValue));
    }

    private void enableGoogleProfile(String displayName, String googleEmail) {
        googleProfile = true;
        if (value(name).isEmpty() && displayName != null) name.setText(displayName);
        if (googleEmail != null) email.setText(googleEmail);
        email.setEnabled(false);
        password.setVisibility(View.GONE);
        googleButton.setVisibility(View.GONE);
    }

    private boolean invalid(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
        return false;
    }

    private CreateCustomerRequest invalidRequest(EditText field, String message) {
        invalid(field, message);
        return null;
    }

    private void showStatus(String message) {
        status.setText(message);
        status.setVisibility(View.VISIBLE);
    }

    private void hideStatus() {
        status.setVisibility(View.GONE);
    }

    private static String value(EditText field) { return field.getText().toString().trim(); }
    private static String digits(EditText field) { return digits(value(field)); }
    private static String digits(String value) { return value == null ? "" : value.replaceAll("\\D", ""); }
}
