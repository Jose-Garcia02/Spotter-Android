package com.josegarcia.appgym.ui.performance;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.josegarcia.appgym.R;
import java.util.List;
public class PerformanceFragment extends Fragment {
    private PerformanceViewModel viewModel;
    private Spinner spinnerExercises;
    private TextView tvComparisonHeader;
    private RecyclerView recyclerPerformanceSets;
    private PerformanceAdapter adapter;
    private String currentExerciseName;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_performance, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spinnerExercises = view.findViewById(R.id.spinnerPerformanceExercises);
        tvComparisonHeader = view.findViewById(R.id.tvComparisonHeader);
        recyclerPerformanceSets = view.findViewById(R.id.recyclerPerformanceSets);
        recyclerPerformanceSets.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PerformanceAdapter();
        recyclerPerformanceSets.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(PerformanceViewModel.class);
        setupObservers();
        viewModel.loadExercises();
    }
    private void setupObservers() {
        viewModel.getExercisesWithHistory().observe(getViewLifecycleOwner(), exercises -> {
            if (exercises == null || exercises.isEmpty()) {
                tvComparisonHeader.setText("Sin entrenamientos para comparar");
                return;
            }
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, exercises);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerExercises.setAdapter(spinnerAdapter);
            if (currentExerciseName != null) {
                int pos = spinnerAdapter.getPosition(currentExerciseName);
                if (pos >= 0) spinnerExercises.setSelection(pos);
            }
            spinnerExercises.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selected = exercises.get(position);
                    if (!selected.equals(currentExerciseName)) {
                        currentExerciseName = selected;
                        viewModel.compareCurrentVsPrevious(selected);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
            // Initial load for first item
            if (currentExerciseName == null && !exercises.isEmpty()) {
                currentExerciseName = exercises.get(0);
                viewModel.compareCurrentVsPrevious(currentExerciseName);
            }
        });
        viewModel.getComparisonHeader().observe(getViewLifecycleOwner(), header -> {
            tvComparisonHeader.setText(header);
        });
        viewModel.getComparisonResults().observe(getViewLifecycleOwner(), results -> {
            if (results == null || results.isEmpty()) {
                // Should show some empty state
                adapter.submitList(new java.util.ArrayList<>());
            } else {
                adapter.submitList(results);
            }
        });
    }
}
