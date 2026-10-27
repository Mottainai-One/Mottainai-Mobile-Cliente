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
import androidx.activity.OnBackPressedCallback;
import androidx.lifecycle.ViewModelProvider;

import com.mottainai.cliente.R;
import com.mottainai.cliente.network.dto.CreateAddressRequest;
import com.mottainai.cliente.network.dto.CreateCustomerRequest;
import com.mottainai.cliente.repository.AuthRepository;
import com.mottainai.cliente.utils.GoogleSignInHelper;
import com.mottainai.cliente.utils.CpfValidator;
import com.mottainai.cliente.utils.InputMasks;
import com.mottainai.cliente.utils.RegistrationFields;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mottainai.cliente.viewmodel.AuthViewModel;

import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class RegisterClienteActivity extends AppCompatActivity {
    public static final String EXTRA_PROFILE_REQUIRED = "profile_required";
    public static final String EXTRA_PROFILE_EMAIL = "profile_email";

    private EditText name, cpf, email, password, confirmPassword, phone, birthDate;
    private EditText cep, street, number, complement, neighborhood, city, state;
    private CheckBox marketing;
    private TextView status, stepLabel;
    private ViewFlipper pages;
    private Button googleButton, nextButton, backButton, submitButton;
    private boolean profileCompletion;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_cliente);

        pages = findViewById(R.id.register_pages);
        stepLabel = findViewById(R.id.text_register_step);
        name = findViewById(R.id.input_register_name);
        cpf = findViewById(R.id.input_register_cpf);
        email = findViewById(R.id.input_register_email);
        password = findViewById(R.id.input_register_password);
        confirmPassword = findViewById(R.id.input_register_password_confirm);
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
        InputMasks.apply(cpf, "###.###.###-##");
        InputMasks.apply(cep, "#####-###");
        InputMasks.apply(birthDate, "##/##/####");
        InputMasks.applyPhone(phone);

        AuthRepository repository = new AuthRepository(this);
        AuthViewModel model = new ViewModelProvider(this).get(AuthViewModel.class);
        findViewById(R.id.btn_register_change_account).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this).setTitle("Trocar conta?")
                        .setMessage("Os dados deste formulário serão descartados. Você poderá entrar "
                                + "com outra conta Google ou cadastrar outro e-mail.")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Trocar conta", (dialog, which) -> {
                            repository.signOut();
                            profileCompletion = false;
                            for (EditText field : new EditText[]{name, cpf, email, password, confirmPassword,
                                    phone, birthDate, cep, street, number, complement, neighborhood, city, state}) {
                                field.setText("");
                                field.setError(null);
                            }
                            marketing.setChecked(false);
                            password.setVisibility(View.VISIBLE);
                            confirmPassword.setVisibility(View.VISIBLE);
                            googleButton.setVisibility(View.VISIBLE);
                            findViewById(R.id.text_register_account).setVisibility(View.GONE);
                            findViewById(R.id.btn_register_change_account).setVisibility(View.GONE);
                            model.reset();
                            showStep(0);
                            hideStatus();
                        }).show());

        AuthRepository.LoginResult pending = repository.pendingProfile();
        if (pending != null) {
            enableProfileCompletion(pending.email);
        } else if (getIntent().getBooleanExtra(EXTRA_PROFILE_REQUIRED, false)) {
            enableProfileCompletion(getIntent().getStringExtra(EXTRA_PROFILE_EMAIL));
        }
        if (savedInstanceState != null) showStep(savedInstanceState.getInt("register_step", 0));
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (model.isBusy()) return;
                if (pages.getDisplayedChild() > 0) showStep(0);
                else finish();
            }
        });

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
            boolean loading = model.isBusy();
            googleButton.setEnabled(!loading);
            nextButton.setEnabled(!loading);
            backButton.setEnabled(!loading);
            submitButton.setEnabled(!loading);
            findViewById(R.id.btn_register_change_account).setEnabled(!loading);
            findViewById(R.id.btn_register_find_cep).setEnabled(!loading);
            for (EditText field : new EditText[]{name, cpf, phone, birthDate, cep, street,
                    number, complement, neighborhood, city, state}) field.setEnabled(!loading);
            email.setEnabled(!loading && !profileCompletion);
            password.setEnabled(!loading && !profileCompletion);
            confirmPassword.setEnabled(!loading && !profileCompletion);
            marketing.setEnabled(!loading);
            if (loading) showStatus(current.message == null ? "Aguarde…" : current.message);
            if (current.kind == AuthViewModel.State.Kind.ERROR
                    || current.kind == AuthViewModel.State.Kind.PROFILE_SYNC_ERROR) {
                showStatus(current.message);
                AuthRepository.LoginResult authenticated = repository.pendingProfile();
                if (authenticated != null) enableProfileCompletion(authenticated.email);
            }
            if (current.kind == AuthViewModel.State.Kind.PROFILE_REQUIRED) {
                enableProfileCompletion(current.email);
                hideStatus();
            }
            if (current.kind == AuthViewModel.State.Kind.AUTHENTICATED
                    || current.kind == AuthViewModel.State.Kind.REGISTERED) {
                setResult(RESULT_OK);
                finish();
            }
        });

        googleButton.setOnClickListener(v -> {
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
        nextButton.setOnClickListener(v -> {
            if (!validatePersonalDetails()) return;
            showStep(1);
            hideStatus();
        });
        backButton.setOnClickListener(v -> {
            showStep(0);
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
        if (!CpfValidator.isValid(cpfDigits)) return invalid(cpf, "Confira o CPF informado. Os dígitos verificadores não são válidos.");
        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() || emailValue.length() > 150)
            return invalid(email, "Informe um e-mail válido.");
        if (!profileCompletion) {
            String passwordValue = password.getText().toString();
            if (passwordValue.length() < 8 || passwordValue.length() > 100)
                return invalid(password, "Use uma senha de 8 a 100 caracteres.");
            if (!passwordValue.equals(confirmPassword.getText().toString()))
                return invalid(confirmPassword, "As senhas precisam ser iguais.");
        }
        if (!RegistrationFields.validPhone(phoneValue)) {
            return invalid(phone, "Informe DDD e telefone: 10 dígitos para fixo ou 11 para celular, começando com 9.");
        }
        try {
            RegistrationFields.birthDateIso(birthValue);
        } catch (DateTimeParseException exception) {
            return invalid(birthDate, "Informe uma data válida no formato DD/MM/AAAA.");
        } catch (IllegalArgumentException exception) {
            return invalid(birthDate, exception.getMessage());
        }
        return true;
    }

    private CreateCustomerRequest validateAndBuild() {
        if (!validatePersonalDetails()) {
            showStep(0);
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

        String passwordValue = profileCompletion ? "" : password.getText().toString();
        String birthValue = value(birthDate);
        return new CreateCustomerRequest(value(name), digits(cpf), value(email), passwordValue,
                RegistrationFields.phoneDigits(value(phone)), RegistrationFields.birthDateIso(birthValue), marketing.isChecked(),
                new CreateAddressRequest(zip, streetValue, numberValue, value(complement),
                        neighborhoodValue, cityValue, stateValue));
    }

    private void enableProfileCompletion(String accountEmail) {
        profileCompletion = true;
        if (accountEmail != null) email.setText(accountEmail);
        email.setEnabled(false);
        findViewById(R.id.text_register_account).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_register_change_account).setVisibility(View.VISIBLE);
        password.setVisibility(View.GONE);
        password.setText("");
        confirmPassword.setVisibility(View.GONE);
        confirmPassword.setText("");
        googleButton.setVisibility(View.GONE);
    }

    private void showStep(int step) {
        pages.setDisplayedChild(step == 1 ? 1 : 0);
        stepLabel.setText(step == 1 ? "Etapa 2 de 2 · Endereço" : "Etapa 1 de 2 · Seus dados");
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("register_step", pages.getDisplayedChild());
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
