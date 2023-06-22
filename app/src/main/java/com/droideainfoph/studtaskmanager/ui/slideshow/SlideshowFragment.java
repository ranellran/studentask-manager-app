package com.droideainfoph.studtaskmanager.ui.slideshow;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.droideainfoph.studtaskmanager.R;
import com.droideainfoph.studtaskmanager.databinding.FragmentSlideshowBinding;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private GradeLevelAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerViewStudentList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new GradeLevelAdapter();
        recyclerView.setAdapter(adapter);

        LoadGradesTask loadGradesTask = new LoadGradesTask();
        loadGradesTask.execute();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class LoadGradesTask extends AsyncTask<Void, Void, List<GradeLevel>> {

        @Override
        protected List<GradeLevel> doInBackground(Void... voids) {
            List<GradeLevel> gradeLevels = new ArrayList<>();

            try {
                // Connect to the database
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                // Execute SQL query to fetch grade levels
                String query = "SELECT grade_level FROM teacher_dashboard_glevel_data ORDER BY grade_level + 0 ASC";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery();

                // Fetch grade levels
                while (resultSet.next()) {
                    String gradeLevelName = resultSet.getString("grade_level");
                    GradeLevel gradeLevel = new GradeLevel(gradeLevelName);

                    // Fetch students for the grade level
                    String studentsQuery = "SELECT username, firstname, lastname FROM studtask_user_student WHERE grade_level = ? ORDER BY firstname ASC";
                    PreparedStatement studentsStatement = connection.prepareStatement(studentsQuery);
                    studentsStatement.setString(1, gradeLevelName);
                    ResultSet studentsResultSet = studentsStatement.executeQuery();

                    List<Student> students = new ArrayList<>();
                    while (studentsResultSet.next()) {
                        String studentId = studentsResultSet.getString("username");
                        String firstName = studentsResultSet.getString("firstname");
                        String lastName = studentsResultSet.getString("lastname");
                        Student student = new Student(studentId, firstName, lastName);
                        students.add(student);
                    }

                    studentsResultSet.close();
                    studentsStatement.close();

                    gradeLevel.setStudents(students);
                    gradeLevels.add(gradeLevel);
                }

                // Close resources
                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return gradeLevels;
        }

        @Override
        protected void onPostExecute(List<GradeLevel> gradeLevels) {
            adapter.setGradeLevels(gradeLevels);
        }
    }


    private class GradeLevel {
        private String name;
        private List<Student> students;

        public GradeLevel(String name) {
            this.name = name;
            this.students = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public List<Student> getStudents() {
            return students;
        }

        public void setStudents(List<Student> students) {
            this.students = students;
        }
    }

    private class Student {
        private String id;
        private String firstName;
        private String lastName;

        public Student(String id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getId() {
            return id;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }

    private class GradeLevelAdapter extends RecyclerView.Adapter<GradeLevelAdapter.GradeLevelViewHolder> {

        private List<GradeLevel> gradeLevels;

        public void setGradeLevels(List<GradeLevel> gradeLevels) {
            this.gradeLevels = gradeLevels;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public GradeLevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grade_level, parent, false);
            return new GradeLevelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GradeLevelViewHolder holder, int position) {
            GradeLevel gradeLevel = gradeLevels.get(position);
            holder.bind(gradeLevel);
        }

        @Override
        public int getItemCount() {
            return gradeLevels != null ? gradeLevels.size() : 0;
        }

        class GradeLevelViewHolder extends RecyclerView.ViewHolder {
            private TextView gradeLevelTextView;
            private RecyclerView studentsRecyclerView;

            public GradeLevelViewHolder(@NonNull View itemView) {
                super(itemView);
                gradeLevelTextView = itemView.findViewById(R.id.gradeLevelTextView);
                studentsRecyclerView = itemView.findViewById(R.id.studentsRecyclerView);
            }

            public void bind(GradeLevel gradeLevel) {
                gradeLevelTextView.setText(gradeLevel.getName());

                StudentAdapter studentAdapter = new StudentAdapter();
                studentsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                studentsRecyclerView.setAdapter(studentAdapter);
                studentAdapter.setStudents(gradeLevel.getStudents());

                gradeLevelTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (studentsRecyclerView.getVisibility() == View.VISIBLE) {
                            studentsRecyclerView.setVisibility(View.GONE);
                        } else {
                            studentsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }
    }

    private class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

        private List<Student> students;

        public void setStudents(List<Student> students) {
            this.students = students;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_students_view_get, parent, false);
            return new StudentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            Student student = students.get(position);
            holder.bind(student);
        }

        @Override
        public int getItemCount() {
            return students != null ? students.size() : 0;
        }

        class StudentViewHolder extends RecyclerView.ViewHolder {
            private TextView studentTextView;

            public StudentViewHolder(@NonNull View itemView) {
                super(itemView);
                studentTextView = itemView.findViewById(R.id.studentNameTextViewGet);
            }

            public void bind(Student student) {
                String fullName = student.getFirstName() + " " + student.getLastName();
                studentTextView.setText(fullName);
            }
        }
    }
}
