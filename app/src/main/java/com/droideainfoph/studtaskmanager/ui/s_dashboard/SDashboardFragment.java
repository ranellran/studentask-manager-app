package com.droideainfoph.studtaskmanager.ui.s_dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.droideainfoph.studtaskmanager.ChatNavigationSelector;
import com.droideainfoph.studtaskmanager.StudentSubjectEnrolledShow;
import com.droideainfoph.studtaskmanager.databinding.FragmentSDashboardBinding;

public class SDashboardFragment extends Fragment {

    private FragmentSDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();





        binding.quarterGradesCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open GradePortalStudent activity here
                Intent intent = new Intent(getActivity(), StudentSubjectEnrolledShow.class);
                startActivity(intent);
            }
        });

        binding.attendanceScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Under development", Toast.LENGTH_SHORT).show();
            }
        });

        binding.studentMaterialCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Under development", Toast.LENGTH_SHORT).show();
            }
        });

        binding.announcementCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Under development", Toast.LENGTH_SHORT).show();
            }
        });

        binding.sendMessageCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ChatNavigationSelector.class);
                startActivity(intent);
            }
        });


        binding.taskManagerCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Under development", Toast.LENGTH_SHORT).show();
            }
        });






        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
