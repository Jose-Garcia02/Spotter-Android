package com.josegarcia.appgym.ui.history;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.ExerciseSet;
import com.josegarcia.appgym.data.entities.WorkoutSession;
import com.josegarcia.appgym.ui.tracker.TrackerActivity;
import com.josegarcia.appgym.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private TextView emptyView;
    private ChipGroup chipGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerHistory);
        emptyView = view.findViewById(R.id.tvEmptyHistory);
        chipGroup = view.findViewById(R.id.chipGroupHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Pass click listener
        adapter = new HistoryAdapter(new ArrayList<>(), this::showSessionDetail);
        recyclerView.setAdapter(adapter);

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipWeek) {
                loadHistoryWeek();
            } else {
                loadHistoryAll();
            }
        });

        // NEW: Swipe to delete
        new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                WorkoutSession session = adapter.getSessionAt(position);
                deleteSession(session, position);
            }
        }).attachToRecyclerView(recyclerView);

        return view;
    }

    private void deleteSession(WorkoutSession session, int position) {
        // Optimistic Remove from adapter immediately? Or wait for confirmation?
        // Usually Swipe to Delete implies "Undo" snackbar or direct delete.
        // If I delete from DB immediately, I should show Undo.
        // For simplicity and safety, let's just delete and show toast.

        // Actually, prompt just says "Swipe to Delete".
        // Let's implement delete logic.

        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(requireContext()).workoutDao().deleteSession(session);
            requireActivity().runOnUiThread(() -> {
                adapter.removeItem(position);
                android.widget.Toast.makeText(getContext(), "Sesión eliminada", android.widget.Toast.LENGTH_SHORT).show();
                if (adapter.getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void showSessionDetail(WorkoutSession session) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_session_detail, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Fixed color transparency for round corners
        }

        TextView title = dialogView.findViewById(R.id.tvDetailTitle);
        TextView content = dialogView.findViewById(R.id.tvDetailContent);
        Button btnClose = dialogView.findViewById(R.id.btnCloseDetail);
        Button btnEdit = dialogView.findViewById(R.id.btnEditSession); // This functionality remains but maybe we add Delete here instead of Menu if easier?
        // Ah, prompt says "Swipe to Delete" OR "Menu 3 dots".
        // Here we have a detail dialog. Let's add Delete button here too or rely on RecyclerView interaction.
        // For now, let's implement swipe on RecyclerView as requested.

        // Wait, prompt prefers "Swipe to Delete" on RecyclerView.
        // I will implement swipe in onViewCreated using ItemTouchHelper.

        title.setText(session.routineName + getString(R.string.details_suffix));
        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            editSession(session);
        });

        // Load details asynchronously
        // Capturamos context seguramente
        Context context = requireContext();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ExerciseSet> sets = AppDatabase.getDatabase(context).workoutDao().getSetsForSession(session.id);


            StringBuilder sb = new StringBuilder();
            if (sets.isEmpty()) {
                sb.append(getString(R.string.no_details));
            } else {
                String currentExercise = "";
                for (ExerciseSet set : sets) {
                    if (!set.exerciseName.equals(currentExercise)) {
                        currentExercise = set.exerciseName;
                        sb.append(getString(R.string.history_detail_exercise_format, currentExercise));
                    }
                    String unitLabel = set.unit != null ? set.unit : Constants.UNIT_KG;
                    if (unitLabel.equalsIgnoreCase(Constants.UNIT_PLACAS)) {
                        unitLabel = "pl";
                    } else if (unitLabel.equalsIgnoreCase(Constants.UNIT_LBS)) {
                        unitLabel = "lbs";
                    } else if (unitLabel.equalsIgnoreCase(Constants.UNIT_KG)) {
                        unitLabel = "kg";
                    }
                    sb.append(getString(R.string.history_detail_set_format, set.setOrder, set.weight, unitLabel, set.reps));
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    content.setText(sb.toString().trim());
                });
            }
        });

        dialog.show();
    }

    private void editSession(WorkoutSession session) {
        Intent intent = new Intent(getActivity(), TrackerActivity.class);
        intent.putExtra("ROUTINE_NAME", session.routineName);
        intent.putExtra("EDIT_SESSION_ID", session.id);
        intent.putExtra("EDIT_SESSION_TIMESTAMP", session.date);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chipGroup.getCheckedChipId() == R.id.chipWeek) {
            loadHistoryWeek();
        } else {
            loadHistoryAll();
        }
    }

    private void loadHistoryAll() {
        Context context = getContext();
        if (context == null) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<WorkoutSession> sessions = AppDatabase.getDatabase(context).workoutDao().getAllSessions();
            updateUi(sessions);
        });
    }

    private void loadHistoryWeek() {
        Context context = getContext();
        if (context == null) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Calendar cal = Calendar.getInstance();
            // Clear time fields to get start of day
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            // Force Monday as first day of week:
            // Calculate days to subtract to reach Monday
            // Mon(2)->0, Tue(3)->1, ... Sun(1)->6
            int currentDoW = cal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (currentDoW - Calendar.MONDAY + 7) % 7;

            cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
            long startOfWeek = cal.getTimeInMillis();

            // End of week: Start of week + 7 days (exclusive boundary for next week)
            cal.add(Calendar.DAY_OF_YEAR, 7);
            long endOfWeek = cal.getTimeInMillis();

            List<WorkoutSession> sessions = AppDatabase.getDatabase(context)
                    .workoutDao().getSessionsBetweenDates(startOfWeek, endOfWeek);
            updateUi(sessions);
        });
    }

    private void updateUi(List<WorkoutSession> sessions) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (sessions.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.updateData(sessions);
                }
            });
        }
    }
}
