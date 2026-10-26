package com.mottainai.cliente.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mottainai.cliente.MainActivity;
import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.ChatAdapter;
import com.mottainai.cliente.models.ChatMessage;
import com.mottainai.cliente.repository.AssistantReplyEngine;
import com.mottainai.cliente.repository.MockOfferRepository;
import com.mottainai.cliente.repository.MockUserRepository;
import com.mottainai.cliente.repository.ScreenRepository;
import com.mottainai.cliente.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Shared behavior for native XML screens; links follow the Figma prototype. */
public class ScreenFragment extends Fragment {
    public String getScreenId() {
        return requireArguments().getString("screen", "2005:3812");
    }

    private String screen;
    private ScreenRepository repository;
    private final List<EditText> fields = new ArrayList<>();
    private com.mottainai.cliente.widgets.ClientMapView pendingMap;
    private final androidx.activity.result.ActivityResultLauncher<String> location =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts
                            .RequestPermission(),
                    allowed -> {
                        if (pendingMap != null) pendingMap.onPermissionResult(allowed);
                    });

    public void requestMapLocation(com.mottainai.cliente.widgets.ClientMapView map) {
        pendingMap = map;
        location.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private final androidx.activity.result.ActivityResultLauncher<String> notifications =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts
                            .RequestPermission(),
                    allowed -> {
                        preferences().edit().putBoolean("nearby_alerts", allowed).apply();
                        open(allowed ? "2557:30760" : "2557:30824");
                    });
    private final androidx.activity.result.ActivityResultLauncher<String> gallery =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) return;
                        try {
                            android.graphics.BitmapFactory.Options options =
                                    new android.graphics.BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            try (java.io.InputStream in =
                                    requireContext().getContentResolver().openInputStream(uri)) {
                                android.graphics.BitmapFactory.decodeStream(in, null, options);
                            }
                            options.inJustDecodeBounds = false;
                            options.inSampleSize = 1;
                            while (Math.max(options.outWidth, options.outHeight)
                                            / options.inSampleSize
                                    > 1024) options.inSampleSize *= 2;
                            android.graphics.Bitmap bitmap;
                            try (java.io.InputStream in =
                                    requireContext().getContentResolver().openInputStream(uri)) {
                                bitmap =
                                        android.graphics.BitmapFactory.decodeStream(
                                                in, null, options);
                            }
                            try (java.io.InputStream in =
                                    requireContext().getContentResolver().openInputStream(uri)) {
                                android.media.ExifInterface exif =
                                        new android.media.ExifInterface(in);
                                int orientation =
                                        exif.getAttributeInt(
                                                android.media.ExifInterface.TAG_ORIENTATION,
                                                android.media.ExifInterface.ORIENTATION_NORMAL);
                                android.graphics.Matrix matrix = new android.graphics.Matrix();
                                switch (orientation) {
                                    case 2:
                                        matrix.setScale(-1, 1);
                                        break;
                                    case 3:
                                        matrix.setRotate(180);
                                        break;
                                    case 4:
                                        matrix.setScale(1, -1);
                                        break;
                                    case 5:
                                        matrix.setRotate(90);
                                        matrix.postScale(-1, 1);
                                        break;
                                    case 6:
                                        matrix.setRotate(90);
                                        break;
                                    case 7:
                                        matrix.setRotate(-90);
                                        matrix.postScale(-1, 1);
                                        break;
                                    case 8:
                                        matrix.setRotate(-90);
                                        break;
                                }
                                if (bitmap != null && !matrix.isIdentity())
                                    bitmap =
                                            android.graphics.Bitmap.createBitmap(
                                                    bitmap,
                                                    0,
                                                    0,
                                                    bitmap.getWidth(),
                                                    bitmap.getHeight(),
                                                    matrix,
                                                    true);
                            } catch (java.io.IOException ignored) {
                                /* Formats without EXIF are already upright. */
                            }
                            savePendingPhoto(bitmap);
                        } catch (Exception exception) {
                            photoError();
                        }
                    });
    private final androidx.activity.result.ActivityResultLauncher<Void> camera =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts
                            .TakePicturePreview(),
                    this::savePendingPhoto);

    private void savePendingPhoto(android.graphics.Bitmap bitmap) {
        if (bitmap == null) return;
        try (java.io.FileOutputStream out =
                new java.io.FileOutputStream(
                        new java.io.File(
                                requireContext().getFilesDir(), "profile-photo-pending.jpg"))) {
            if (!bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out))
                throw new java.io.IOException();
            open("2571:30962");
        } catch (Exception exception) {
            photoError();
        }
    }

    private void photoError() {
        if (isAdded())
            Toast.makeText(requireContext(), R.string.design_photo_error, Toast.LENGTH_LONG).show();
    }

    public static ScreenFragment create(String screen) {
        ScreenFragment fragment;
        switch (screen) {
            case "2005:3812":
                fragment = new HomeFragment();
                break;
            case "2005:3914":
                fragment = new CatalogFragment();
                break;
            case "2005:4305":
                fragment = new StoresFragment();
                break;
            case "2093:6331":
                fragment = new PointsFragment();
                break;
            case "2005:4483":
                fragment = new ProfileFragment();
                break;
            case "2143:11079":
                fragment = new ImpactFragment();
                break;
            default:
                fragment = new ScreenFragment();
        }
        Bundle arguments = new Bundle();
        arguments.putString("screen", screen);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle state) {
        screen = requireArguments().getString("screen", "2005:3812");
        repository = ScreenRepository.get(requireContext());
        int layout = repository.layoutFor(screen);
        fields.clear();
        return inflater.inflate(layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        boolean artwork =
                screen.equals("2244:11739")
                        || screen.equals("2244:11824")
                        || screen.equals("2143:9806");
        boolean overlay = repository.screen(screen).optBoolean("overlay");
        if (!overlay) host().setArtworkFullBleed(artwork);
        com.mottainai.cliente.utils.Typography.apply(view);
        placeConversationHistoryInHeader(view);
        bind(view);
        for (EditText field : fields) {
            String label = tag(field).optString("field");
            if (!label.equals("Busca") && !label.equals("Mensagem"))
                com.mottainai.cliente.utils.FormControls.prepare(
                        field, label.toLowerCase(java.util.Locale.ROOT).contains("senha"));
        }
        updateConsent(view, preferences().getBoolean("marketing", false));
        if (!host().isDesignPreview())
            com.mottainai.cliente.utils.ScreenContent.bind(this, view, screen, preferences());
        boolean camera = screen.equals("2680:78487");
        requireActivity()
                .findViewById(R.id.design_container)
                .setBackgroundColor(
                        camera
                                ? Color.parseColor("#070B10")
                                : androidx.core.content.ContextCompat.getColor(
                                        requireContext(), R.color.design_canvas));
        boolean lightBars = !isDark() && !camera;
        androidx.core.view.WindowInsetsControllerCompat bars =
                androidx.core.view.WindowCompat.getInsetsController(
                        requireActivity().getWindow(),
                        requireActivity().getWindow().getDecorView());
        if (!overlay) {
            bars.setAppearanceLightStatusBars(lightBars);
            bars.setAppearanceLightNavigationBars(lightBars && !artwork);
        }
    }

    private MainActivity host() {
        return (MainActivity) requireActivity();
    }

    private void placeConversationHistoryInHeader(View root) {
        if (!repository.screen(screen).optString("name").contains("Conversa com a Lô")
                || repository.screen(screen).optBoolean("overlay")) return;
        View candidate = com.mottainai.cliente.utils.ScreenContent.find(root, "Cabeçalho");
        if (!(candidate instanceof LinearLayout)) return;
        LinearLayout header = (LinearLayout) candidate;
        View history = findAction(root, "Minhas conversas");
        if (history != null) {
            ViewGroup previous = (ViewGroup) history.getParent();
            previous.removeView(history);
            if (previous.getChildCount() == 0) previous.setVisibility(View.GONE);
        } else {
            TextView button =
                    com.mottainai.cliente.utils.ScreenContent.label(
                            requireContext(), getString(R.string.design_chat_history), 14);
            button.setTag("{\"name\":\"Histórico de conversas\",\"action\":\"Minhas conversas\"}");
            button.setClickable(true);
            button.setFocusable(true);
            history = button;
        }
        history.setMinimumHeight(dp(48));
        history.setPadding(dp(8), dp(4), dp(8), dp(4));
        styleHistoryLabel(history);
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dp(12));
        background.setColor(Color.parseColor(isDark() ? "#1B3558" : "#EAF2FC"));
        history.setBackground(background);
        LinearLayout.LayoutParams position =
                new LinearLayout.LayoutParams(dp(128), ViewGroup.LayoutParams.WRAP_CONTENT);
        position.setMarginStart(dp(8));
        header.addView(history, position);
    }

    private void styleHistoryLabel(View view) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            text.setGravity(Gravity.CENTER);
            text.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                            requireContext(), R.color.design_primary));
        }
        if (view instanceof ViewGroup)
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
                styleHistoryLabel(((ViewGroup) view).getChildAt(i));
    }

    private boolean isDark() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    private void bind(View view) {
        JSONObject tag = tag(view);
        if (view instanceof android.widget.ImageView && tag.optString("name").equals("Ellipse")) {
            java.io.File photo =
                    new java.io.File(
                            requireContext().getFilesDir(),
                            screen.equals("2571:30962")
                                    ? "profile-photo-pending.jpg"
                                    : "profile-photo.jpg");
            if (photo.isFile()) {
                android.widget.ImageView image = (android.widget.ImageView) view;
                image.setImageURI(Uri.fromFile(photo));
                image.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(Color.TRANSPARENT);
                image.setBackground(circle);
                image.setClipToOutline(true);
            } else if (!host().isDesignPreview()) {
                android.widget.ImageView image = (android.widget.ImageView) view;
                image.setImageResource(R.drawable.ic_profile);
                image.setImageTintList(
                        android.content.res.ColorStateList.valueOf(
                                androidx.core.content.ContextCompat.getColor(
                                        requireContext(), R.color.design_text)));
                image.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
            }
        }
        if (view instanceof TextView && !(view instanceof EditText)) {
            TextView text = (TextView) view;
            String value = text.getText().toString();
            String full = preferences().getString("field_Nome completo", null);
            if (full != null && !full.trim().isEmpty()) {
                String first = full.trim().split("\\s+")[0];
                if (value.equals("Camila")) text.setText(first);
                else if (value.equals("Camila Oliveira")) text.setText(full);
                else if (value.equals("Bom dia, Camila"))
                    text.setText(getString(R.string.design_greeting, first));
            }
            String radius = preferences().getString("radius", "5 km");
            if (value.equals("5 km") && !insideRadiusOption(view)) text.setText(radius);
            else if (value.equals("Até 5 km de você"))
                text.setText(getString(R.string.design_radius_title, radius));
        }
        if (view instanceof EditText) {
            EditText edit = (EditText) view;
            fields.add(edit);
            String fieldLabel = tag.optString("field");
            if (!host().isDesignPreview()
                    && !fieldLabel.equals("Mensagem")
                    && !fieldLabel.equals("Busca")) {
                edit.setText("");
                if (fieldLabel.toLowerCase(java.util.Locale.ROOT).contains("senha"))
                    edit.setHint(R.string.design_password_hint);
                else edit.setHint(fieldLabel);
            }
            if (!fieldLabel.toLowerCase(java.util.Locale.ROOT).contains("senha")
                    && !fieldLabel.equals("Mensagem")
                    && !fieldLabel.equals("Busca")) {
                String saved = preferences().getString("field_" + fieldLabel, null);
                if (saved != null) edit.setText(saved);
            }
            if (tag.optString("field").equals("Mensagem"))
                edit.setOnEditorActionListener(
                        (v, action, event) -> {
                            if (action == EditorInfo.IME_ACTION_SEND) {
                                sendChat();
                                return true;
                            }
                            return false;
                        });
            if (tag.optString("field").equals("Busca"))
                edit.setOnEditorActionListener(
                        (v, action, event) -> {
                            if (action == EditorInfo.IME_ACTION_SEARCH) {
                                String query =
                                        edit.getText()
                                                .toString()
                                                .toLowerCase(java.util.Locale.ROOT);
                                if (query.trim().isEmpty()) return true;
                                preferences()
                                        .edit()
                                        .putString("search_query", edit.getText().toString().trim())
                                        .apply();
                                open(
                                        !host().isDesignPreview()
                                                ? "2413:31330"
                                                : query.contains("leite")
                                                        ? "2019:4155"
                                                        : (query.contains("cesta")
                                                                        || query.contains(
                                                                                "hortifruti")
                                                                ? "2019:4067"
                                                                : "2413:31330"));
                                return true;
                            }
                            return false;
                        });
        }
        if (view instanceof TextView && tag.optBoolean("strike")) {
            TextView label = (TextView) view;
            label.setPaintFlags(label.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        String label = tag.optString("action").trim();
        if (label.equals("Sair")) {
            view.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#B3261E")));
            setTextColors(view, Color.WHITE);
        }
        JSONObject link =
                repository.action(screen, tag.optString("id"), tag.optString("name"), label);
        boolean consent =
                tag.optString("name").equals("Conta / Consentimento de marketing")
                        || tag.optString("name").equals("Consentimento / Opção documentada");
        if (view.isClickable() || link != null || consent) {
            view.setOnClickListener(v -> act(tag, label, link));
            view.setClickable(true);
            view.setFocusable(true);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) bind(group.getChildAt(i));
        }
    }

    private JSONObject tag(View view) {
        try {
            return new JSONObject(String.valueOf(view.getTag()));
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    private boolean validateFields() {
        for (EditText field : fields) {
            String value = field.getText().toString().trim();
            String label = tag(field).optString("field");
            if (label.equals("Busca")
                    || label.equals("Mensagem")
                    || label.toLowerCase(java.util.Locale.ROOT).contains("opcional")) continue;
            if (value.isEmpty()) {
                com.mottainai.cliente.utils.FormControls.error(
                        field, getString(R.string.design_required_field));
                return false;
            }
            if (label.toLowerCase(java.util.Locale.ROOT).contains("mail")
                    && !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                com.mottainai.cliente.utils.FormControls.error(
                        field, getString(R.string.design_invalid_email));
                return false;
            }
        }
        return true;
    }

    private void act(JSONObject tag, String label, JSONObject link) {
        String name = tag.optString("name");
        if (label.equals("Minhas conversas")) {
            open("2557:30504");
            return;
        }
        if (!host().isDesignPreview()) {
            if (label.equals("Ativar avisos") || screen.equals("2557:30792")) {
                requestNotificationPermission();
                return;
            }
            if (label.equals("Tirar foto") || label.equals("Permitir câmera")) {
                try {
                    camera.launch(null);
                } catch (android.content.ActivityNotFoundException ignored) {
                    photoError();
                }
                return;
            }
            if (label.equals("Lojas próximas") || label.equals("Como chegar")) {
                com.mottainai.cliente.widgets.ClientMapView.openMaps(requireContext());
                return;
            }
            if (label.equals("Como retirar?")) {
                sendSuggestion("Como funciona a retirada?");
                return;
            }
        }
        if (label.equals("Começar") || label.equals("Pular apresentação")) {
            new SessionManager(requireContext()).setOnboardingDone();
            host().showScreen("2028:4492", false);
            return;
        }
        if (name.startsWith("Fundo /")
                || name.startsWith("Voltar ")
                || name.equals("Fechar histórico")
                || name.equals("Cancelar captura")) {
            host().getOnBackPressedDispatcher().onBackPressed();
            return;
        }
        if (name.equals("Campo / Buscar produtos")) {
            for (EditText field : fields)
                if (tag(field).optString("field").equals("Busca")) {
                    field.requestFocus();
                    android.view.inputmethod.InputMethodManager keyboard =
                            (android.view.inputmethod.InputMethodManager)
                                    requireContext()
                                            .getSystemService(
                                                    android.content.Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(
                            field, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            return;
        }
        if (name.startsWith("Mensagem /")
                || name.equals("Escrever mensagem")
                || name.equals("Campo / mensagem")
                || name.equals("Enviar mensagem")) {
            sendChat();
            return;
        }
        if (name.contains("Alternar tema")) {
            host().setAppearance(!isDark());
            return;
        }
        if (label.equals("Claro") || label.equals("Escuro")) {
            host().setAppearance(label.equals("Escuro"));
            return;
        }
        if (name.startsWith("Destino /")) {
            String[] labels = {"Início", "Ofertas", "Lojas", "Pontos", "Perfil"};
            String[] ids = {"2005:3812", "2005:3914", "2005:4305", "2093:6331", "2005:4483"};
            for (int i = 0; i < labels.length; i++)
                if (name.endsWith(labels[i])) {
                    host().showScreen(ids[i], false);
                    return;
                }
        }
        if (name.contains("Flutuante") || name.contains("flutuante")) {
            open("2055:5379");
            return;
        }
        if (label.equals("Entrar")) {
            if (validateFields()) {
                saveFields();
                host().setLocalSession(true);
                host().showScreen("2005:3812", false);
            }
            return;
        }
        if (label.equals("Continuar com Google") || label.equals("Confirmar com Google")) {
            Toast.makeText(requireContext(), R.string.design_google_unavailable, Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (label.equals("Sair da conta")) {
            open("2028:4409");
            return;
        }
        if (label.equals("Sair")) {
            host().setLocalSession(false);
            host().getSupportFragmentManager()
                    .popBackStack(
                            null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            host().showScreen("2028:4492", false);
            return;
        }
        if (label.startsWith("Voltar")
                || label.equals("Cancelar")
                || label.equals("Continuar no app")
                || label.equals("Fechar")
                || label.equals("Entendi")) {
            if (screen.equals("2571:30962"))
                new java.io.File(requireContext().getFilesDir(), "profile-photo-pending.jpg")
                        .delete();
            if (link != null) {
                JSONArray destinations = link.optJSONArray("targets");
                if (destinations != null)
                    for (int i = 0; i < destinations.length(); i++)
                        if (repository.available(destinations.optString(i))) {
                            open(destinations.optString(i));
                            return;
                        }
            }
            if (label.contains("conta") && !label.equals("Continuar no app")
                    || label.equals("Fechar")
                    || label.equals("Entendi")) {
                open("2022:4219");
                return;
            }
            host().getOnBackPressedDispatcher().onBackPressed();
            return;
        }
        if (label.equals("Criar conta")) {
            if (screen.equals("2143:9630")) open("2143:9720");
            else open("2143:9435");
            return;
        }
        if (label.equals("Continuar") && screen.equals("2143:9896")) {
            if (validateFields()) {
                saveFields();
                open("2143:9538");
            }
            return;
        }
        if (label.equals("Esqueci minha senha")) {
            open("2143:10074");
            return;
        }
        if (label.equals("Ver todas")) {
            open("2005:3914");
            return;
        }
        if (label.equals("Ver histórico")) {
            open("2093:6456");
            return;
        }
        if (label.equals("Como funcionam")) {
            open("2093:6831");
            return;
        }
        if (label.equals("Ofertas por perto")) {
            if (host().isDesignPreview()) open("2055:5739");
            else sendSuggestion("Quais ofertas estão disponíveis?");
            return;
        }
        if (label.contains("Cesta hortifruti") && label.startsWith("⌕")) {
            open("2019:4067");
            return;
        }
        if (label.contains("Leite") && label.startsWith("⌕")) {
            open("2019:4155");
            return;
        }
        if (name.equals("Buscar produtos")) {
            open("2005:4018");
            return;
        }
        if (name.contains("Abrir filtros")) {
            open("2143:10917");
            return;
        }
        if (name.equals("Opção / Configurações da conta")) {
            open("2022:4219");
            return;
        }
        if (name.equals("Opção / Pontos de impacto") || name.equals("Opção / Seu impacto")) {
            open("2093:6331");
            return;
        }
        if (name.equals("Opção / Economia e impacto")) {
            open("2143:11079");
            return;
        }
        if (name.contains("Região de busca") || name.equals("Opção / Minha região")) {
            open("2143:10256");
            return;
        }
        if (label.equals("Alterar e-mail")) {
            open("2028:4217");
            return;
        }
        if (label.equals("Como chegar")) {
            Intent map =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "geo:0,0?q="
                                            + Uri.encode(
                                                    "Rua Afonso Celso, 210, Vila Mariana, São"
                                                            + " Paulo")));
            try {
                startActivity(map);
            } catch (android.content.ActivityNotFoundException ignored) {
                open("2005:4305");
            }
            return;
        }
        if (label.equals("Escolher da galeria")) {
            gallery.launch("image/*");
            return;
        }
        if (name.equals("Capturar foto")) {
            try {
                camera.launch(null);
            } catch (android.content.ActivityNotFoundException ignored) {
                photoError();
            }
            return;
        }
        if (label.equals("Salvar foto")) {
            java.io.File pending =
                    new java.io.File(requireContext().getFilesDir(), "profile-photo-pending.jpg");
            if (pending.isFile()) {
                try {
                    java.nio.file.Files.move(
                            pending.toPath(),
                            new java.io.File(requireContext().getFilesDir(), "profile-photo.jpg")
                                    .toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    open("2571:30994");
                } catch (java.io.IOException exception) {
                    photoError();
                }
            } else {
                open("2571:30930");
            }
            return;
        }
        if (name.equals("Ellipse") || name.equals("Alterar foto do perfil")) {
            open("2571:30930");
            return;
        }
        if (name.contains("Região de busca")) {
            open("2143:10256");
            return;
        }
        if (label.matches("(5|10|15|20) km")) {
            preferences().edit().putString("radius", label).apply();
            open("2418:25646");
            return;
        }
        if (label.equals("Mercado da Praça")) {
            open("2005:4391");
            return;
        }
        if (label.equals("Todas as lojas") || label.equals("Limpar")) {
            open("2005:3914");
            return;
        }
        if (label.equals("320 pontos — Ver saldo")) {
            open("2093:6331");
            return;
        }
        if (label.equals("Revisar cadastro")) {
            if (validateFields()) {
                saveFields();
                open(screen.startsWith("2392:") ? "2392:19502" : "2143:9630");
            }
            return;
        }
        if (label.equals("Desativar avisos")) {
            preferences().edit().putBoolean("nearby_alerts", false).apply();
            open("2557:30824");
            return;
        }
        if (label.equals("Compartilhar")) {
            open("2557:31016");
            return;
        }
        if (name.equals("Conta / Consentimento de marketing")
                || name.equals("Consentimento / Opção documentada")) {
            boolean selected = !preferences().getBoolean("marketing", false);
            preferences().edit().putBoolean("marketing", selected).apply();
            if (getView() != null) updateConsent(getView(), selected);
            return;
        }
        if (label.equals("Salvar dados") || label.equals("Salvar preferências")) {
            if (!validateFields()) return;
            saveFields();
            if (label.equals("Salvar preferências")) openTransient("2650:73528", "2650:73585");
            else openTransient("2393:19298", "2393:19412");
            return;
        }
        if (link != null) {
            JSONArray targets = link.optJSONArray("targets");
            if (targets != null)
                for (int i = 0; i < targets.length(); i++) {
                    String target = targets.optString(i);
                    if (repository.available(target)
                            && !repository.canonical(target).equals(screen)) {
                        if (!fields.isEmpty() && !validateFields()) return;
                        saveFields();
                        open(target);
                        return;
                    }
                }
        }
        // A decorative group can carry a prototype pointer without being an action.
        // Do not interrupt navigation with a generic dialog for these containers.
    }

    private SharedPreferences preferences() {
        return new SessionManager(requireContext()).preferences();
    }

    private void saveFields() {
        SharedPreferences.Editor editor = preferences().edit();
        for (EditText field : fields) {
            String label = tag(field).optString("field");
            if (label.equals("Mensagem")
                    || label.equals("Busca")
                    || label.toLowerCase(java.util.Locale.ROOT).contains("senha")) continue;
            editor.putString("field_" + label, field.getText().toString());
        }
        editor.apply();
    }

    private void updateConsent(View root, boolean selected) {
        if (tag(root).optString("name").equals("Conta / Consentimento de marketing")
                || tag(root).optString("name").equals("Consentimento / Opção documentada")) {
            root.setSelected(selected);
            root.setContentDescription(
                    getString(
                            selected
                                    ? R.string.design_marketing_selected
                                    : R.string.design_marketing_unselected));
            View mark = selectionMark(root);
            if (mark != null) {
                androidx.appcompat.widget.AppCompatCheckBox checkbox;
                if (mark instanceof androidx.appcompat.widget.AppCompatCheckBox)
                    checkbox = (androidx.appcompat.widget.AppCompatCheckBox) mark;
                else {
                    ViewGroup parent = (ViewGroup) mark.getParent();
                    int index = parent.indexOfChild(mark);
                    checkbox = new androidx.appcompat.widget.AppCompatCheckBox(requireContext());
                    checkbox.setTag(mark.getTag());
                    checkbox.setMinWidth(0);
                    checkbox.setMinHeight(0);
                    checkbox.setPadding(0, 0, 0, 0);
                    checkbox.setClickable(false);
                    checkbox.setFocusable(false);
                    checkbox.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                    ViewGroup.LayoutParams params = mark.getLayoutParams();
                    params.width = dp(24);
                    params.height = dp(24);
                    parent.removeViewAt(index);
                    parent.addView(checkbox, index, params);
                    androidx.core.widget.CompoundButtonCompat.setButtonTintList(
                            checkbox,
                            new android.content.res.ColorStateList(
                                    new int[][] {
                                        new int[] {android.R.attr.state_checked}, new int[] {}
                                    },
                                    new int[] {
                                        androidx.core.content.ContextCompat.getColor(
                                                requireContext(), R.color.design_primary),
                                        Color.parseColor(isDark() ? "#8C9BAF" : "#65758A")
                                    }));
                }
                checkbox.setChecked(selected);
            }
        }
        if (root instanceof ViewGroup)
            for (int i = 0; i < ((ViewGroup) root).getChildCount(); i++)
                updateConsent(((ViewGroup) root).getChildAt(i), selected);
    }

    private View selectionMark(View root) {
        if (tag(root).optString("name").equals("Caixa de seleção")) return root;
        if (root instanceof ViewGroup)
            for (int i = 0; i < ((ViewGroup) root).getChildCount(); i++) {
                View result = selectionMark(((ViewGroup) root).getChildAt(i));
                if (result != null) return result;
            }
        return null;
    }

    private void openTransient(String loading, String destination) {
        MainActivity activity = host();
        activity.showScreen(loading, true);
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(
                        () -> {
                            if (activity.isFinishing() || activity.isDestroyed()) return;
                            Fragment current = activity.currentScreen();
                            if (current != null
                                    && loading.equals(((ScreenFragment) current).getScreenId())) {
                                activity.showScreen(destination, false);
                                if (destination.equals("2143:9806")) {
                                    new android.os.Handler(android.os.Looper.getMainLooper())
                                            .postDelayed(
                                                    () -> {
                                                        if (activity.isFinishing()
                                                                || activity.isDestroyed()) return;
                                                        Fragment welcome = activity.currentScreen();
                                                        if (welcome != null
                                                                && "2143:9806"
                                                                        .equals(
                                                                                ((ScreenFragment)
                                                                                                welcome)
                                                                                        .getScreenId())) {
                                                            activity.setLocalSession(true);
                                                            activity.showScreen("2005:3812", false);
                                                        }
                                                    },
                                                    900);
                                }
                            }
                        },
                        650);
    }

    private void open(String id) {
        if (id.equals("2557:30792") && !host().isDesignPreview()) {
            requestNotificationPermission();
            return;
        }
        String destination = null;
        switch (id) {
            case "2143:9720":
            case "2392:19586":
                destination = "2143:9806";
                break;
            case "2393:19298":
                destination = "2393:19412";
                break;
            case "2650:73528":
                destination = "2650:73585";
                break;
            case "2458:28686":
                destination = "2456:29136";
                break;
            case "2571:30994":
                destination = "2022:4219";
                break;
            case "2557:30408":
            case "2557:30472":
            case "2416:25074":
                destination = "2005:3812";
                break;
            case "2413:31188":
                destination = "2005:3914";
                break;
            case "2417:25322":
                destination = "2093:6331";
                break;
            case "2055:5629":
                destination = "2055:5739";
                break;
        }
        if (destination != null) openTransient(id, destination);
        else host().showScreen(id, true);
    }

    private void sendChat() {
        EditText composer = null;
        for (EditText field : fields)
            if (tag(field).optString("field").equals("Mensagem")) composer = field;
        if (composer == null || composer.getText().toString().trim().isEmpty()) return;
        String message = composer.getText().toString().trim();
        composer.setText("");
        androidx.core.widget.NestedScrollView scroll =
                requireView().findViewById(R.id.design_scroll);
        if (scroll == null || !(scroll.getChildAt(0) instanceof LinearLayout)) return;
        LinearLayout messages = (LinearLayout) scroll.getChildAt(0);
        if (!Boolean.TRUE.equals(messages.getTag(R.id.design_chat_user))) {
            messages.removeAllViews();
            messages.setMinimumHeight(0);
            messages.setGravity(Gravity.TOP);
            messages.setTag(R.id.design_chat_user, true);
        }
        addBubble(messages, message, true);
        String reply =
                host().isDesignPreview()
                        ? new AssistantReplyEngine(
                                        new MockOfferRepository(), new MockUserRepository())
                                .reply(message)
                                .getText()
                        : getString(R.string.design_chat_unavailable);
        addBubble(messages, reply, false);
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private void addBubble(LinearLayout messages, String text, boolean user) {
        ChatMessage message =
                new ChatMessage(user ? ChatMessage.Sender.USER : ChatMessage.Sender.BOT, text);
        messages.addView(ChatAdapter.createMessageView(messages, message));
    }

    private void sendSuggestion(String message) {
        for (EditText f : fields)
            if (tag(f).optString("field").equals("Mensagem")) {
                f.setText(message);
                sendChat();
                return;
            }
    }

    private View findAction(View v, String action) {
        if (tag(v).optString("action").trim().equals(action) && v.isClickable()) return v;
        if (v instanceof ViewGroup)
            for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
                View found = findAction(((ViewGroup) v).getChildAt(i), action);
                if (found != null) return found;
            }
        return null;
    }

    public void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 33
                && androidx.core.content.ContextCompat.checkSelfPermission(
                                requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notifications.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        } else {
            boolean allowed =
                    androidx.core.app.NotificationManagerCompat.from(requireContext())
                            .areNotificationsEnabled();
            preferences().edit().putBoolean("nearby_alerts", allowed).apply();
            if (!allowed)
                startActivity(
                        new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(
                                        android.provider.Settings.EXTRA_APP_PACKAGE,
                                        requireContext().getPackageName()));
            else host().showScreen("2557:30760", false);
        }
    }

    private boolean insideRadiusOption(View view) {
        for (android.view.ViewParent p = view.getParent(); p instanceof View; p = p.getParent()) {
            String action = tag((View) p).optString("action").trim();
            if (action.matches("(5|10|15|20) km")) return true;
        }
        return false;
    }

    private void setTextColors(View v, int color) {
        if (v instanceof TextView) ((TextView) v).setTextColor(color);
        if (v instanceof ViewGroup)
            for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
                setTextColors(((ViewGroup) v).getChildAt(i), color);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        pendingMap = null;
        fields.clear();
        super.onDestroyView();
    }
}
