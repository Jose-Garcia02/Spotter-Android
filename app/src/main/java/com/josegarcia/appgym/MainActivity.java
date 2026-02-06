package com.josegarcia.appgym;

import android.os.Bundle;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.ui.setup.wizard.SetupSplitActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_SETUP_COMPLETED = "is_setup_completed";
    private static final String PREF_THEME = "pref_theme"; // Match with ViewModel
    public static final int MODE_AMOLED = 100; // Match with ViewModel

    // region Métodos de la Actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Cargar preferencias antes de crear la vista
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Default to MODE_NIGHT_YES (Oscuro) si no hay preferencia guardada.
        // Esto corrige el bug de "Luz por defecto" visualmente.
        int themeMode = prefs.getInt(PREF_THEME, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);

        int delegateMode;
        if (themeMode == MODE_AMOLED) {
            setTheme(R.style.Theme_AppGym_Amoled);
            delegateMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            setTheme(R.style.Theme_AppGym);
            delegateMode = themeMode;
        }

        // Usar setLocalNightMode para esta instancia, y setDefaultNightMode para la app globalmente.
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(delegateMode);
        getDelegate().setLocalNightMode(delegateMode);

        super.onCreate(savedInstanceState);

        // 1. Carga inicial de datos...
        AppDatabase.getDatabase(getApplicationContext());

        // 2. Verificación de Onboarding
        boolean isSetupCompleted = prefs.getBoolean(KEY_SETUP_COMPLETED, false);

        if (!isSetupCompleted) {
            startActivity(new android.content.Intent(this, SetupSplitActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            setupNavigation();
        }

        // FAB lógica eliminada para evitar duplicidad con el botón del fragmento de entrenamiento.
    }
    // endregion

    // region Configuración de UI
    private void setupNavigation() {
        // Manejar insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, 0);
            return windowInsets;
        });

        BottomNavigationView navView = findViewById(R.id.nav_view);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navView, navController);

            // Listener de destino simplificado (sin FAB)
        }
    }
    // endregion
}

