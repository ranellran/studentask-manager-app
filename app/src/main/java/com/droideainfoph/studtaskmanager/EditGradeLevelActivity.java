package com.droideainfoph.studtaskmanager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.droideainfoph.studtaskmanager.R;
import com.droideainfoph.studtaskmanager.TeacherDashboard;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EditGradeLevelActivity extends AppCompatActivity {
    private EditText gradeLevelEditText;
    private EditText sectionEditText;
    private EditText adviserNameEditText;
    private EditText salutationEditText;
    private Button updateButton;
    private Button deleteButton;

    private int gradeLevelId;

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;


    // Declare global variables

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_grade_level);

        // Get the grade level ID passed from the previous activity
        gradeLevelId = getIntent().getIntExtra("gradeLevelId", 1);

        // Get references to the UI elements
        gradeLevelEditText = findViewById(R.id.gradeLevelEditText);
        sectionEditText = findViewById(R.id.sectionEditText);
        adviserNameEditText = findViewById(R.id.adviserNameEditText);
        salutationEditText = findViewById(R.id.salutationEditText);
        updateButton = findViewById(R.id.editGLevelsaveButton);
        deleteButton = findViewById(R.id.editGLevelcancel_button);



// Connect to the MySQL database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

// Load the existing data for the selected grade level
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM teacher_dashboard_glevel_data WHERE id = " + gradeLevelId);

            if (resultSet.next()) {
                gradeLevelEditText.setText(resultSet.getString("grade_level"));
                sectionEditText.setText(resultSet.getString("section"));
                adviserNameEditText.setText(resultSet.getString("adviser_name"));
                salutationEditText.setText(resultSet.getString("salutation"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        // Set a click listener for the Update button
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the updated data from the UI elements
                String gradeLevel = gradeLevelEditText.getText().toString().trim();
                String section = sectionEditText.getText().toString().trim();
                String adviserName = adviserNameEditText.getText().toString().trim();
                String salutation = salutationEditText.getText().toString().trim();

                // Validate the input
                if (gradeLevel.isEmpty()) {
                    gradeLevelEditText.setError("Please enter a grade level");
                    return;
                }
                if (section.isEmpty()) {
                    sectionEditText.setError("Please enter a section");
                    return;
                }
                if (adviserName.isEmpty()) {
                    adviserNameEditText.setError("Please enter an adviser name");
                    return;
                }
                if (salutation.isEmpty()) {
                    salutationEditText.setError("Please enter a salutation");
                    return;
                }

                // Update the data in the MySQL database
                try {
                    statement = connection.createStatement();
                    statement.executeUpdate("UPDATE teacher_dashboard_glevel_data SET grade_level = '" + gradeLevel + "', section = '" + section + "', adviser_name = '" + adviserName + "', salutation = '" + salutation + "' WHERE id = " + gradeLevelId);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // Return to the previous activity
                finish();
            }
        });

        // Set a click listener for the Delete button
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Delete the data from the MySQL database
                try {
                    statement = connection.createStatement();
                    statement.executeUpdate("DELETE FROM teacher_dashboard_glevel_data WHERE id=" + gradeLevelId);



                    // Display a success message
                    Toast.makeText(EditGradeLevelActivity.this, "Grade level deleted successfully.", Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Toast.makeText(EditGradeLevelActivity.this, "An error occurred while deleting the grade level.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}