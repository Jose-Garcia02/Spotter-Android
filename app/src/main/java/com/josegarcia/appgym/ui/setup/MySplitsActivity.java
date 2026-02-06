package com.josegarcia.appgym.ui.setup;
import com.josegarcia.appgym.ui.setup.wizard.SetupSplitActivity;
import com.josegarcia.appgym.ui.setup.wizard.ConfigureSplitActivity;
import com.josegarcia.appgym.utils.ui.DialogHelper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.Split;

import java.util.List;

public class MySplitsActivity extends AppCompatActivity {

    private SplitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_splits); // NEW LAYOUT

        // Title is already "Mis Planes" in XML now, but keeping reference logic is fine

        // Setup Create Button (Static at bottom)
        Button btnCreate = findViewById(R.id.btnCreateNewPlan);
        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, SetupSplitActivity.class);
            intent.putExtra("IS_NEW_ROUTINE", true);
            startActivity(intent);
        });

        // Apps Window Insets
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, 0);
            // Also adjust button margin if needed? ConstraintLayout handles bottom constraint to parent.
            // But if navbar is transparent Edge-to-Edge, padding 0 might be problematic.
            // Let's apply bottom padding to the root view so button goes up.
            v.setPadding(0, insets.top, 0, insets.bottom);
            return windowInsets;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerSplits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Use custom adapter or extend functionality of SplitAdapter?
        // SplitAdapter currently only has onClick.
        // Micro-module 2.5 requirements: "Botón 'Activar' para cambiar entre Upper/Lower y PPL sin borrar el historial."
        // And "Modo Edición: Al abrir... reutilizar la interfaz del Módulo 2.3".

        // So clicking a split in "My Routines" -> opens ConfigureSplitActivity (which allows Editing and Activating).
        // ConfigureSplitActivity already has "Guardar Rutina Final" (which activates).
        // We probably want to change the text of that button if we are just editing vs activating?
        // Or keep it simple: Clicking item opens ConfigureSplitActivity.
        // Inside ConfigureSplitActivity, user can edit. And the "Activate" button can be "Guardar y Activar" or just "Activar".

        // Let's reuse SplitAdapter for now, clicking an item goes to Configure.

        loadSplits(recyclerView);
    }

    private void loadSplits(RecyclerView recyclerView) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Updated to fetch only USER splits (not templates)
            List<Split> splits = AppDatabase.getDatabase(this).splitDao().getUserSplits();
            runOnUiThread(() -> {
                adapter = new SplitAdapter(splits,
                    // On Item Click
                    split -> {
                        Intent intent = new Intent(this, ConfigureSplitActivity.class);
                        intent.putExtra("SPLIT_ID", split.id);
                        intent.putExtra("SPLIT_NAME", split.name);
                        startActivity(intent);
                    },
                    // On Delete Click (Micro-Module 4.7)
                    split -> {
                        DialogHelper.showConfirmationDialog(
                            this,
                            "Eliminar Plan",
                            "¿Estás seguro de eliminar este plan? Se perderán las rutinas asociadas.",
                            "Eliminar",
                            () -> deleteSplit(split, recyclerView)
                        );
                    }
                );
                recyclerView.setAdapter(adapter);
            });
        });
    }

    private void deleteSplit(Split split, RecyclerView recyclerView) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(this).splitDao().delete(split);
            // Reload list
            loadSplits(recyclerView);
        });
    }
}
