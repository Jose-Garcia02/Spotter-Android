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
    private Spinner spinnerRoutines;
    private TextView tvComparisonHeader;
    private RecyclerView recyclerPerformanceSets;
    private PerformanceAdapter adapter;
    private String currentRoutineName;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_performance, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spinnerRoutines = view.findViewById(R.id.spinnerPerformanceExercises);
        tvComparisonHeader = view.findViewById(R.id.tvComparisonHeader);
        recyclerPerformanceSets = view.findViewById(R.id.recyclerPerformanceSets);
        recyclerPerformanceSets.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PerformanceAdapter();
        recyclerPerformanceSets.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(PerformanceViewModel.class);
        setupObservers();
        viewModel.loadRoutines();
    }
    private void setupObservers() {
        viewModel.getRoutinesWithHistory().observe(getViewLifecycleOwner(), routines -> {
            if (routines == null || routines.isEmpty()) {
                tvComparisonHeader.setText("Sin rutinas para comparar.");
                return;
            }
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, routines);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRoutines.setAdapter(spinnerAdapter);
            if (currentRoutineName != null) {
                int pos = spinnerAdapter.getPosition(currentRoutineName);
                if (pos >= 0) spinnerRoutines.setSelection(pos);
            }
            spinnerRoutines.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selected = routines.get(position);
                    if (!selected.equals(currentRoutineName)) {
                        currentRoutineName = selected;
                        viewModel.compareCurrentVsPrevious(selected);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
            // Initial load for first item
            if (currentRoutineName == null && !routines.isEmpty()) {
                currentRoutineName = routines.get(0);
                viewModel.compareCurrentVsPrevious(currentRoutineName);
            }
        });
        viewModel.getComparisonHeader().observe(getViewLifecycleOwner(), header -> {
            tvComparisonHeader.setText(header);
        });
        viewModel.getComparisonResults().observe(getViewLifecycleOwner(), results -> {
            if (results == null || results.isEmpty()) {
                adapter.submitList(new java.util.ArrayList<>());
            } else {
                adapter.submitList(results);
            }
        });
    }
}
