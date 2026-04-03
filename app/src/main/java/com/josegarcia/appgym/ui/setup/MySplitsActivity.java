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

    private RecyclerView recyclerSplits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_splits); // NEW LAYOUT

        // ...existing code...

        recyclerSplits = findViewById(R.id.recyclerSplits);
        recyclerSplits.setLayoutManager(new LinearLayoutManager(this));

        // ...existing code...

        loadSplits(recyclerSplits);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar splits cuando volvemos de ConfigureSplitActivity
        if (recyclerSplits != null) {
            loadSplits(recyclerSplits);
        }
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
