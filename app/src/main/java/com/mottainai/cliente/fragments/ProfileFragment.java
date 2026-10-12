package com.mottainai.cliente.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mottainai.cliente.R;
import com.mottainai.cliente.activities.LoginClienteActivity;
import com.mottainai.cliente.network.dto.CustomerDetailsResponse;
import com.mottainai.cliente.network.dto.LoyaltyAccount;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.utils.SessionManager;
import com.mottainai.cliente.viewmodel.LoyaltyViewModel;
import com.mottainai.cliente.viewmodel.ProfileViewModel;

import java.util.Locale;

public class ProfileFragment extends Fragment {
    private ActivityResultLauncher<PickVisualMediaRequest> avatarPicker;
    private AlertDialog editDialog;
    private Button editSaveButton;
    private TextView editStatus;
    private boolean saveAttempted;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        avatarPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                this::showAvatarPreview);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.row_preferences).setVisibility(View.GONE);
        view.findViewById(R.id.row_help).setVisibility(View.GONE);

        ProfileViewModel profileModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        LoyaltyViewModel loyaltyModel = new ViewModelProvider(this).get(LoyaltyViewModel.class);
        TextView name = view.findViewById(R.id.tv_profile_name);
        TextView email = view.findViewById(R.id.tv_profile_level);
        TextView location = view.findViewById(R.id.tv_profile_location);
        TextView initials = view.findViewById(R.id.tv_avatar_initials);
        TextView status = view.findViewById(R.id.tv_profile_status);
        View retry = view.findViewById(R.id.btn_profile_retry);
        View editData = view.findViewById(R.id.row_your_data);
        String customerId = new SessionManager(requireContext()).getClientId();

        view.findViewById(R.id.btn_change_avatar).setOnClickListener(v ->
                avatarPicker.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
        view.findViewById(R.id.btn_profile_points_retry).setOnClickListener(v -> {
            if (customerId != null && !customerId.trim().isEmpty()) {
                loyaltyModel.loadAccount(customerId);
            }
        });
        retry.setOnClickListener(v -> profileModel.load());
        editData.setOnClickListener(v -> showEditDialog(profileModel));
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> confirmLogout());

        profileModel.state().observe(getViewLifecycleOwner(), current -> {
            if (current == null) return;
            retry.setVisibility(current.kind == ProfileViewModel.State.Kind.ERROR
                    ? View.VISIBLE : View.GONE);
            editData.setEnabled(current.profile != null
                    && current.kind != ProfileViewModel.State.Kind.LOADING
                    && current.kind != ProfileViewModel.State.Kind.SAVING);

            if (current.profile != null) {
                renderProfile(current.profile, name, email, location, initials);
                status.setText(current.kind == ProfileViewModel.State.Kind.ERROR
                        ? current.message : "");
            } else if (current.kind == ProfileViewModel.State.Kind.LOADING) {
                status.setText("Carregando perfil…");
            } else if (current.kind == ProfileViewModel.State.Kind.ERROR) {
                status.setText(current.message);
            }
            renderEditState(current);
        });

        loyaltyModel.account().observe(getViewLifecycleOwner(), state -> renderPoints(view, state));
        if (customerId == null || customerId.trim().isEmpty()) {
            ((TextView) view.findViewById(R.id.tv_profile_points_status))
                    .setText("Entre novamente para consultar seus pontos.");
        } else if (loyaltyModel.account().getValue() == null) {
            loyaltyModel.loadAccount(customerId);
        }

        ProfileViewModel.State current = profileModel.state().getValue();
        if (current == null || current.kind == ProfileViewModel.State.Kind.IDLE) {
            profileModel.load();
        }
    }

    private void renderProfile(CustomerDetailsResponse profile, TextView name, TextView email,
                               TextView location, TextView initials) {
        String fullName = profile.fullName == null ? "Cliente" : profile.fullName.trim();
        name.setText(fullName.isEmpty() ? "Cliente" : fullName);
        email.setText(profile.email == null ? "" : profile.email);
        initials.setText(fullName.isEmpty() ? "C"
                : fullName.substring(0, 1).toUpperCase(Locale.ROOT));
        if (profile.address == null || profile.address.city == null
                || profile.address.city.trim().isEmpty()) {
            location.setText("Endereço não cadastrado");
        } else {
            String city = profile.address.city.trim();
            String state = profile.address.state == null ? "" : profile.address.state.trim();
            location.setText(state.isEmpty() ? city : city + " - " + state);
        }
    }

    private void renderPoints(View view, LoadState<LoyaltyAccount> state) {
        if (state == null) return;
        TextView balance = view.findViewById(R.id.tv_profile_points_balance);
        TextView status = view.findViewById(R.id.tv_profile_points_status);
        View retry = view.findViewById(R.id.btn_profile_points_retry);
        if (state.status == LoadState.Status.LOADING) {
            status.setText("Carregando saldo…");
            retry.setVisibility(View.GONE);
        } else if (state.status == LoadState.Status.ERROR) {
            balance.setText("— pts");
            status.setText(state.message);
            retry.setVisibility(View.VISIBLE);
        } else if (state.data == null || state.data.pointsBalance == null) {
            balance.setText("— pts");
            status.setText("Saldo indisponível no momento.");
            retry.setVisibility(View.GONE);
        } else {
            balance.setText(String.format(Locale.forLanguageTag("pt-BR"),
                    "%,d pts", state.data.pointsBalance));
            status.setText("");
            retry.setVisibility(View.GONE);
        }
    }

    private void showEditDialog(ProfileViewModel model) {
        CustomerDetailsResponse profile = model.currentProfile();
        if (profile == null) return;

        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_profile, null, false);
        EditText name = content.findViewById(R.id.input_profile_name);
        EditText phone = content.findViewById(R.id.input_profile_phone);
        CheckBox marketing = content.findViewById(R.id.check_profile_marketing);
        editStatus = content.findViewById(R.id.tv_edit_profile_status);
        name.setText(profile.fullName == null ? "" : profile.fullName);
        phone.setText(profile.phone == null ? "" : profile.phone);
        marketing.setChecked(Boolean.TRUE.equals(profile.marketingConsent));

        editDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Editar dados do perfil")
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();
        editDialog.setOnDismissListener(dialog -> {
            editDialog = null;
            editSaveButton = null;
            editStatus = null;
            saveAttempted = false;
        });
        editDialog.setOnShowListener(dialog -> {
            editSaveButton = editDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            editDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> editDialog.dismiss());
            editSaveButton.setOnClickListener(v -> {
                String fullName = name.getText().toString().trim();
                if (fullName.isEmpty()) {
                    name.setError("Informe seu nome.");
                    return;
                }
                saveAttempted = true;
                model.update(fullName, phone.getText().toString(), marketing.isChecked());
            });
        });
        editDialog.show();
    }

    private void renderEditState(ProfileViewModel.State state) {
        if (editDialog == null || editStatus == null || editSaveButton == null) return;
        if (state.kind == ProfileViewModel.State.Kind.SAVING) {
            editDialog.setCancelable(false);
            editSaveButton.setEnabled(false);
            editDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            editStatus.setVisibility(View.VISIBLE);
            editStatus.setText("Salvando alterações…");
        } else if (saveAttempted && state.kind == ProfileViewModel.State.Kind.READY) {
            saveAttempted = false;
            editDialog.dismiss();
            Toast.makeText(requireContext(), "Perfil atualizado.", Toast.LENGTH_SHORT).show();
        } else if (saveAttempted && state.kind == ProfileViewModel.State.Kind.ERROR) {
            saveAttempted = false;
            editDialog.setCancelable(true);
            editSaveButton.setEnabled(true);
            editDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
            editStatus.setVisibility(View.VISIBLE);
            editStatus.setText(state.message);
        }
    }

    private void showAvatarPreview(Uri uri) {
        if (uri == null || getView() == null) return;
        ImageView photo = getView().findViewById(R.id.iv_avatar_photo);
        TextView initials = getView().findViewById(R.id.tv_avatar_initials);
        TextView note = getView().findViewById(R.id.tv_avatar_note);
        try {
            photo.setImageURI(uri);
            photo.setVisibility(View.VISIBLE);
            initials.setVisibility(View.GONE);
            note.setText("Prévia local selecionada; a foto ainda não foi enviada.");
        } catch (RuntimeException exception) {
            Toast.makeText(requireContext(), "Não foi possível abrir essa imagem.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sair da conta?")
                .setMessage("Você precisará entrar novamente para acessar seu perfil.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sair", (dialog, which) -> {
                    new SessionManager(requireContext()).clearSession();
                    Intent login = new Intent(requireContext(), LoginClienteActivity.class);
                    login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(login);
                })
                .show();
    }
}
