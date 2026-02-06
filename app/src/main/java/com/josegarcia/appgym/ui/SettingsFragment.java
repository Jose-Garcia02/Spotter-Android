package com.josegarcia.appgym.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.ui.setup.MySplitsActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private ActivityResultLauncher<String> createDocumentLauncher;
    private ActivityResultLauncher<String[]> openDocumentLauncher;
    private ActivityResultLauncher<String> createWeightDocumentLauncher;
    private ActivityResultLauncher<String[]> openWeightDocumentLauncher;

    private SettingsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(SettingsViewModel.class);

        // Observers
        viewModel.getStatusMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        createDocumentLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("text/csv"), uri -> {
            if (uri != null) {
                viewModel.exportHistory(requireContext(), uri);
            }
        });

        // Use more permissive MIME type filter to fix local file selection issues
        openDocumentLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                viewModel.importHistory(requireContext(), uri);
            }
        });

        createWeightDocumentLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("text/csv"), uri -> {
            if (uri != null) {
                viewModel.exportBodyWeight(requireContext(), uri);
            }
        });

        openWeightDocumentLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                viewModel.importBodyWeight(requireContext(), uri);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_export_csv).setOnClickListener(v -> {
            String fileName = "gym_history_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".csv";
            createDocumentLauncher.launch(fileName);
        });

        view.findViewById(R.id.btn_import_csv).setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"*/*"}));

        view.findViewById(R.id.btn_export_weight_csv).setOnClickListener(v -> {
            String fileName = "body_weight_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".csv";
            createWeightDocumentLauncher.launch(fileName);
        });

        view.findViewById(R.id.btn_import_weight_csv).setOnClickListener(v -> openWeightDocumentLauncher.launch(new String[]{"*/*"}));

        // New Button for Split Selection
        view.findViewById(R.id.btn_change_split).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireContext(), MySplitsActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.btn_help_csv).setOnClickListener(v -> showHelpDialog());

        // Theme Handling
        setupThemeSelector(view);
    }

    private void setupThemeSelector(View view) {
        View itemDark = view.findViewById(R.id.itemThemeDark);
        View itemLight = view.findViewById(R.id.itemThemeLight);
        View itemAmoled = view.findViewById(R.id.itemThemeAmoled);

        // Setup Labels & Icons
        setupThemeItem(itemDark, R.drawable.ic_theme_dark, R.string.theme_dark);
        setupThemeItem(itemLight, R.drawable.ic_theme_light, R.string.theme_light);
        setupThemeItem(itemAmoled, R.drawable.ic_theme_amoled, R.string.theme_amoled);

        int currentInfo = viewModel.getSavedTheme(requireContext());
        updateThemeSelection(currentInfo, itemDark, itemLight, itemAmoled);

        itemDark.setOnClickListener(v -> applyTheme(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES));
        itemLight.setOnClickListener(v -> applyTheme(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO));
        itemAmoled.setOnClickListener(v -> applyTheme(SettingsViewModel.MODE_AMOLED));
    }

    private void setupThemeItem(View includeView, int iconRes, int labelRes) {
        android.widget.ImageView icon = includeView.findViewById(R.id.imgThemeIcon);
        android.widget.TextView label = includeView.findViewById(R.id.tvThemeLabel);
        icon.setImageResource(iconRes);
        label.setText(labelRes);
    }

    private void updateThemeSelection(int mode, View dark, View light, View amoled) {
        // Lógica explicita:
        // YES (2) -> Dark
        // NO (1) -> Light
        // AMOLED (100) -> Amoled
        // Else/Default -> Dark

        boolean isAmoled = mode == SettingsViewModel.MODE_AMOLED;
        boolean isLight = mode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
        boolean isDark = !isLight && !isAmoled; // Incluye MODE_NIGHT_YES y cualquier otro fallback

        // Depuración visual
        // Log.d("SettingsFragment", "updateThemeSelection: mode=" + mode + ", isLight=" + isLight + ", isDark=" + isDark);

        setThemeSelected(dark, isDark);
        setThemeSelected(light, isLight);
        setThemeSelected(amoled, isAmoled);
    }

    private void setThemeSelected(View view, boolean selected) {
        android.widget.ImageView icon = view.findViewById(R.id.imgThemeIcon);
        icon.setSelected(selected);
    }

    private void applyTheme(int mode) {
        int currentMode = viewModel.getSavedTheme(requireContext());
        if (currentMode == mode) return; // No hacer nada si ya está seleccionado

        viewModel.setTheme(requireContext(), mode);

        // Recreación suave para evitar parpadeos (Flash negro/blanco)
        // en lugar de recreate(), finalizamos e iniciamos sin animación brusca.
        requireActivity().finish();
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(requireActivity().getIntent());
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showHelpDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_csv_help, null);
        builder.setView(view);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        android.widget.TextView title = view.findViewById(R.id.tvDialogTitle);
        android.widget.TextView message = view.findViewById(R.id.tvDialogMessage);
        android.widget.Button btnOk = view.findViewById(R.id.btnOk);

        title.setText(R.string.csv_help_title);
        message.setText(R.string.csv_help_message);

        btnOk.setText(R.string.understood);
        btnOk.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
