package com.josegarcia.appgym.ui;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.josegarcia.appgym.data.CsvImporter;
import com.josegarcia.appgym.data.database.AppDatabase;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String PREF_THEME = "pref_theme";
    public static final int MODE_AMOLED = 100;

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }


    public void exportHistory(Context context, Uri uri) {
        isLoading.setValue(true);
        postMessage(context.getString(com.josegarcia.appgym.R.string.export_start));

        executor.execute(() -> {
            try {
                OutputStream os = context.getContentResolver().openOutputStream(uri);
                if (os != null) {
                    CsvImporter.exportDatabaseToCsv(context, os);
                    postMessage(context.getString(com.josegarcia.appgym.R.string.export_history_success));
                } else {
                    postMessage(context.getString(com.josegarcia.appgym.R.string.export_error));
                }
            } catch (Exception e) {
                postMessage(context.getString(com.josegarcia.appgym.R.string.export_fail, e.getMessage()));
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void importHistory(Context context, Uri uri) {
        isLoading.setValue(true);
        postMessage(context.getString(com.josegarcia.appgym.R.string.import_start));

        AppDatabase db = AppDatabase.getDatabase(context);
        executor.execute(() -> {
            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                if (is != null) {
                    CsvImporter.importFromStream(is, db.workoutDao());
                    postMessage(context.getString(com.josegarcia.appgym.R.string.import_history_success));
                } else {
                    postMessage(context.getString(com.josegarcia.appgym.R.string.import_open_error));
                }
            } catch (Exception e) {
                postMessage(context.getString(com.josegarcia.appgym.R.string.import_fail, e.getMessage()));
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void exportBodyWeight(Context context, Uri uri) {
        isLoading.setValue(true);
        postMessage(context.getString(com.josegarcia.appgym.R.string.export_weight_start));

        executor.execute(() -> {
            try {
                OutputStream os = context.getContentResolver().openOutputStream(uri);
                if (os != null) {
                    CsvImporter.exportBodyWeightToCsv(context, os);
                    postMessage(context.getString(com.josegarcia.appgym.R.string.export_weight_success));
                } else {
                    postMessage(context.getString(com.josegarcia.appgym.R.string.export_error));
                }
            } catch (Exception e) {
                postMessage(context.getString(com.josegarcia.appgym.R.string.export_fail, e.getMessage()));
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void importBodyWeight(Context context, Uri uri) {
        isLoading.setValue(true);
        postMessage(context.getString(com.josegarcia.appgym.R.string.import_start));

        AppDatabase db = AppDatabase.getDatabase(context);
        executor.execute(() -> {
            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                if (is != null) {
                    CsvImporter.importBodyWeightFromStream(is, db.bodyWeightDao());
                    postMessage(context.getString(com.josegarcia.appgym.R.string.import_weight_success));
                } else {
                    postMessage(context.getString(com.josegarcia.appgym.R.string.import_open_error));
                }
            } catch (Exception e) {
                postMessage(context.getString(com.josegarcia.appgym.R.string.import_fail, e.getMessage()));
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    private void postMessage(String message) {
        statusMessage.postValue(message);
    }

    public void setTheme(Context context, int mode) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_THEME, mode).apply();

        int delegateMode;
        if (mode == MODE_AMOLED) {
            delegateMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            delegateMode = mode;
        }

        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(delegateMode);
    }

    public int getSavedTheme(Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return prefs.getInt(PREF_THEME, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
    }
}
