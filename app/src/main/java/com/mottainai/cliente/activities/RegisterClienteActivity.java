package com.mottainai.cliente.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.mottainai.cliente.R;
import com.mottainai.cliente.network.dto.CreateAddressRequest;
import com.mottainai.cliente.network.dto.CreateCustomerRequest;
import com.mottainai.cliente.repository.AuthRepository;
import com.mottainai.cliente.viewmodel.AuthViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class RegisterClienteActivity extends AppCompatActivity {
    private EditText name, cpf, email, password, phone, birthDate;
    private EditText cep, street, number, complement, neighborhood, city, state;
    private CheckBox marketing;
    private TextView status;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_cliente);
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
        Button submit = findViewById(R.id.btn_register_submit);
        Button findCep = findViewById(R.id.btn_register_find_cep);
        AuthRepository repository = new AuthRepository(this);
        AuthViewModel model = new ViewModelProvider(this).get(AuthViewModel.class);

        model.address().observe(this, address -> {
            if (address == null || !digits(cep).equals(digits(address.cep))) return;
            street.setText(address.logradouro);
            neighborhood.setText(address.bairro);
            city.setText(address.localidade);
            state.setText(address.uf);
            status.setVisibility(View.GONE);
        });
        model.cepError().observe(this, message -> {
            if (message != null) showStatus(message);
        });
        model.state().observe(this, current -> {
            submit.setEnabled(current.kind != AuthViewModel.State.Kind.LOADING);
            if (current.kind == AuthViewModel.State.Kind.LOADING) showStatus("Cadastrando…");
            if (current.kind == AuthViewModel.State.Kind.ERROR) showStatus(current.message);
            if (current.kind == AuthViewModel.State.Kind.REGISTERED) {
                setResult(RESULT_OK);
                finish();
            }
        });
        findCep.setOnClickListener(v -> {
            String zip = digits(cep);
            if (zip.length() != 8) {
                cep.setError("Informe os 8 dígitos do CEP.");
                return;
            }
            showStatus("Consultando CEP…");
            model.lookupCep(repository, zip);
        });
        submit.setOnClickListener(v -> {
            CreateCustomerRequest request = validateAndBuild();
            if (request != null) model.register(repository, request);
        });
    }

    private CreateCustomerRequest validateAndBuild() {
        String fullName = value(name);
        String cpfDigits = digits(cpf);
        String emailValue = value(email);
        String passwordValue = password.getText().toString();
        String phoneValue = value(phone);
        String birthValue = value(birthDate);
        String zip = digits(cep);
        String streetValue = value(street);
        String numberValue = value(number);
        String neighborhoodValue = value(neighborhood);
        String cityValue = value(city);
        String stateValue = value(state).toUpperCase(Locale.ROOT);

        if (fullName.isEmpty() || fullName.length() > 150) return invalid(name, "Informe seu nome (até 150 caracteres).");
        if (cpfDigits.length() != 11) return invalid(cpf, "Informe os 11 dígitos do CPF.");
        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() || emailValue.length() > 150)
            return invalid(email, "Informe um e-mail válido.");
        if (passwordValue.length() < 8 || passwordValue.length() > 100)
            return invalid(password, "Use uma senha de 8 a 100 caracteres.");
        if (phoneValue.isEmpty() || phoneValue.length() > 20) return invalid(phone, "Informe seu telefone.");
        if (!birthValue.isEmpty()) {
            try {
                if (LocalDate.parse(birthValue).isAfter(LocalDate.now()))
                    return invalid(birthDate, "A data de nascimento não pode ser futura.");
            } catch (DateTimeParseException exception) {
                return invalid(birthDate, "Use o formato AAAA-MM-DD.");
            }
        }
        if (zip.length() != 8) return invalid(cep, "Informe os 8 dígitos do CEP.");
        if (streetValue.isEmpty() || streetValue.length() > 150) return invalid(street, "Informe a rua.");
        if (numberValue.isEmpty() || numberValue.length() > 10) return invalid(number, "Informe o número.");
        if (value(complement).length() > 100) return invalid(complement, "Complemento muito longo.");
        if (neighborhoodValue.isEmpty() || neighborhoodValue.length() > 100)
            return invalid(neighborhood, "Informe o bairro.");
        if (cityValue.isEmpty() || cityValue.length() > 100) return invalid(city, "Informe a cidade.");
        if (!stateValue.matches("[A-Z]{2}")) return invalid(state, "Use a sigla do estado (UF).");

        return new CreateCustomerRequest(fullName, cpfDigits, emailValue, passwordValue,
                phoneValue, birthValue.isEmpty() ? null : birthValue, marketing.isChecked(),
                new CreateAddressRequest(zip, streetValue, numberValue, value(complement),
                        neighborhoodValue, cityValue, stateValue));
    }

    private CreateCustomerRequest invalid(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
        return null;
    }

    private void showStatus(String message) {
        status.setText(message);
        status.setVisibility(View.VISIBLE);
    }

    private static String value(EditText field) { return field.getText().toString().trim(); }
    private static String digits(EditText field) { return digits(value(field)); }
    private static String digits(String value) { return value == null ? "" : value.replaceAll("\\D", ""); }
}
