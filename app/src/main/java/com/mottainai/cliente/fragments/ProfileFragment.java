package com.mottainai.cliente.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mottainai.cliente.R;
import com.mottainai.cliente.activities.LoginClienteActivity;
import com.mottainai.cliente.network.dto.CustomerProfileResponse;
import com.mottainai.cliente.network.dto.UpdateFirebaseCustomerProfileRequest;
import com.mottainai.cliente.utils.SessionManager;
import com.mottainai.cliente.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {
    private CustomerProfileResponse profile;
    private AlertDialog editDialog;
    private LinearLayout editForm;
    private TextView editStatus;
    private boolean savingProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View yourData = view.findViewById(R.id.row_your_data);
        view.findViewById(R.id.row_preferences).setVisibility(View.GONE);
        view.findViewById(R.id.row_help).setVisibility(View.GONE);

        ProfileViewModel model = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        TextView name = view.findViewById(R.id.tv_profile_name);
        TextView email = view.findViewById(R.id.tv_profile_level);
        TextView initials = view.findViewById(R.id.tv_avatar_initials);
        TextView status = view.findViewById(R.id.tv_profile_status);
        View retry = view.findViewById(R.id.btn_profile_retry);
        boolean demoMode = new SessionManager(requireContext()).isDemoMode();
        yourData.setEnabled(false);
        yourData.setOnClickListener(v -> showEditProfile(model));
        model.state().observe(getViewLifecycleOwner(), current -> {
            if (current == null) return;
            retry.setVisibility(current.kind == ProfileViewModel.State.Kind.ERROR
                    ? View.VISIBLE : View.GONE);
            if (current.kind == ProfileViewModel.State.Kind.LOADING) {
                status.setText("Carregando perfil…");
                yourData.setEnabled(false);
            } else if (current.kind == ProfileViewModel.State.Kind.SAVING) {
                status.setText("Salvando seus dados…");
                yourData.setEnabled(false);
            } else if (current.kind == ProfileViewModel.State.Kind.ERROR) {
                status.setText(current.message);
                if (current.profile != null) {
                    showProfile(current.profile, name, email, initials);
                    yourData.setEnabled(!demoMode);
                }
            } else if (current.kind == ProfileViewModel.State.Kind.READY) {
                showProfile(current.profile, name, email, initials);
                status.setText(current.message != null ? current.message
                        : demoMode ? "Dados fictícios. Edição desativada na demonstração." : "");
                yourData.setEnabled(!demoMode);
            }
            renderEditState(current);
        });
        retry.setOnClickListener(v -> model.load());
        if (model.state().getValue() == null
                || model.state().getValue().kind == ProfileViewModel.State.Kind.IDLE) {
            model.load();
        }

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Sair da conta?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Sair", (dialog, which) -> {
                        SessionManager session = new SessionManager(requireContext());
                        if (session.isDemoMode()) session.exitDemoMode();
                        else session.clearSession();
                        Intent login = new Intent(requireContext(), LoginClienteActivity.class);
                        login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(login);
                    }).show();
        });
    }

    private void showProfile(CustomerProfileResponse current, TextView name,
                             TextView email, TextView initials) {
        profile = current;
        String fullName = current.fullName == null ? "Cliente" : current.fullName.trim();
        name.setText(fullName.isEmpty() ? "Cliente" : fullName);
        email.setText(current.email == null ? "" : current.email);
        initials.setText(fullName.isEmpty() ? "C"
                : fullName.substring(0, 1).toUpperCase(java.util.Locale.ROOT));
    }

    private void showEditProfile(ProfileViewModel model) {
        if (profile == null) return;
        Context context = requireContext();
        LinearLayout form = new LinearLayout(context);
        editForm = form;
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(8), 0, dp(8), 0);

        EditText fullName = addTextField(form, "Nome completo", profile.fullName,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS, 150);
        EditText phone = addTextField(form, "Novo telefone (deixe vazio para manter)", null,
                InputType.TYPE_CLASS_PHONE, 20);
        CheckBox marketingConsent = new CheckBox(context);
        marketingConsent.setText("Aceito receber comunicações e novidades");
        marketingConsent.setChecked(Boolean.TRUE.equals(profile.marketingConsent));
        form.addView(marketingConsent, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editStatus = new TextView(context);
        form.addView(editStatus);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Editar seus dados")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();
        editDialog = dialog;
        dialog.setOnDismissListener(ignored -> {
            editDialog = null;
            editForm = null;
            editStatus = null;
            savingProfile = false;
        });
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = fullName.getText().toString().trim();
                    if (name.isEmpty()) {
                        fullName.setError("Informe seu nome.");
                        return;
                    }
                    String newPhone = phone.getText().toString().trim();
                    savingProfile = true;
                    model.save(new UpdateFirebaseCustomerProfileRequest(name,
                            newPhone.isEmpty() ? null : newPhone, marketingConsent.isChecked()));
                }));
        dialog.show();
    }

    private void renderEditState(ProfileViewModel.State current) {
        if (editDialog == null || !editDialog.isShowing() || !savingProfile) return;
        if (current.kind == ProfileViewModel.State.Kind.READY) {
            editDialog.dismiss();
            return;
        }
        boolean saving = current.kind == ProfileViewModel.State.Kind.SAVING;
        editDialog.setCancelable(!saving);
        editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!saving);
        editDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(!saving);
        for (int i = 0; i < editForm.getChildCount(); i++) editForm.getChildAt(i).setEnabled(!saving);
        editStatus.setText(saving ? "Salvando seus dados…" : current.message);
        if (!saving) savingProfile = false;
    }

    @Override public void onDestroyView() {
        if (editDialog != null) editDialog.dismiss();
        super.onDestroyView();
    }

    private EditText addTextField(LinearLayout parent, String hint, String value,
                                  int inputType, int maxLength) {
        EditText input = new EditText(requireContext());
        input.setHint(hint);
        input.setSingleLine(true);
        input.setInputType(inputType);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        if (value != null) input.setText(value);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(8);
        parent.addView(input, params);
        return input;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
