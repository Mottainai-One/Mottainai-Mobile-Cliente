package com.mottainai.cliente.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.cliente.R;
import com.mottainai.cliente.adapters.PointsActivityAdapter;
import com.mottainai.cliente.models.UserProfile;
import com.mottainai.cliente.repository.MockPointsActivityRepository;
import com.mottainai.cliente.repository.MockUserRepository;

import java.util.Locale;

public class PointsFragment extends Fragment {

    private final MockUserRepository userRepository = new MockUserRepository();
    private final MockPointsActivityRepository activityRepository = new MockPointsActivityRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_points, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserProfile user = userRepository.getCurrentUser();

        TextView balanceLabel = view.findViewById(R.id.tv_points_balance);
        balanceLabel.setText(String.format(Locale.forLanguageTag("pt-BR"), "%,d pts", user.getPointsBalance()));

        TextView levelLabel = view.findViewById(R.id.tv_points_level);
        levelLabel.setText(user.getLevelLabel() + " · " + user.getLevelSubLabel());

        ProgressBar progressBar = view.findViewById(R.id.progress_level);
        progressBar.setProgress(user.getLevelProgressPercent());

        RecyclerView recyclerView = view.findViewById(R.id.rv_activity);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        PointsActivityAdapter adapter = new PointsActivityAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setEntries(activityRepository.getRecentActivity());

        view.findViewById(R.id.btn_see_impact).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_pointsFragment_to_impactFragment));
    }
}
