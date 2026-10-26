package com.mottainai.cliente;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.mottainai.cliente.design.DesignRepository;
import com.mottainai.cliente.fragments.DesignScreenFragment;

/** One app and navigation history, with persistent light/dark appearance. */
public class MainActivity extends AppCompatActivity {
    private static final String PREFS="cliente_preferences";
    private boolean artworkFullBleed;
    @Override protected void onCreate(Bundle state) {
        AppCompatDelegate.setDefaultNightMode(getSharedPreferences(PREFS,MODE_PRIVATE).getInt("theme",AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
        super.onCreate(state);
        if(!isDesignPreview()) {
            android.content.SharedPreferences prefs=getSharedPreferences(PREFS,MODE_PRIVATE);
            if(!prefs.getBoolean("fixtures_removed_v1",false)) {
                android.content.SharedPreferences.Editor editor=prefs.edit().putBoolean("fixtures_removed_v1",true);
                for(String key:new String[]{"field_Nome completo","field_E-mail","field_Telefone"}) {
                    String value=prefs.getString(key,"");
                    if(value.equals("Camila Oliveira")||value.equals("camila@exemplo.com")||value.equals("(11) 90000-0000"))editor.remove(key);
                }
                editor.apply();
            }
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        boolean dark=(getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
        WindowCompat.getInsetsController(getWindow(),getWindow().getDecorView()).setAppearanceLightStatusBars(!dark);
        WindowCompat.getInsetsController(getWindow(),getWindow().getDecorView()).setAppearanceLightNavigationBars(!dark);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.design_content),(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars()|WindowInsetsCompat.Type.displayCutout());
            Insets keyboard=insets.getInsets(WindowInsetsCompat.Type.ime());
            if(artworkFullBleed)v.setPadding(bars.left,0,bars.right,keyboard.bottom);
            else v.setPadding(bars.left,bars.top,bars.right,Math.max(bars.bottom,keyboard.bottom));
            return insets;
        });
        if(state==null) showScreen(getIntent().getStringExtra("screen")!=null?getIntent().getStringExtra("screen"):
                (getSharedPreferences(PREFS,MODE_PRIVATE).getBoolean("demo_session",false)?"2005:3812":(getSharedPreferences(PREFS,MODE_PRIVATE).getBoolean("introduction_seen",false)?"2028:4492":"2542:29599")),false);
    }
    public void showScreen(String id, boolean history) {
        DesignRepository repository=DesignRepository.get(this);
        if(!isDesignPreview()&&repository.canonical(id).equals("2557:30792")) {
            if(currentScreen() instanceof DesignScreenFragment){((DesignScreenFragment)currentScreen()).requestNotificationPermission();return;}
            id="2557:30760";
        }
        if(!repository.available(id)) return;
        android.view.View focused=getCurrentFocus();
        if(focused!=null) {
            android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(focused.getWindowToken(),0);
        }
        String canonical=repository.canonical(id);
        androidx.fragment.app.FragmentManager manager=getSupportFragmentManager();
        if(manager.findFragmentById(R.id.design_overlay)!=null)manager.popBackStackImmediate("overlay",androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        boolean overlay=repository.screen(canonical).optBoolean("overlay");
        androidx.fragment.app.FragmentTransaction transaction=manager.beginTransaction().setReorderingAllowed(true);
        if(overlay) {
            if(manager.findFragmentById(R.id.design_content)==null) {
                String base=overlayBase(canonical);
                transaction.replace(R.id.design_content,DesignScreenFragment.create(base),base);
            }
            transaction.add(R.id.design_overlay,DesignScreenFragment.create(canonical),canonical).addToBackStack("overlay");
        } else {
            transaction.replace(R.id.design_content,DesignScreenFragment.create(canonical),canonical);
            if(history)transaction.addToBackStack(canonical);
        }
        transaction.commit();
    }
    public androidx.fragment.app.Fragment currentScreen() {
        androidx.fragment.app.Fragment overlay=getSupportFragmentManager().findFragmentById(R.id.design_overlay);
        return overlay!=null?overlay:getSupportFragmentManager().findFragmentById(R.id.design_content);
    }
    public boolean isDesignPreview() { return BuildConfig.DEBUG&&getIntent().getBooleanExtra("design_preview",false); }
    private String overlayBase(String id) {
        String name=DesignRepository.get(this).screen(id).optString("name");
        if(name.contains("Foto do perfil"))return "2022:4219";
        if(name.contains("Fidelidade"))return "2093:6331";
        if(name.contains("Sair da conta"))return "2005:4483";
        if(name.contains("Filtros"))return "2005:3914";
        if(name.contains("Conversa"))return "2055:5379";
        if(name.contains("Cadastro"))return "2143:9538";
        return "2005:3812";
    }
    public void setAppearance(boolean dark) {
        int mode=dark?AppCompatDelegate.MODE_NIGHT_YES:AppCompatDelegate.MODE_NIGHT_NO;
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().putInt("theme",mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }
    public void setArtworkFullBleed(boolean fullBleed) {
        artworkFullBleed=fullBleed;
        if(fullBleed)findViewById(R.id.design_content).setPadding(0,0,0,0);
        ViewCompat.requestApplyInsets(findViewById(R.id.design_content));
    }
    public void setDemoSession(boolean active) {
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean("demo_session",active).apply();
    }
}
