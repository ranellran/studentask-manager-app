package com.droideainfoph.studtaskmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TeacherStudentSelectorActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private List<Student> selectedStudents;


    Button cancelOperation;

    String gradeLevelBelongID;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_student_selector);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cancelOperation = findViewById(R.id.student_selector_cancel_btn);

        selectedStudents = new ArrayList<>();



        SearchGradeLevelTask searchGradeLevelTask = new SearchGradeLevelTask();
        searchGradeLevelTask.execute();

        cancelOperation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Perform the back button action
                onBackPressed();
                finish();
            }
        });




        findViewById(R.id.student_selector_proceed_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedStudents.isEmpty()) {
                    Toast.makeText(TeacherStudentSelectorActivity.this, "No students selected", Toast.LENGTH_SHORT).show();
                } else {
                    RetrieveLRNTask retrieveLRNTask = new RetrieveLRNTask();
                    retrieveLRNTask.execute(selectedStudents);
                }
            }
        });
    }








    private class SearchGradeLevelTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String gradeLevel = "";

            // Perform your MySQL database operation here
            // Replace the following code with your actual implementation

            // Assuming you have a MySQL database connection and the necessary query
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            Intent intent = getIntent();
            int idG = intent.getIntExtra("id", 0); // The second parameter is the default value if the key is not found
            String nextActivityGradeLevelID = String.valueOf(idG);

            try {
                // Establish the database connection
                conn = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                // Prepare the query
                String query = "SELECT grade_level FROM teacher_dashboard_glevel_data WHERE id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, Integer.parseInt(nextActivityGradeLevelID));

                // Execute the query
                rs = stmt.executeQuery();

                // Process the result
                if (rs.next()) {
                    gradeLevel = rs.getString("grade_level");
                    if (gradeLevel == null) {
                        gradeLevel = ""; // Assign an empty string if the grade level is null
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Close the database resources
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return gradeLevel;
        }

        @Override
        protected void onPostExecute(String gradeLevel) {
            if (gradeLevel != null) {
                LoadStudentsTask loadStudentsTask = new LoadStudentsTask();
                loadStudentsTask.execute(gradeLevel);
            } else {
                Toast.makeText(TeacherStudentSelectorActivity.this, "Failed to retrieve grade level", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadStudentsTask extends AsyncTask<String, Void, List<Student>> {
        @Override
        protected List<Student> doInBackground(String... params) {
            String gradeLevel = params[0];
            List<Student> students = new ArrayList<>();
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;

            try {
                connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));
                statement = connection.createStatement();
                resultSet = statement.executeQuery("SELECT lastname, firstname FROM studtask_user_student WHERE grade_level = '" + gradeLevel + "' ORDER BY lastname ASC");

                while (resultSet.next()) {
                    String lastName = resultSet.getString("lastname");
                    String firstName = resultSet.getString("firstname");

                    Student student = new Student(lastName, firstName);
                    students.add(student);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null)
                        resultSet.close();
                    if (statement != null)
                        statement.close();
                    if (connection != null)
                        connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return students;
        }

        @Override
        protected void onPostExecute(List<Student> students) {
            if (students != null) {
                studentAdapter = new StudentAdapter(students);
                recyclerView.setAdapter(studentAdapter);
            } else {
                Toast.makeText(TeacherStudentSelectorActivity.this, "Failed to load students", Toast.LENGTH_SHORT).show();
            }
        }
    }
















    private static class Student {
        private String lastName;
        private String firstName;

        public Student(String lastName, String firstName) {
            this.lastName = lastName;
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getFirstName() {
            return firstName;
        }
    }

    private class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

        private List<Student> students;

        public StudentAdapter(List<Student> students) {
            this.students = students;
            selectAllStudents(); // Call the method to select all students by default
        }

        private void selectAllStudents() {
            selectedStudents.addAll(students); // Add all students to the selectedStudents list
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_students_display_format, parent, false);
            return new StudentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            final Student student = students.get(position);
            String lastName = capitalize(student.getLastName());
            String firstName = capitalize(student.getFirstName());
            String fullName = lastName + ", " + firstName;
            holder.studentNameTextView.setText(fullName);
            holder.studentCheckBox.setChecked(true); // Set the checkbox as checked by default

            holder.studentCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        selectedStudents.add(student);
                    } else {
                        selectedStudents.remove(student);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return students.size();
        }

        public class StudentViewHolder extends RecyclerView.ViewHolder {
            private TextView studentNameTextView;
            private CheckBox studentCheckBox;

            public StudentViewHolder(@NonNull View itemView) {
                super(itemView);
                studentNameTextView = itemView.findViewById(R.id.studentNameTextView);
                studentCheckBox = itemView.findViewById(R.id.studentCheckBox);
            }
        }

        private String capitalize(String input) {
            return TextUtils.isEmpty(input) ? "" : input.substring(0, 1).toUpperCase() + input.substring(1);
        }
    }





    private class RetrieveLRNTask extends AsyncTask<List<Student>, Void, List<String>> {

        @Override
        protected List<String> doInBackground(List<Student>... studentLists) {
            List<Student> students = studentLists[0];
            List<String> lrnList = new ArrayList<>();

            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;

            try {
                connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));
                statement = connection.createStatement();

                for (Student student : students) {
                    String lastName = student.getLastName();
                    String firstName = student.getFirstName();

                    String query = "SELECT lrn FROM studtask_user_student WHERE lastname = '" + lastName + "' AND firstname = '" + firstName + "'";
                    resultSet = statement.executeQuery(query);

                    if (resultSet.next()) {
                        String lrn = resultSet.getString("lrn");
                        lrnList.add(lrn);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null)
                        resultSet.close();
                    if (statement != null)
                        statement.close();
                    if (connection != null)
                        connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return lrnList;
        }

        @Override
        protected void onPostExecute(List<String> lrnList) {
            if (lrnList != null && !lrnList.isEmpty()) {

                // Retrieve the values from the Intent
                int gradeLevelIDBelong = getIntent().getIntExtra("id", 0); // The second parameter is the default value if the key is not found
                String gradeLevelBelong = getIntent().getStringExtra("gradeLevel");
                String dateCreatedBelong = getIntent().getStringExtra("dateCreated");


                // Start the next activity and pass the LRN list using Intent
                Intent intent = new Intent(TeacherStudentSelectorActivity.this, AddSubjectActivity.class);

                intent.putStringArrayListExtra("lrnList", new ArrayList<>(lrnList));



                if (lrnList != null && !lrnList.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    for (String lrn : lrnList) {
                        builder.append(lrn).append("\n");
                    }
                    String lrnText = builder.toString();

                } else {

                }


                intent.putExtra("id", gradeLevelIDBelong);
                intent.putExtra("gradeLevel", gradeLevelBelong);
                intent.putExtra("dateCreated", dateCreatedBelong);
                startActivity(intent);


            } else {
                Toast.makeText(TeacherStudentSelectorActivity.this, "Failed to retrieve LRN", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
