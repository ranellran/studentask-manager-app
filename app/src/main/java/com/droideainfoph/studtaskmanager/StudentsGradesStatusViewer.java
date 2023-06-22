package com.droideainfoph.studtaskmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class StudentsGradesStatusViewer extends AppCompatActivity {

    TextView gradesTextViewer;
    TextView gradesStatusViewer;
    TextView studentSubjectNameViewer;
    TextView otherInfoMessage;
    TextView passedOrFailed;
    Button quarterSelectorButton;









    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_grades_status_viewer);

        gradesTextViewer = findViewById(R.id.text_grades_viewer);
        gradesStatusViewer = findViewById(R.id.text_grade_status1);
        studentSubjectNameViewer = findViewById(R.id.text_subject_name_viewer);
        otherInfoMessage = findViewById(R.id.other_message);
        passedOrFailed = findViewById(R.id.text_grade_status2);
        quarterSelectorButton = findViewById(R.id.s_quarter_selector_button);


        String tableName = getIntent().getStringExtra("tableName");


        SharedPreferences sharedPreferences = getSharedPreferences("students_lrn", Context.MODE_PRIVATE);
        String studentsLRN = sharedPreferences.getString("lrn", "");

        String quarterGradesDesicion5 = "1st_quarter_grades";
        String quarterGradesStatusDesicion5 = "1st_quarter_status";

        // Get the shared preferences object
        SharedPreferences gradesAndStatusDecisionMaker = getSharedPreferences("gradesAndStatusDecisionMaker", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = gradesAndStatusDecisionMaker.edit();
        editor.putString("gradesDecisionMaker", quarterGradesDesicion5);
        editor.putString("gradesStatusDecisionMaker", quarterGradesStatusDesicion5);
        editor.apply();


        // Pass the table name and LRN to the FetchGradesTask
        FetchGradesTask fetchGradesTask = new FetchGradesTask(tableName, studentsLRN, quarterGradesDesicion5, quarterGradesStatusDesicion5);
        fetchGradesTask.execute();





        // Declare the SwipeRefreshLayout and its listener
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {


            // Retrieve the values
            String gradesDecisionMaker = gradesAndStatusDecisionMaker.getString("gradesDecisionMaker", "");
            String gradesStatusDecisionMaker = gradesAndStatusDecisionMaker.getString("gradesStatusDecisionMaker", "");

            // Use the retrieved values
            Log.d("TAG", "Grades Decision Maker: " + gradesDecisionMaker);
            Log.d("TAG", "Grades Status Decision Maker: " + gradesStatusDecisionMaker);


            // Pass the table name and LRN to the FetchGradesTask
            FetchGradesTask fetchGradesTaskOther = new FetchGradesTask(tableName, studentsLRN, gradesDecisionMaker, gradesStatusDecisionMaker);
            fetchGradesTaskOther.execute();


            // Simulate a delay of 3 seconds before stopping the refreshing animation
            new Handler().postDelayed(() -> {
                // After the delay, call setRefreshing(false) to stop the refreshing animation
                swipeRefreshLayout.setRefreshing(false);
            }, 3000); // 3000 milliseconds = 3 seconds
        });





        quarterSelectorButton.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(StudentsGradesStatusViewer.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_box_quarter_selection, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            // Set CardView style and round corners
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
            }

            // Retrieve the radio button views from the dialog's layout
            RadioButton firstQuarterRadioButton = dialogView.findViewById(R.id.firstQuarterRadioButton);
            RadioButton secondQuarterRadioButton = dialogView.findViewById(R.id.secondQuarterRadioButton);
            RadioButton thirdQuarterRadioButton = dialogView.findViewById(R.id.thirdQuarterRadioButton);
            RadioButton fourthQuarterRadioButton = dialogView.findViewById(R.id.fourthQuarterRadioButton);



            // Set an OnClickListener for the radio button views
            firstQuarterRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Use the gradesDecisionMaker and gradesStatusDecisionMaker variables here
                    String gradesDecisionMaker = "1st_quarter_grades";
                    String gradesStatusDecisionMaker = "1st_quarter_status";


                    // Get the shared preferences object
                    SharedPreferences gradesAndStatusDecisionMaker = getSharedPreferences("gradesAndStatusDecisionMaker", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = gradesAndStatusDecisionMaker.edit();
                    editor.putString("gradesDecisionMaker", gradesDecisionMaker);
                    editor.putString("gradesStatusDecisionMaker", gradesStatusDecisionMaker);
                    editor.apply();


                    // Pass the table name and LRN to the FetchGradesTask
                    FetchGradesTask fetchGradesTask = new FetchGradesTask(tableName, studentsLRN, gradesDecisionMaker, gradesStatusDecisionMaker);
                    fetchGradesTask.execute();

                    // Close the dialog box
                    dialog.dismiss();
                }
            });




            secondQuarterRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String gradesDecisionMaker = "2nd_quarter_grades";
                    String gradesStatusDecisionMaker = "2nd_quarter_status";

                    // Get the shared preferences object
                    SharedPreferences gradesAndStatusDecisionMaker = getSharedPreferences("gradesAndStatusDecisionMaker", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = gradesAndStatusDecisionMaker.edit();
                    editor.putString("gradesDecisionMaker", gradesDecisionMaker);
                    editor.putString("gradesStatusDecisionMaker", gradesStatusDecisionMaker);
                    editor.apply();

                    // Pass the table name and LRN to the FetchGradesTask
                    FetchGradesTask fetchGradesTask = new FetchGradesTask(tableName, studentsLRN, gradesDecisionMaker, gradesStatusDecisionMaker);
                    fetchGradesTask.execute();

                    // Close the dialog box
                    dialog.dismiss();
                }
            });


            thirdQuarterRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String gradesDecisionMaker = "3rd_quarter_grades";
                    String gradesStatusDecisionMaker = "3rd_quarter_status";

                    // Get the shared preferences object
                    SharedPreferences gradesAndStatusDecisionMaker = getSharedPreferences("gradesAndStatusDecisionMaker", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = gradesAndStatusDecisionMaker.edit();
                    editor.putString("gradesDecisionMaker", gradesDecisionMaker);
                    editor.putString("gradesStatusDecisionMaker", gradesStatusDecisionMaker);
                    editor.apply();

                    // Pass the table name and LRN to the FetchGradesTask
                    FetchGradesTask fetchGradesTask = new FetchGradesTask(tableName, studentsLRN, gradesDecisionMaker, gradesStatusDecisionMaker);
                    fetchGradesTask.execute();

                    // Close the dialog box
                    dialog.dismiss();
                }
            });

            fourthQuarterRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String gradesDecisionMaker = "4th_quarter_grades";
                    String gradesStatusDecisionMaker = "4th_quarter_status";


                    // Get the shared preferences object
                    SharedPreferences gradesAndStatusDecisionMaker = getSharedPreferences("gradesAndStatusDecisionMaker", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = gradesAndStatusDecisionMaker.edit();
                    editor.putString("gradesDecisionMaker", gradesDecisionMaker);
                    editor.putString("gradesStatusDecisionMaker", gradesStatusDecisionMaker);
                    editor.apply();

                    // Pass the table name and LRN to the FetchGradesTask
                    FetchGradesTask fetchGradesTask = new FetchGradesTask(tableName, studentsLRN, gradesDecisionMaker, gradesStatusDecisionMaker);
                    fetchGradesTask.execute();

                    // Close the dialog box
                    dialog.dismiss();
                }
            });

            // Show the dialog
            dialog.show();






        });




        //quarterSelectorButton.setOnClickListener(view -> {
        //    AlertDialog.Builder builder = new AlertDialog.Builder(Student.this);
        //    builder.setView(R.layout.dialog_quarter_selector);
        //    AlertDialog dialog = builder.create();
        //
        //    // Set CardView style and round corners
        //    dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
        //
        //    // Show the dialog
        //    dialog.show();
        //});



    }





    private class FetchGradesTask extends AsyncTask<Void, Void, String[]> {
        private String tableName;
        private String lrn;
        private String gradesDecisionMaker1;
        private String gradesStatusDecisionMaker1;
        private String gradesDecisionMaker2;
        private String gradesStatusDecisionMaker2;

        private String studentsLRN;




        public FetchGradesTask(String tableName, String lrn, String gradesDecisionMaker, String gradesStatusDecisionMaker) {
            this.tableName = tableName;
            this.lrn = lrn;
            this.gradesDecisionMaker1 = gradesDecisionMaker;
            this.gradesStatusDecisionMaker1 = gradesStatusDecisionMaker;

            SharedPreferences sharedPreferences = getSharedPreferences("students_lrn", Context.MODE_PRIVATE);
            String studentsLRN = sharedPreferences.getString("lrn", "");
            SharedPreferences gradesAndStatusDecisionMaker = getSharedPreferences("gradesAndStatusDecisionMaker", Context.MODE_PRIVATE);
            String gradesDecisionMaker2 = gradesAndStatusDecisionMaker.getString("gradesDecisionMaker", "");
            String gradesStatusDecisionMaker2 = gradesAndStatusDecisionMaker.getString("gradesStatusDecisionMaker", "");

            this.gradesDecisionMaker2 = gradesDecisionMaker2;
            this.gradesStatusDecisionMaker2 = gradesStatusDecisionMaker2;
            this.studentsLRN = studentsLRN;
        }



        @Override
        protected String[] doInBackground(Void... voids) {
            String grades = null;
            String status = null;
            String subjectName = null;



            // Use the retrieved values
            Log.d("TAG", "Grades Decision Maker: " + gradesDecisionMaker2);
            Log.d("TAG", "Grades Status Decision Maker: " + gradesStatusDecisionMaker2);
            Log.d("TAG", "studentsLRN: " + studentsLRN);
            Log.d("TAG", "tableName: " + tableName);

            try {
                // Establish a database connection
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to retrieve the grades and status corresponding to the LRN from the generated table
                PreparedStatement statement = connection.prepareStatement("SELECT "+gradesDecisionMaker2+", "+gradesStatusDecisionMaker2+", subject_name FROM " + tableName + " WHERE lrn = ?");
                statement.setString(1, studentsLRN); // Set the parameter value for LRN
                ResultSet resultSet = statement.executeQuery();

                // Check if a result is found
                if (resultSet.next()) {
                    // Retrieve the grades and status from the result set
                    grades = resultSet.getString(gradesDecisionMaker2);
                    status = resultSet.getString(gradesStatusDecisionMaker2);
                    subjectName = resultSet.getString("subject_name");
                }




                // ...
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Create an array to hold the values
            String[] result = new String[3];
            result[0] = grades;
            result[1] = status;
            result[2] = subjectName;


            return result;
        }

        @Override
        protected void onPostExecute(String[] values) {
            String grades = values[0];
            String status = values[1];
            String subjectName = values[2];
            String quarterDecision = gradesDecisionMaker2;



            String finalQuarterDecision;

            if (quarterDecision.equals("1st_quarter_grades")) {
                // Perform actions for 1st quarter grades
                finalQuarterDecision = "1st Quarter";
            } else if (quarterDecision.equals("2nd_quarter_grades")) {
                // Perform actions for 2nd quarter grades
                finalQuarterDecision = "2nd Quarter";
            } else if (quarterDecision.equals("3rd_quarter_grades")) {
                // Perform actions for 3rd quarter grades
                finalQuarterDecision = "3rd Quarter";
            } else if (quarterDecision.equals("4th_quarter_grades")) {
                // Perform actions for 4th quarter grades
                finalQuarterDecision = "4th Quarter";
            } else {
                // Handle unknown quarter decision
                finalQuarterDecision = ""; // Set a default value or handle the error case
                // Handle the case when grades, status, or subjectName is null
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StudentsGradesStatusViewer.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }




            if (!TextUtils.isEmpty(grades) && !TextUtils.isEmpty(status) && !TextUtils.isEmpty(subjectName)) {


                // Check if grades indicate pass or fail
                double gradeValue = Double.parseDouble(grades);
                String gradeStatus = (gradeValue >= 75.00) ? "[ Passed ]" : "[ Failed ]";



                if (grades != null && status != null && subjectName != null) {


                    // Check if grades are lower than 75
                    if (Float.parseFloat(grades) < 75) {
                        // Update the TextViews with the retrieved data
                        gradesTextViewer.setText(grades);
                        gradesTextViewer.setTextColor(Color.parseColor("#e91e63"));

                        // Update the grade status TextView
                        passedOrFailed.setText(gradeStatus);
                        passedOrFailed.setTextColor(Color.parseColor("#e91e63"));

                        otherInfoMessage.setText("From "+finalQuarterDecision+" Grade Distribution");

                        gradesStatusViewer.setText("A " + status + " Grade For the Subject");

                    } else {

                        // Update the grade status TextView
                        passedOrFailed.setText(gradeStatus);
                        passedOrFailed.setTextColor(Color.parseColor("#000000"));

                        otherInfoMessage.setText("From "+finalQuarterDecision+" Grade Distribution");

                        // Update the TextViews with the retrieved data
                        gradesTextViewer.setText(grades);
                        gradesTextViewer.setTextColor(Color.parseColor("#000000"));

                        gradesStatusViewer.setText("A " + status + " Grade For the Subject");

                    }

                    studentSubjectNameViewer.setText(subjectName);


                } else {


                    gradesTextViewer.setText(":(");
                    gradesTextViewer.setTextColor(Color.parseColor("#000000"));

                    gradesStatusViewer.setText("No Data Found Right Now!");
                    studentSubjectNameViewer.setText(subjectName);
                    otherInfoMessage.setText("Subject Name");


                    // Handle the case when grades, status, or subjectName is null
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(StudentsGradesStatusViewer.this, "Data is incomplete", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } else {

                gradesTextViewer.setText(":(");
                gradesTextViewer.setTextColor(Color.parseColor("#000000"));

                gradesStatusViewer.setText("No Data Found Right Now!");
                studentSubjectNameViewer.setText(subjectName);
                otherInfoMessage.setText("Subject Name");


                // Handle the case when grades, status, or subjectName is null
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StudentsGradesStatusViewer.this, "Data is incomplete", Toast.LENGTH_SHORT).show();
                    }
                });






            }


        }
    }
















                //grades = databaseResult[0];
                //status = "A " + databaseResult[1] + " Grades for";
                //subjectName = databaseResult[2];













}