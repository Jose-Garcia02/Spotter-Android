package com.josegarcia.appgym.ui.setup.wizard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.josegarcia.appgym.R;

public class SetupSplitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard_home);

        // Check if New Routine Mode
        boolean isNewRoutine = getIntent().getBooleanExtra("IS_NEW_ROUTINE", false);
        View btnClose = findViewById(R.id.btnCloseWizard);
        if (isNewRoutine) {
            btnClose.setVisibility(View.VISIBLE);
            btnClose.setOnClickListener(v -> finish());
        }

        // Apply Window Insets
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, insets.bottom);
            return windowInsets;
        });

        findViewById(R.id.btnOptionClassic).setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassicSplitListActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnOptionHybrid).setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomSplitWizardActivity.class);
            intent.putExtra("IS_HYBRID", true);
            startActivity(intent);
        });

        findViewById(R.id.btnOptionCustom).setOnClickListener(v -> {
             Intent intent = new Intent(this, CustomSplitWizardActivity.class);
             intent.putExtra("IS_HYBRID", false);
             startActivity(intent);
        });
    }
}
