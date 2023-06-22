package com.droideainfoph.studtaskmanager.ui.t_dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.droideainfoph.studtaskmanager.ChatNavigationSelector;
import com.droideainfoph.studtaskmanager.LogInTeacher;
import com.droideainfoph.studtaskmanager.TeacherDashboard;
import com.droideainfoph.studtaskmanager.TeacherSubjectView;
import com.droideainfoph.studtaskmanager.databinding.FragmentTDashboardBinding;

public class TDashboardFragment extends Fragment {

    private FragmentTDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentTDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();




        binding.quarterGradesCardViewS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open GradePortalStudent activity here
                Intent intent = new Intent(getActivity(), TeacherDashboard.class);
                startActivity(intent);
            }
        });

        binding.teacherAttendanceScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Under development", Toast.LENGTH_SHORT).show();
            }
        });

        binding.teacherTaskManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Under development", Toast.LENGTH_SHORT).show();
            }
        });

        binding.teacherAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Under development", Toast.LENGTH_SHORT).show();
            }
        });


        binding.teacherSendMessage.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChatNavigationSelector.class);
            startActivity(intent);
        });

        binding.teacherStudentMaterials.setOnClickListener(new View.OnClickListener() {
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