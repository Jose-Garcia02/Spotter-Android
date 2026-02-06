package com.josegarcia.appgym.ui.setup.wizard;
import com.josegarcia.appgym.ui.setup.wizard.ConfigureSplitActivity;
import com.josegarcia.appgym.ui.setup.DraftManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.josegarcia.appgym.R; // Re-add R
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.Routine;
import com.josegarcia.appgym.data.entities.Split;

import java.util.ArrayList;
import java.util.List;

public class CustomSplitWizardActivity extends AppCompatActivity {

    private boolean isHybrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard_custom);

        isHybrid = getIntent().getBooleanExtra("IS_HYBRID", false);
        // Maybe update UI title if xml allows (it has hardcoded text usually?)
        // Assuming user knows what they clicked.

        EditText etDays = findViewById(R.id.etDaysCount);
        Button btnCreate = findViewById(R.id.btnCreateSplit);

        btnCreate.setOnClickListener(v -> {
            String input = etDays.getText().toString().trim();
            if (input.isEmpty()) {
                etDays.setError("Ingresa un número");
                return;
            }

            int days;
            try {
                days = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                etDays.setError("Número inválido");
                return;
            }

            if (days < 1 || days > 7) {
                etDays.setError("Entre 1 y 7 días");
                return;
            }

            // MODIFIED: Create Draft instead of DB Insert
            createDraft(days);
        });
    }

    private void createDraft(int days) {
        String type = isHybrid ? "Hybrid" : "Custom";
        String name = isHybrid ? "Rutina Híbrida" : "Rutina Personalizada";

        Split split = new Split(name, days + " días por semana", false, type);
        List<Routine> routines = new ArrayList<>();

        for (int i = 1; i <= days; i++) {
            // ID is 0, splitId is 0 (will be set on commit)
            routines.add(new Routine(0, "Día " + i, R.color.primary_accent, false, i));
        }

        DraftManager.getInstance().startDraft(split, routines);

        Intent intent = new Intent(this, ConfigureSplitActivity.class);
        intent.putExtra("IS_DRAFT", true);
        intent.putExtra("SPLIT_NAME", name); // For UI title
        startActivity(intent);
        finish();
    }

    // Removed createCustomSplit direct DB logic
}
