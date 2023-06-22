package com.droideainfoph.studtaskmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentSubjectEnrolledShow extends AppCompatActivity {

    LinearLayout studentLayoutGetSubjectList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String gradeLevel;
    Button btnAddSubject;
    private boolean refreshing = false; // Flag to indicate if refresh is in progress


    // Define the SubjectData class
    private static class SubjectData {
        private int id;
        private String subjectName;
        private String subjectTeacherName;
        private String semestral;
        private String semestralValue;
        private String quarterType;
        private String uniqueCode;
        private String dateCreated;

        public SubjectData(int id, String subjectName, String subjectTeacherName, String semestral, String semestralValue, String quarterType, String uniqueCode, String dateCreated) {
            this.id = id;
            this.subjectName = subjectName;
            this.subjectTeacherName = subjectTeacherName;
            this.semestral = semestral;
            this.semestralValue = semestralValue;
            this.quarterType = quarterType;
            this.uniqueCode = uniqueCode;
            this.dateCreated = dateCreated;
        }


        public int getId() {
            return id;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public String getTeacherName() {
            return subjectTeacherName;
        }

        public String getSemestral() {
            return semestral;
        }

        public String getSemestralValue() {
            return semestralValue;
        }

        public String getQuarterType() {
            return quarterType;
        }

        public String getUniqueCode() {
            return uniqueCode;
        }

        public String getDateCreated() {
            return dateCreated;
        }


        public int getSubjectId() {
            return id;
        }

    }


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_subject_enrolled_show);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        btnAddSubject = findViewById(R.id.s_add_subject_button);

        btnAddSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUniqueCodePrompt();
            }
        });

        // Set a listener for the refresh action
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!refreshing) { // Check if refresh is not already in progress
                    refreshing = true; // Set the flag to indicate refresh is in progress
                    // Call the GetSubjectListDataTask to fetch the subject data
                    GetSubjectListDataTask getSubjectListDataTask = new GetSubjectListDataTask(gradeLevel);
                    getSubjectListDataTask.execute();
                    // Delay hiding the refresh indicator for 3 seconds
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            refreshing = false; // Set the flag to indicate refresh is complete
                        }
                    }, 3000);
                }
            }
        });


        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Perform the back button action

            }
        });


        studentLayoutGetSubjectList = findViewById(R.id.s_subject_enrolled_list_layout);

        SharedPreferences sharedPreferences = getSharedPreferences("students_lrn", Context.MODE_PRIVATE);
        String studentsLRN = sharedPreferences.getString("lrn", "");

        SearchGradeLevelTask searchGradeLevelTask = new SearchGradeLevelTask(studentsLRN);
        searchGradeLevelTask.execute();


    }


    private void showUniqueCodePrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Unique Code");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        final AlertDialog dialog = builder.create(); // Create the dialog instance
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String uniqueCode = input.getText().toString().trim();
                // Perform any desired validation or processing with the unique code
                // and proceed with adding the subject if the code is valid
                addSubjectWithUniqueCode(uniqueCode);
            }
        });

        // Disable the automatic expansion of the EditText on pressing the Enter key
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String uniqueCode = input.getText().toString().trim();
                    // Perform any desired validation or processing with the unique code
                    // and proceed with adding the subject if the code is valid
                    addSubjectWithUniqueCode(uniqueCode);
                    dialog.dismiss(); // Dismiss the dialog after processing the code
                    return true;
                }
                return false;
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addSubjectWithUniqueCode(String uniqueCode) {

        new SubjectDataGathered().execute(uniqueCode);

    }






    public class SubjectDataGathered extends AsyncTask<String, Void, Void> {

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(String... uniqueCodes) {
            if (uniqueCodes.length > 0) {
                String uniqueCode = uniqueCodes[0];

                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                            getString(R.string.db_username), getString(R.string.db_password));

                    String query = "SELECT * FROM subject_list_data WHERE unique_code = ?";
                    try {
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, uniqueCode);
                        ResultSet resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            // Data found for the unique code, retrieve the values
                            int idSubjectBelong = resultSet.getInt("id_subject_belong");
                            String gradeLevelSubjectBelong = resultSet.getString("grade_level_subject_belong");
                            String subjectName = resultSet.getString("subject_name");
                            String teacherName = resultSet.getString("teacher_name");
                            String semestral = resultSet.getString("semestral");
                            String semestralValue = resultSet.getString("semestral_value");
                            String quarterType = resultSet.getString("quarter_type");
                            String uniqueCodeResult = resultSet.getString("unique_code");
                            Date dateCreated = resultSet.getDate("date_created");

                            // Process the retrieved values as needed
                            // For example, display them in a toast message
                            String toastMessage = "Subject exists:\n" +
                                    "Subject Name: " + subjectName + "\n" +
                                    "Teacher Name: " + teacherName;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(StudentSubjectEnrolledShow.this, toastMessage, Toast.LENGTH_SHORT).show();
                                }
                            });


                            // Create the data bundle to pass to the DatabaseTableFinder AsyncTask
                            Bundle dataBundle = new Bundle();
                            dataBundle.putInt("idSubjectBelong", idSubjectBelong);
                            dataBundle.putString("gradeLevelSubjectBelong", gradeLevelSubjectBelong);
                            dataBundle.putString("subjectName", subjectName);
                            dataBundle.putString("teacherName", teacherName);
                            dataBundle.putString("semestral", semestral);
                            dataBundle.putString("semestralValue", semestralValue);
                            dataBundle.putString("quarterType", quarterType);
                            dataBundle.putString("uniqueCode", uniqueCodeResult);
                            dataBundle.putSerializable("dateCreated", dateCreated);

                            // Execute the DatabaseTableFinder AsyncTask and pass the data bundle
                            if (dataBundle != null) {
                                new DatabaseTableFinder().execute(dataBundle);
                            } else {

                                Toast.makeText(StudentSubjectEnrolledShow.this, "Error while trying to get the subject data", Toast.LENGTH_SHORT).show();


                            }
                        } else {
                            // Subject does not exist
                            Toast.makeText(StudentSubjectEnrolledShow.this, "Subject does not exist", Toast.LENGTH_SHORT).show();
                        }

                        resultSet.close();
                        statement.close();
                    } catch (SQLException e) {
                        // Handle any database errors
                        e.printStackTrace();
                    }

                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }







    private class DatabaseTableFinder extends AsyncTask<Bundle, Void, Void> {
        private boolean isInsertionSuccessful = false; // Class variable to store insertion status

        @Override
        protected Void doInBackground(Bundle... bundles) {
            Bundle dataBundle = bundles[0];

            SharedPreferences sharedPreferences = getSharedPreferences("students_lrn", Context.MODE_PRIVATE);
            String studentsLRN = sharedPreferences.getString("lrn", "");




            // Retrieve the data from the bundle
            int idSubjectBelong = dataBundle.getInt("idSubjectBelong");
            String gradeLevelSubjectBelong = dataBundle.getString("gradeLevelSubjectBelong");
            String subjectName = dataBundle.getString("subjectName");
            String teacherName = dataBundle.getString("teacherName");
            String semestral = dataBundle.getString("semestral");
            String semestralValue = dataBundle.getString("semestralValue");
            String quarterType = dataBundle.getString("quarterType");
            String uniqueCode = dataBundle.getString("uniqueCode");
            Date dateCreated = (Date) dataBundle.getSerializable("dateCreated");

            String searchTableName = "subject_"+uniqueCode;



            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                // Create the SQL statement to insert data into the specified table

                String insertQuery = "INSERT INTO " + searchTableName +
                        " (grade_level_id_belong, grade_level_belong, subject_name, subject_teacher_name, semestral_type, semestral_value, quarter_type, unique_code, date_created_belong, lrn, table_name) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(insertQuery);
                statement.setInt(1, idSubjectBelong);
                statement.setString(2, gradeLevelSubjectBelong);

                statement.setString(3, subjectName);
                statement.setString(4, teacherName);
                statement.setString(5, semestral);
                statement.setString(6, semestralValue);
                statement.setString(7, quarterType);
                statement.setString(8, uniqueCode);
                statement.setDate(9, new java.sql.Date(dateCreated.getTime()));

                statement.setString(10, studentsLRN);
                statement.setString(11, searchTableName);


                // Execute the insert statement
                int rowsAffected = statement.executeUpdate();

                statement.close();
                connection.close();

                isInsertionSuccessful = rowsAffected > 0; // Set class variable based on insertion result
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isInsertionSuccessful) {
                Toast.makeText(StudentSubjectEnrolledShow.this, "Subject Add Succeed", Toast.LENGTH_SHORT).show();


            } else {
                Toast.makeText(StudentSubjectEnrolledShow.this, "Cannot Add Subject", Toast.LENGTH_SHORT).show();
            }
        }
    }




















    private class SearchGradeLevelTask extends AsyncTask<Void, Void, String> {
        private String studentsLRN;

        public SearchGradeLevelTask(String studentsLRN) {
            this.studentsLRN = studentsLRN;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String gradeLevel = null;

            try {
                // Connect to the MySQL database
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to retrieve the grade level belonging to the specific LRN
                PreparedStatement statement = connection.prepareStatement("SELECT grade_level FROM studtask_user_student WHERE lrn = ?");
                statement.setString(1, studentsLRN); // Set the parameter value for LRN
                ResultSet resultSet = statement.executeQuery();

                // Check if a result is found
                if (resultSet.next()) {
                    // Retrieve the grade level from the result set
                    gradeLevel = resultSet.getString("grade_level");
                }

                // Close the database connection and statement
                resultSet.close();
                statement.close();
                connection.close();
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }

            return gradeLevel;
        }

        @Override
        protected void onPostExecute(String gradeLevel) {
            // Do something with the grade level
            if (gradeLevel != null) {

                StudentSubjectEnrolledShow.this.gradeLevel = gradeLevel;

                // Grade level found, handle the result
                GetSubjectListDataTask getSubjectListDataTask = new GetSubjectListDataTask(gradeLevel);
                getSubjectListDataTask.execute();
            } else {
                // LRN not found in the database, handle the case
                // ...
            }
        }

    }



    private class GetSubjectListDataTask extends AsyncTask<Void, Void, List<SubjectData>> {
        private String gradeLevel;

        public GetSubjectListDataTask(String gradeLevel) {
            this.gradeLevel = gradeLevel;
        }

        @Override
        protected List<SubjectData> doInBackground(Void... voids) {
            List<SubjectData> subjectDataList = new ArrayList<>();

            SharedPreferences sharedPreferences = getSharedPreferences("students_lrn", Context.MODE_PRIVATE);
            String studentsLRN = sharedPreferences.getString("lrn", "");

            try {
                // Connect to the MySQL database
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to retrieve the subjects from the subject_list_data table
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM subject_list_data WHERE grade_level_subject_belong = ?");
                statement.setString(1, gradeLevel);
                ResultSet resultSet = statement.executeQuery();

                // Iterate through the result set and add each subject data to the list
                while (resultSet.next()) {
                    String uniqueCode = resultSet.getString("unique_code");
                    String tableName = "subject_" + uniqueCode;

                    // Create a SQL query to check if the LRN is present in the table
                    String lrnQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE lrn = ?";
                    PreparedStatement lrnStatement = connection.prepareStatement(lrnQuery);
                    lrnStatement.setString(1, studentsLRN);
                    ResultSet lrnResultSet = lrnStatement.executeQuery();

                    if (lrnResultSet.next() && lrnResultSet.getInt(1) == 1) {
                        // LRN found in the table, add the subject data to the list
                        int id = resultSet.getInt("id_subject_belong");
                        String subjectName = resultSet.getString("subject_name");
                        String teacherName = resultSet.getString("teacher_name");
                        String semestral = resultSet.getString("semestral");
                        String semestralValue = resultSet.getString("semestral_value");
                        String quarterType = resultSet.getString("quarter_type");
                        String dateCreated = resultSet.getString("date_created");

                        SubjectData subjectData = new SubjectData(id, subjectName, teacherName, semestral, semestralValue, quarterType, uniqueCode, dateCreated);
                        subjectDataList.add(subjectData);
                    }

                    // Close the database resources for LRN query
                    lrnResultSet.close();
                    lrnStatement.close();
                }

                // Close the database resources for subject_list_data query
                resultSet.close();
                statement.close();
                connection.close();
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }

            return subjectDataList;
        }


        @Override
        protected void onPostExecute(List<SubjectData> subjectDataList) {
            if (subjectDataList == null) {
                // Handle the null case
                return;
            }


            // Remove all views from the studentLayoutGetSubjectList before adding new ones
            studentLayoutGetSubjectList.removeAllViews();

            // Iterate through the list of subject data and create UI elements for each subject
            for (SubjectData subjectData : subjectDataList) {
                // Create a new CardView to represent the subject
                CardView subjectCardView = new CardView(StudentSubjectEnrolledShow.this);
                CardView.LayoutParams cardLayoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                cardLayoutParams.setMargins(16, 16, 16, 16);
                subjectCardView.setLayoutParams(cardLayoutParams);
                subjectCardView.setRadius(20);
                subjectCardView.setCardElevation(20);
                subjectCardView.setPadding(20, 20, 20, 20);
                subjectCardView.setCardBackgroundColor(ContextCompat.getColor(StudentSubjectEnrolledShow.this, R.color.blue_300));

                // Create a LinearLayout to hold the subject details
                LinearLayout subjectLinearLayout = new LinearLayout(StudentSubjectEnrolledShow.this);
                LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                subjectLinearLayout.setLayoutParams(linearLayoutParams);
                subjectLinearLayout.setOrientation(LinearLayout.VERTICAL);
                subjectLinearLayout.setPadding(20, 20, 20, 20);

                // Create a TextView for the subject name
                TextView subjectNameTextView = new TextView(StudentSubjectEnrolledShow.this);
                LinearLayout.LayoutParams subjectNameLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                subjectNameLayoutParams.setMargins(0, 0, 0, 16);
                subjectNameTextView.setLayoutParams(subjectNameLayoutParams);
                subjectNameTextView.setTextSize(20);
                subjectNameTextView.setText(subjectData.getSubjectName());

                // Create a TextView for the teacher name
                TextView teacherNameTextView = new TextView(StudentSubjectEnrolledShow.this);
                LinearLayout.LayoutParams teacherNameLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                teacherNameLayoutParams.setMargins(0, 0, 0, 8);
                teacherNameTextView.setLayoutParams(teacherNameLayoutParams);
                teacherNameTextView.setText(subjectData.getTeacherName());

                // Create a TextView for the semester and semestral value
                TextView semesterTextView = new TextView(StudentSubjectEnrolledShow.this);
                LinearLayout.LayoutParams semesterLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                semesterLayoutParams.setMargins(0, 0, 0, 8);
                semesterTextView.setLayoutParams(semesterLayoutParams);
                semesterTextView.setText(String.format("Semester: %s, Semestral Value: %s", subjectData.getSemestral(), subjectData.getSemestralValue()));

                // Create a TextView for the quarter type
                TextView quarterTypeTextView = new TextView(StudentSubjectEnrolledShow.this);
                LinearLayout.LayoutParams quarterTypeLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                quarterTypeLayoutParams.setMargins(0, 0, 0, 8);
                quarterTypeTextView.setLayoutParams(quarterTypeLayoutParams);
                quarterTypeTextView.setText(String.format("Quarter Type: %s", subjectData.getQuarterType()));


                // Create a TextView for the date created
                TextView dateCreatedTextView = new TextView(StudentSubjectEnrolledShow.this);
                LinearLayout.LayoutParams dateCreatedLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                dateCreatedLayoutParams.setMargins(0, 0, 0, 8);
                dateCreatedTextView.setLayoutParams(dateCreatedLayoutParams);
                dateCreatedTextView.setText(String.format("Created on: %s", subjectData.getDateCreated()));

                // Add the TextViews to the subjectLinearLayout
                subjectLinearLayout.addView(subjectNameTextView);
                subjectLinearLayout.addView(teacherNameTextView);
                subjectLinearLayout.addView(semesterTextView);
                subjectLinearLayout.addView(quarterTypeTextView);

                subjectLinearLayout.addView(dateCreatedTextView);

                // Add the subjectLinearLayout to the subjectCardView
                subjectCardView.addView(subjectLinearLayout);

                // Add the subjectCardView to the parent layout
                studentLayoutGetSubjectList.addView(subjectCardView);

                String tableName = "subject_" + subjectData.getUniqueCode();
                SharedPreferences sharedPreferences = getSharedPreferences("students_lrn", Context.MODE_PRIVATE);
                String lrn = sharedPreferences.getString("lrn", "");

                // Set the OnClickListener
                subjectCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent intent = new Intent(StudentSubjectEnrolledShow.this, StudentsGradesStatusViewer.class);
                        intent.putExtra("studentLRN", lrn); // Replace "lrn" with the actual key for LRN
                        intent.putExtra("tableName", tableName); // Replace "tableName" with the actual key for table name
                        startActivity(intent);


                        // Pass the table name and LRN to the FetchGradesTask
                        FetchGradesTask fetchGradesTask = new FetchGradesTask(tableName, lrn);
                        fetchGradesTask.execute();
                    }
                });


            }

            swipeRefreshLayout.setRefreshing(false);
            refreshing = false; // Set the flag to indicate refresh is complete


        }


    }




















    private class FetchGradesTask extends AsyncTask<Void, Void, String> {
        private String tableName;
        private String lrn;

        public FetchGradesTask(String tableName, String lrn) {
            this.tableName = tableName;
            this.lrn = lrn;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String grades = null;
            String status = null;

            try {
                // Establish a database connection
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));


                // Execute a SQL query to retrieve the grades and status corresponding to the LRN from the generated table
                PreparedStatement statement = connection.prepareStatement("SELECT 1st_quarter_grades, 1st_quarter_status FROM " + tableName + " WHERE lrn = ?");
                statement.setString(1, lrn); // Set the parameter value for LRN
                ResultSet resultSet = statement.executeQuery();

                // Check if a result is found
                if (resultSet.next()) {
                    // Retrieve the grades and status from the result set
                    grades = resultSet.getString("1st_quarter_grades");
                    status = resultSet.getString("1st_quarter_status");
                }

                // ...
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return grades + "|" + status; // Concatenate grades and status using a delimiter for further processing
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                String[] data = result.split("\\|");
                String grades = data[0];
                String status = data[1];

                // Display the grades and status in a toast message
                Toast.makeText(StudentSubjectEnrolledShow.this, "Grades: " + grades + "\nStatus: " + status, Toast.LENGTH_SHORT).show();
            } else {
                // Handle the case when grades and status are not found
            }
        }

        // ...
    }


}
