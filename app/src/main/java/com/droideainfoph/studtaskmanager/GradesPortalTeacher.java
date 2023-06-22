package com.droideainfoph.studtaskmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;

public class GradesPortalTeacher extends AppCompatActivity {


    private Handler handler;
    private Runnable saveGradesRunnable;
    Spinner quarterSelector;
    TextView gradeLevelSign;
    LinearLayout studentsViewerEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades_portal_teacher);

        quarterSelector = findViewById(R.id.quarter_spinner);
        gradeLevelSign = findViewById(R.id.grade_level_text);
        studentsViewerEditor = findViewById(R.id.allStudentView);

        Intent intent = getIntent();
        String gradeLevel = intent.getStringExtra("gradeLevel");

        String tableName2 = intent.getStringExtra("tableName");

        gradeLevelSign.setText(gradeLevel);

        // Inside the GradesPortalTeacher activity's onCreate() method or any other appropriate method

        // Retrieve the table name from the intent extras
        String tableName = intent.getStringExtra("tableName");



        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Perform the back button action
                finish();
            }
        });

        quarterSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedQuarter = parent.getItemAtPosition(position).toString();

                if (selectedQuarter.equals("1st Quarter")) {
                    // Handle 1st Quarter logic

                    String selectedQuarterGradesDecision = "1st_quarter_grades";
                    String selectedQuarterStatusDecision = "1st_quarter_status";


                    // Create an AsyncTask to get the LRN list
                    FetchLRNListTask fetchLRNListTask = new FetchLRNListTask(tableName, selectedQuarterGradesDecision, selectedQuarterStatusDecision);
                    fetchLRNListTask.execute();


                    Toast.makeText(GradesPortalTeacher.this, "You are now editing the 1st Quarter Value", Toast.LENGTH_SHORT).show();


                } else if (selectedQuarter.equals("2nd Quarter")) {
                    // Handle 2nd Quarter logic

                    String selectedQuarterGradesDecision = "2nd_quarter_grades";
                    String selectedQuarterStatusDecision = "2nd_quarter_status";


                    // Create an AsyncTask to get the LRN list
                    FetchLRNListTask fetchLRNListTask = new FetchLRNListTask(tableName, selectedQuarterGradesDecision, selectedQuarterStatusDecision);
                    fetchLRNListTask.execute();


                    Toast.makeText(GradesPortalTeacher.this, "You are now editing the 2nd Quarter Value", Toast.LENGTH_SHORT).show();


                } else if (selectedQuarter.equals("3rd Quarter")) {
                    // Handle 3rd Quarter logic

                    String selectedQuarterGradesDecision = "3rd_quarter_grades";
                    String selectedQuarterStatusDecision = "3rd_quarter_status";


                    // Create an AsyncTask to get the LRN list
                    FetchLRNListTask fetchLRNListTask = new FetchLRNListTask(tableName, selectedQuarterGradesDecision, selectedQuarterStatusDecision);
                    fetchLRNListTask.execute();


                    Toast.makeText(GradesPortalTeacher.this, "You are now editing the 3rd Quarter Value", Toast.LENGTH_SHORT).show();


                } else if (selectedQuarter.equals("4th Quarter")) {
                    // Handle 4th Quarter logic


                    String selectedQuarterGradesDecision = "4th_quarter_grades";
                    String selectedQuarterStatusDecision = "4th_quarter_status";


                    // Create an AsyncTask to get the LRN list
                    FetchLRNListTask fetchLRNListTask = new FetchLRNListTask(tableName, selectedQuarterGradesDecision, selectedQuarterStatusDecision);
                    fetchLRNListTask.execute();

                } else {
                    // Handle unknown quarter logic or display an error message
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(GradesPortalTeacher.this, "Please select a quarter value for operation", Toast.LENGTH_SHORT).show();
            }

        });


    }





    private void startAutoSaveGrades() {
        handler = new Handler();
        saveGradesRunnable = new Runnable() {
            @Override
            public void run() {
               //~~~~~~~~~~~~~~~~~~~~~~~~~
                handler.postDelayed(this, 5000); // Delay of 5 seconds (5000 milliseconds)
            }
        };
        handler.postDelayed(saveGradesRunnable, 5000); // Initial delay of 5 seconds
    }

    private void stopAutoSaveGrades() {
        if (handler != null && saveGradesRunnable != null) {
            handler.removeCallbacks(saveGradesRunnable);
        }
    }

    // Usage example
    @Override
    protected void onResume() {
        super.onResume();
        startAutoSaveGrades();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoSaveGrades();
    }


    private class FetchLRNListTask extends AsyncTask<Void, Void, ArrayList<String>> {
        private String tableName;
        private String selectedQuarterGradesDecision;
        private String selectedQuarterStatusDecision;

        public FetchLRNListTask(String tableName, String selectedQuarterGradesDecision, String selectedQuarterStatusDecision) {
            this.tableName = tableName;
            this.selectedQuarterGradesDecision = selectedQuarterGradesDecision;
            this.selectedQuarterStatusDecision = selectedQuarterStatusDecision;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> lrnList = new ArrayList<>();


            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to retrieve the LRN list from the specified table
                String query = "SELECT lrn FROM " + tableName;
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery();

                // Iterate through the result set and add each LRN to the lrnList
                while (resultSet.next()) {
                    String lrn = resultSet.getString("lrn");
                    lrnList.add(lrn);
                }

                // Close the database resources
                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return lrnList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> lrnList) {
            // Pass the LRN list to the FetchStudentNameTask
            FetchStudentNameTask fetchStudentNameTask = new FetchStudentNameTask(selectedQuarterGradesDecision, selectedQuarterStatusDecision);
            fetchStudentNameTask.execute(lrnList);
        }


    }


    private class FetchStudentNameTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        private String selectedQuarterGradesDecision;
        private String selectedQuarterStatusDecision;
        private String studentLastNameFirstname;
        private String studentLRN;

        public FetchStudentNameTask(String selectedQuarterGradesDecision, String selectedQuarterStatusDecision) {
            this.selectedQuarterGradesDecision = selectedQuarterGradesDecision;
            this.selectedQuarterStatusDecision = selectedQuarterStatusDecision;
        }



        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... lrnLists) {
            ArrayList<String> lrnList = lrnLists[0];
            ArrayList<String> studentList = new ArrayList<>();


            // Sort the studentList alphabetically
            Collections.sort(studentList);
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                for (String lrn : lrnList) {
                    // Execute a SQL query to retrieve the corresponding name and 1st quarter grades/status associated with the LRN
                    String query = "SELECT lastname, firstname FROM studtask_user_student WHERE lrn = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, lrn);
                    ResultSet resultSet = statement.executeQuery();

                    // Check if a result is found
                    if (resultSet.next()) {
                        // Retrieve the last name and first name from the result set
                        String lastname = resultSet.getString("lastname");
                        String firstname = resultSet.getString("firstname");


                        // Create a string representation of the student's name and LRN
                        String studentData = lastname + ", " + firstname + "|" + lrn;

                        // Add the student's data to the studentList
                        studentList.add(studentData);


                    }

                    resultSet.close();
                    statement.close();
                }

                connection.close();

                // Sort the studentList alphabetically
                Collections.sort(studentList);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return studentList;
        }



        @Override
        protected void onPostExecute(ArrayList<String> studentList) {

            // Clear previously displayed views
            studentsViewerEditor.removeAllViews();

            for (String studentData : studentList) {
                // Split the student data into LRN and last name with first name
                String[] studentDataArray = studentData.split("\\|"); // Use "|" as the separator
                studentLastNameFirstname = studentDataArray[0]; // Get the combined last name and first name
                studentLRN = studentDataArray[1]; // Get the LRN

                // Instantiate and execute the FetchGradesTask to fetch the grades and spinner values
                FetchGradesTask fetchGradesTask = new FetchGradesTask();
                fetchGradesTask.execute(studentLRN, studentLastNameFirstname, selectedQuarterGradesDecision, selectedQuarterStatusDecision);

            }

        }




    }


    private class FetchGradesTask extends AsyncTask<String, Void, String> {
        private String studentLRN;
        private String studentLastNameFirstname;
        private String gradesValueFrom;
        private String spinnerValue;

        private String selectedQuarterGradesDecision;
        private String selectedQuarterStatusDecision;

        @Override
        protected String doInBackground(String... params) {
            studentLRN = params[0];
            studentLastNameFirstname = params[1];
            selectedQuarterGradesDecision = params[2];
            selectedQuarterStatusDecision = params[3];

            Intent intent = getIntent();
            String tableName2 = intent.getStringExtra("tableName");


            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to retrieve the grades and spinner values associated with the studentLRN
                String query = "SELECT "+selectedQuarterGradesDecision+", "+selectedQuarterStatusDecision+" FROM "+tableName2+" WHERE lrn = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, studentLRN);
                ResultSet resultSet = statement.executeQuery();

                // Check if a result is found
                if (resultSet.next()) {
                    // Retrieve the grades and spinner values from the result set
                    gradesValueFrom = resultSet.getString(selectedQuarterGradesDecision);
                    spinnerValue = resultSet.getString(selectedQuarterStatusDecision);
                }

                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return gradesValueFrom;
        }

        @Override
        protected void onPostExecute(String gradesValue) {
            // Create the CardView for each student
            CardView cardView = new CardView(GradesPortalTeacher.this);
            LinearLayout.LayoutParams cardViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardView.setLayoutParams(cardViewParams);
            cardView.setCardElevation(4);
            cardView.setUseCompatPadding(true);
            cardView.setContentPadding(16, 16, 16, 16);

            // Create the parent LinearLayout for each student item
            LinearLayout itemLayout = new LinearLayout(GradesPortalTeacher.this);
            LinearLayout.LayoutParams itemLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemLayout.setLayoutParams(itemLayoutParams);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);

            // Create the LinearLayout for student name and LRN
            LinearLayout nameLayout = new LinearLayout(GradesPortalTeacher.this);
            LinearLayout.LayoutParams nameLayoutParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 3);
            nameLayout.setLayoutParams(nameLayoutParams);
            nameLayout.setOrientation(LinearLayout.VERTICAL);

            // Create the TextView for the student's name
            TextView nameTextView = new TextView(GradesPortalTeacher.this);
            nameTextView.setTextAppearance(GradesPortalTeacher.this, android.R.style.TextAppearance_Medium);
            String studentName = studentLastNameFirstname;
            nameTextView.setText(studentName.toUpperCase()); // Convert the name to uppercase

            // Create the TextView for the student's LRN
            TextView lrnTextView = new TextView(GradesPortalTeacher.this);
            lrnTextView.setTextAppearance(GradesPortalTeacher.this, android.R.style.TextAppearance_Small);
            lrnTextView.setText("LRN: " + studentLRN);

            // Add the name and LRN views to the nameLayout
            nameLayout.addView(nameTextView);
            nameLayout.addView(lrnTextView);

            // Create the LinearLayout for spinner and grades
            LinearLayout spinnerLayout = new LinearLayout(GradesPortalTeacher.this);
            LinearLayout.LayoutParams spinnerLayoutParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            spinnerLayout.setLayoutParams(spinnerLayoutParams);
            spinnerLayout.setGravity(Gravity.CENTER);

            // Create the spinner for grades status
            Spinner gradesSpinner = new Spinner(GradesPortalTeacher.this);
            String[] spinnerValues = {"Tentative", "Final"}; // Array of spinner values
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(GradesPortalTeacher.this,
                    android.R.layout.simple_spinner_dropdown_item, spinnerValues);
            gradesSpinner.setAdapter(spinnerAdapter);
            gradesSpinner.setSelection(spinnerAdapter.getPosition(spinnerValue));


            // Create the LinearLayout for grades
            LinearLayout gradesLayout = new LinearLayout(GradesPortalTeacher.this);
            LinearLayout.LayoutParams gradesLayoutParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            gradesLayout.setLayoutParams(gradesLayoutParams);
            gradesLayout.setGravity(Gravity.CENTER);

            // Create the EditText for user grades
            EditText gradesEditText = new EditText(GradesPortalTeacher.this);
            gradesEditText.setText(gradesValue);
            // Set up the EditText properties as needed

            // Add tags to the EditText and Spinner for easy retrieval later
            gradesEditText.setTag(studentLRN + "_grades");
            gradesEditText.setSingleLine(true);
            gradesSpinner.setTag(studentLRN + "_spinner");

            // Add the spinner and grades views to their respective layouts
            spinnerLayout.addView(gradesSpinner);
            gradesLayout.addView(gradesEditText);

            // Add the views to the itemLayout
            itemLayout.addView(nameLayout);
            itemLayout.addView(spinnerLayout);
            itemLayout.addView(gradesLayout);

            // Add the itemLayout to the parentLayout
            studentsViewerEditor.addView(itemLayout);

            // Set listeners for EditText and Spinner
            gradesEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Do nothing
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Do nothing
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    // Perform database update for grades
                    String grades = editable.toString();
                    updateGradesInDatabase(studentLRN, grades);
                }
            });

            gradesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    // Perform database update for spinner value
                    String status = adapterView.getItemAtPosition(position).toString();
                    updateStatusInDatabase(studentLRN, status);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Do nothing
                }
            });

            // Use the gradesValue and spinnerValue as needed
        }


        private void updateGradesInDatabase(String studentLRN, String grades) {
            // Execute the database update in an AsyncTask or background thread
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    Intent intent = getIntent();
                    String tableName2 = intent.getStringExtra("tableName");

                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                                getString(R.string.db_username), getString(R.string.db_password));

                        // Prepare the update statement
                        String query = "UPDATE " + tableName2 + " SET "+selectedQuarterGradesDecision+" = ? WHERE lrn = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, grades);
                        statement.setString(2, studentLRN);

                        // Execute the update statement
                        int rowsAffected = statement.executeUpdate();

                        // Close the database resources
                        statement.close();
                        connection.close();

                        // Check the number of rows affected to determine if the update was successful
                        if (rowsAffected > 0) {
                            // Update successful, perform any required UI updates or show a success message
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Perform UI updates or show success message


                                }
                            });
                        } else {
                            // Update failed, perform any required UI updates or show an error message
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Perform UI updates or show error message

                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Handle any exceptions that occurred during the database update
                    }
                }
            });
        }

        private void updateStatusInDatabase(String studentLRN, String status) {
            // Execute the database update in an AsyncTask or background thread
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Intent intent = getIntent();
                    String tableName2 = intent.getStringExtra("tableName");
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                                getString(R.string.db_username), getString(R.string.db_password));

                        // Prepare the update statement
                        String query = "UPDATE " + tableName2 + " SET "+selectedQuarterStatusDecision+" = ? WHERE lrn = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, status);
                        statement.setString(2, studentLRN);

                        // Execute the update statement
                        int rowsAffected = statement.executeUpdate();

                        // Close the database resources
                        statement.close();
                        connection.close();

                        // Check the number of rows affected to determine if the update was successful
                        if (rowsAffected > 0) {
                            // Update successful, perform any required UI updates or show a success message
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Perform UI updates or show success message

                                }
                            });
                        } else {
                            // Update failed, perform any required UI updates or show an error message
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Perform UI updates or show error message

                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Handle any exceptions that occurred during the database update
                    }
                }
            });
        }





    }













}




