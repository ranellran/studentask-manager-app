package com.droideainfoph.studtaskmanager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AddSubjectActivity extends AppCompatActivity implements DatabaseConnectionChecker.ConnectionCheckListener{

    private EditText subjectNameEditText;
    private EditText subjectTeacherNameEditText;
    private Spinner spinnerSemestral;
    private Spinner spinnerSemestralValue;

    String quarterType;
    String semestralType;
    String semestralValue;
    private String uniqueCode;











    private ProgressDialog loadingDialog;
    private AlertDialog retryDialog;

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    private boolean isConnected = false;
    private int retryCount = 0;
    private Handler timeoutHandler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subject);

        subjectNameEditText = findViewById(R.id.subject_edit_text);
        subjectTeacherNameEditText = findViewById(R.id.subject_teacher_name_edit_text);
        spinnerSemestral = findViewById(R.id.spinnerSemester);
        spinnerSemestralValue = findViewById(R.id.semester_selector);


        subjectNameEditText.setSingleLine(true);
        subjectTeacherNameEditText.setSingleLine(true);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.semester, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemestral.setAdapter(adapter);

        spinnerSemestral.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSemester = parent.getItemAtPosition(position).toString();

                if (selectedSemester.equals("No")) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(AddSubjectActivity.this,
                            R.array.semester_selector_items, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSemestralValue.setAdapter(adapter);
                    spinnerSemestralValue.setEnabled(false);
                    quarterType = "Quarter";
                    semestralType = "No";
                } else if (selectedSemester.equals("Yes")) {
                    spinnerSemestralValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedSemesterValue1 = parent.getItemAtPosition(position).toString();
                            if (selectedSemesterValue1.equals("1")) {
                                semestralValue = "1st Sem";
                            } else if (selectedSemesterValue1.equals("2")) {
                                semestralValue = "2nd Sem";
                            } else if (selectedSemesterValue1.equals("None")) {
                                semestralValue = "None";
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Handle nothing selected event
                        }
                    });

                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(AddSubjectActivity.this,
                            R.array.semester_selector_items, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSemestralValue.setAdapter(adapter);
                    spinnerSemestralValue.setEnabled(true);
                    quarterType = "Semestral";
                    semestralType = "Yes";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle nothing selected event
            }
        });


        Button saveButton = findViewById(R.id.subject_save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Show loading dialog for checking database connection
                ProgressDialog loadingDialog = new ProgressDialog(AddSubjectActivity.this);
                loadingDialog.setMessage("Checking database connection...");
                loadingDialog.setCancelable(true);
                loadingDialog.show();

                // Check database connection
                DatabaseConnectionChecker connectionChecker = new DatabaseConnectionChecker(isConnected -> {
                    // Dismiss the loading dialog
                    loadingDialog.dismiss();
                    Log.d("Debug", "Started connection check...");

                    if (isConnected) {
                        // Database connection is successful
                        // Send data to MySQL database using AsyncTask
                        // Retrieve the Intent that started the activity
                        Intent intent = getIntent();

                        String subjectName = subjectNameEditText.getText().toString(); //name
                        String subjectTeacherName = subjectTeacherNameEditText.getText().toString(); //teacher name
                        String semestralValueGet = semestralValue; // 1st and 2nd Sem
                        String quarterTypeGet = quarterType; //quarter or not
                        String semestralTypeGet = semestralType; //yes or no
                        String uniqueCodeGet = uniqueCode;
                        String tableName = "subject_" + uniqueCodeGet + "";

                        ArrayList<String> lrnList = intent.getStringArrayListExtra("lrnList");


                        int gradeLevelIDBelong = intent.getIntExtra("id", 0); // 0 is the default value if "id" is not found
                        String gradeLevelBelong = intent.getStringExtra("gradeLevel");
                        String dateCreatedBelong = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

                        if (uniqueCode == null) {
                            GenerateUniqueCodeTask generateCodeTask = new GenerateUniqueCodeTask() {
                                @Override
                                protected void onPostExecute(String code) {
                                    uniqueCode = code;

                                    if (subjectName != null && subjectTeacherName != null && semestralValueGet != null &&
                                            quarterTypeGet != null && semestralTypeGet != null && uniqueCodeGet != null && tableName != null ) {

                                        CreateDatabaseSubjectTable createTableTask = new CreateDatabaseSubjectTable(tableName) {
                                            @Override
                                            protected void onPostExecute(Void aVoid) {
                                                SaveSubjectDataTask saveSubjectDataTask = new SaveSubjectDataTask();
                                                saveSubjectDataTask.createSubjectTable(
                                                        subjectName,
                                                        subjectTeacherName,
                                                        semestralValueGet,
                                                        quarterTypeGet,
                                                        semestralTypeGet,
                                                        uniqueCodeGet,
                                                        tableName,
                                                        lrnList,
                                                        gradeLevelIDBelong,
                                                        gradeLevelBelong,
                                                        dateCreatedBelong
                                                );
                                                saveSubjectDataTask.execute();
                                            }
                                        };
                                        createTableTask.execute();

                                    } else {
                                        // Handle the case when any of the variables is null
                                        Toast.makeText(AddSubjectActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            };
                            generateCodeTask.execute();
                        } else {
                            if (subjectName != null && subjectTeacherName != null && semestralValueGet != null &&
                                    quarterTypeGet != null && semestralTypeGet != null && uniqueCodeGet != null && tableName != null ) {

                                CreateDatabaseSubjectTable createTableTask = new CreateDatabaseSubjectTable(tableName) {
                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        SaveSubjectDataTask saveSubjectDataTask = new SaveSubjectDataTask();
                                        saveSubjectDataTask.createSubjectTable(
                                                subjectName,
                                                subjectTeacherName,
                                                semestralValueGet,
                                                quarterTypeGet,
                                                semestralTypeGet,
                                                uniqueCodeGet,
                                                tableName,
                                                lrnList,
                                                gradeLevelIDBelong,
                                                gradeLevelBelong,
                                                dateCreatedBelong
                                        );
                                        saveSubjectDataTask.execute();
                                    }
                                };
                                createTableTask.execute();

                            } else {
                                // Handle the case when any of the variables is null
                                Toast.makeText(AddSubjectActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                            }
                        }



                    } else {
                        runOnUiThread(() -> {
                            // Database connection failed
                            Toast.makeText(AddSubjectActivity.this, "Unable to establish database connection", Toast.LENGTH_SHORT).show();
                            showRetryDialog();
                        });
                    }
                }, dbUrl, dbUsername, dbPassword);

                connectionChecker.startConnectionCheck();

            }
        });


// Provide the database connection information
        dbUrl = getString(R.string.db_url_mysql);
        dbUsername = getString(R.string.db_username);
        dbPassword = getString(R.string.db_password);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        if (retryDialog != null && retryDialog.isShowing()) {
            retryDialog.dismiss();
        }
        cancelTimeoutTimer();
    }

    @Override
    public void onConnectionCheckComplete(boolean isConnected) {
        this.isConnected = isConnected;
        loadingDialog.dismiss();
        cancelTimeoutTimer();
        Log.d("Debug", "Connection check complete. isConnected: " + isConnected);

        if (!isConnected) {
            showRetryDialog();
        }
    }

    private void showRetryDialog() {
        if (retryDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Connection Failed")
                    .setMessage("Unable to establish database connection. Do you want to retry?")
                    .setPositiveButton("Try Again", (dialog, which) -> {
                        dialog.dismiss();
                        retryConnectionCheck();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    });

            retryDialog = builder.create();
            retryDialog.setCancelable(false);
        }

        if (!retryDialog.isShowing()) {
            retryDialog.show();
        }
    }

    private void retryConnectionCheck() {
        if (retryCount < 1) {
            retryCount++;
            loadingDialog.show();

            DatabaseConnectionChecker connectionChecker = new DatabaseConnectionChecker(this, dbUrl, dbUsername, dbPassword);
            connectionChecker.startConnectionCheck();
            startTimeoutTimer();
            Log.d("Debug", "Retry connection check...");
        } else {
            Toast.makeText(AddSubjectActivity.this, "Maximum retry count reached", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimeoutTimer() {
        timeoutHandler.postDelayed(() -> {
            onConnectionCheckComplete(false);
            Log.d("Debug", "Connection check timeout...");
        }, 3000);
    }

    private void cancelTimeoutTimer() {
        timeoutHandler.removeCallbacksAndMessages(null);
    }













    private class CreateDatabaseSubjectTable extends AsyncTask<Void, Void, Void> {
        private String tableName;

        public CreateDatabaseSubjectTable(String tableName) {
            this.tableName = tableName;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Create a connection to the MySQL database
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                // Prepare the SQL statement to create a new table
                String subjectTableQuery = "CREATE TABLE `" + tableName + "` ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "subject_name VARCHAR(255) NOT NULL,"
                        + "subject_teacher_name VARCHAR(255) NOT NULL,"
                        + "semestral_value VARCHAR(255) NOT NULL,"
                        + "quarter_type VARCHAR(255) NOT NULL,"
                        + "semestral_type VARCHAR(255) NOT NULL,"
                        + "lrn VARCHAR(12) UNIQUE NOT NULL,"
                        + "unique_code VARCHAR(255) NOT NULL,"
                        + "table_name VARCHAR(255) NOT NULL,"
                        + "grade_level_id_belong VARCHAR(255) NOT NULL,"
                        + "grade_level_belong VARCHAR(255) NOT NULL,"
                        + "date_created_belong VARCHAR(255) NOT NULL,"
                        + "1st_quarter_grades VARCHAR(100),"
                        + "1st_quarter_status VARCHAR(20) DEFAULT 'Tentative',"
                        + "2nd_quarter_grades VARCHAR(100),"
                        + "2nd_quarter_status VARCHAR(20) DEFAULT 'Tentative',"
                        + "3rd_quarter_grades VARCHAR(100),"
                        + "3rd_quarter_status VARCHAR(20) DEFAULT 'Tentative',"
                        + "4th_quarter_grades VARCHAR(100),"
                        + "4th_quarter_status VARCHAR(20) DEFAULT 'Tentative'"
                        + ")";

                // Create the new table
                Statement statement = connection.createStatement();
                statement.executeUpdate(subjectTableQuery);

                // Close the database connection and statement
                statement.close();
                connection.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Display a toast message indicating successful table creation
            Toast.makeText(getApplicationContext(), "Table created successfully", Toast.LENGTH_SHORT).show();
        }
    }





















    private class SaveSubjectDataTask extends AsyncTask<Void, Void, Void> {
        private String subjectName;
        private String subjectTeacherName;
        private String semestralValue;
        private String quarterType;
        private String semestralType;
        private String uniqueCode;
        private String tableName;
        private ArrayList<String> lrnList;
        private int gradeLevelIDBelong;
        private String gradeLevelBelong;
        private String dateCreatedBelong;

        private void createSubjectTable(String subjectName, String subjectTeacherName, String semestralValue,
                                        String quarterType, String semestralType, String uniqueCode,
                                        String tableName, ArrayList<String> lrnList, int gradeLevelIDBelong,
                                        String gradeLevelBelong, String dateCreatedBelong) {
            this.subjectName = subjectName;
            this.subjectTeacherName = subjectTeacherName;
            this.semestralValue = semestralValue;
            this.quarterType = quarterType;
            this.semestralType = semestralType;
            this.uniqueCode = uniqueCode;
            this.tableName = tableName;
            this.lrnList = lrnList;
            this.gradeLevelIDBelong = gradeLevelIDBelong;
            this.gradeLevelBelong = gradeLevelBelong;
            this.dateCreatedBelong = dateCreatedBelong;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Execute the code to save the data into the database
            try {
                // Establish a connection to the database
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                // Create the SQL statement to create the new table
                String insertIntoTeacherDashboardDataQuery =
                        "INSERT INTO subject_list_data " +
                                "(id_subject_belong, grade_level_subject_belong, subject_name, teacher_name, semestral, semestral_value, quarter_type, unique_code, date_created) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";


                // Execute the insert query
                PreparedStatement statement = connection.prepareStatement(insertIntoTeacherDashboardDataQuery);

                // Set the values for the prepared statement
                statement.setInt(1, gradeLevelIDBelong);
                statement.setString(2, gradeLevelBelong);
                statement.setString(3, subjectName);
                statement.setString(4, subjectTeacherName);
                statement.setString(5, semestralType);
                statement.setString(6, semestralValue);
                statement.setString(7, quarterType);
                statement.setString(8, uniqueCode);
                statement.setString(9, dateCreatedBelong);

                // Execute the SQL statement
                statement.executeUpdate();


                // Insert the data into the newly created table
                String insertQuery = "INSERT INTO " + tableName + " (subject_name, subject_teacher_name, semestral_value, quarter_type, semestral_type, unique_code, table_name, lrn, grade_level_id_belong, grade_level_belong, date_created_belong) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

                Log.d("Debug", "LRN List:");
                for (String lrn : lrnList) {
                    Log.d("Debug", lrn);
                }


                // Iterate over the lrnList and insert each value
                for (String lrn : lrnList) {
                    // Set the values for the prepared statement
                    preparedStatement.setString(1, subjectName);
                    preparedStatement.setString(2, subjectTeacherName);
                    preparedStatement.setString(3, semestralValue);
                    preparedStatement.setString(4, quarterType);
                    preparedStatement.setString(5, semestralType);
                    preparedStatement.setString(6, uniqueCode);
                    preparedStatement.setString(7, tableName);
                    preparedStatement.setString(8, lrn);
                    preparedStatement.setInt(9, gradeLevelIDBelong);
                    preparedStatement.setString(10, gradeLevelBelong);
                    preparedStatement.setString(11, dateCreatedBelong);

                    // Execute the SQL statement
                    preparedStatement.executeUpdate();
                }



                // Close the resources
                preparedStatement.close();
                statement.close();
                connection.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Called when the background task is completed
            // You can perform any UI updates or additional logic here

            // Navigate back to the TeacherSubjectView activity
            Intent intent = new Intent(AddSubjectActivity.this, TeacherSubjectView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            // Execute the RefreshSubjectViewUIAsyncTask in the TeacherSubjectView activity
            if (getParent() instanceof TeacherSubjectView) {
                TeacherSubjectView parentActivity = (TeacherSubjectView) getParent();
                parentActivity.refreshSubjectViewUI();
            }
        }


    }

















    private class GenerateUniqueCodeTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String code;
            boolean codeExists;
            do {
                code = generateRandomCode();
                codeExists = checkCodeExists(code);
            } while (codeExists);
            return code;
        }


    }


    private String generateRandomCode() {
        final String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lowerCase = upperCase.toLowerCase(Locale.getDefault());
        final String digits = "0123456789";
        final String alphanumeric = upperCase + lowerCase + digits;
        final int codeLength = 8;

        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < codeLength; i++) {
            int index = random.nextInt(alphanumeric.length());
            char character = alphanumeric.charAt(index);
            codeBuilder.append(character);
        }
        return codeBuilder.toString();
    }

    private boolean checkCodeExists(String code) {
        boolean codeExists = false;
        String query = "SELECT COUNT(*) FROM subject_list_data WHERE unique_code = ?";

        try (Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                getString(R.string.db_username), getString(R.string.db_password));
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    codeExists = count > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return codeExists;
    }
}
