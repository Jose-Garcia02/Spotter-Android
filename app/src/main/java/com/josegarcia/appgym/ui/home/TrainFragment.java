package com.josegarcia.appgym.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.Routine;
import com.josegarcia.appgym.ui.tracker.TrackerActivity;
import com.josegarcia.appgym.utils.Constants;

public class TrainFragment extends Fragment {

    private RoutineAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_train, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerRoutines);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new RoutineAdapter(this::onRoutineClicked);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.fab_train_add).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.josegarcia.appgym.ui.tracker.ExerciseSelectionActivity.class);
            startActivity(intent);
        });

        HomeViewModel viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class); // Share ViewModel with Activity scope or get new one? Shared logic is fine.

        viewModel.getActiveRoutines().observe(getViewLifecycleOwner(), routines -> {
            adapter.setRoutines(routines);
        });

        return view;
    }

    private void onRoutineClicked(Routine routine) {
        if (Constants.ROUTINE_FREE.equals(routine.name)) {
            Intent intent = new Intent(getActivity(), com.josegarcia.appgym.ui.tracker.ExerciseSelectionActivity.class);
            // Don't set IS_SELECTION_MODE, so it defaults to starting TrackerActivity
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), TrackerActivity.class);
            intent.putExtra("ROUTINE_NAME", routine.name);
            intent.putExtra("ROUTINE_ID", routine.id);
            startActivity(intent);
        }
    }
}
