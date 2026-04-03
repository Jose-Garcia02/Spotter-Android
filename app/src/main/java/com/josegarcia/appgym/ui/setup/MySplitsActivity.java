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

        // Inicializar adapter vacío para evitar pantalla negra
        adapter = new SplitAdapter(new java.util.ArrayList<>(), split -> {}, split -> {});
        recyclerSplits.setAdapter(adapter);

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
            // Retry logic: esperar y reintentar si la BD aún no tiene datos (first load)
            List<Split> splits = AppDatabase.getDatabase(this).splitDao().getUserSplits();

            int retries = 0;
            int maxRetries = 10; // Intentar hasta 5 segundos

            while (splits.isEmpty() && retries < maxRetries) {
                try {
                    Thread.sleep(500); // Esperar 500ms antes de reintentar
                    splits = AppDatabase.getDatabase(this).splitDao().getUserSplits();
                    retries++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            final List<Split> finalSplits = splits;
            runOnUiThread(() -> {
                adapter = new SplitAdapter(finalSplits,
                    // On Item Click
                    split -> {
                        Intent intent = new Intent(this, ConfigureSplitActivity.class);
                        intent.putExtra("SPLIT_ID", split.id);
                        intent.putExtra("SPLIT_NAME", split.name);
                        startActivity(intent);
                    },
                    // On Delete Click
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
