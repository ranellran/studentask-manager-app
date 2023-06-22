package com.droideainfoph.studtaskmanager;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public class LogInMain extends AppCompatActivity implements DatabaseConnectionChecker.ConnectionCheckListener {

    private TextView no_account;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText schoolIdEditText;

    private Button student_proceed;
    private Button teacher_proceed;

    private ProgressDialog loadingDialog;
    private AlertDialog retryDialog;

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    private boolean isConnected = false;
    private int retryCount = 0;
    private Handler timeoutHandler = new Handler();

    private static final int DELAY_TIME_MS = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_main);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        schoolIdEditText = findViewById(R.id.school_id);
        student_proceed = findViewById(R.id.proceed_student_btn);
        teacher_proceed = findViewById(R.id.proceed_teacher_btn);
        no_account = findViewById(R.id.register_textview);

        usernameEditText.setSingleLine(true);

        // Set the input type to show password characters as dots
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Create the loading dialog
        loadingDialog = new ProgressDialog(LogInMain.this);
        loadingDialog.setMessage("Checking database connection...");
        loadingDialog.setCancelable(false);

        // Set click listener for "No Account?" text
        no_account.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpMain.class);
            startActivity(intent);
        });


        // Delayed execution using a Handler
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                verifyStoragePermissionWithDelay(LogInMain.this);
            }
        }, DELAY_TIME_MS);





        // Set click listener for "Student Proceed" button
        student_proceed.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String schoolIDStr = schoolIdEditText.getText().toString();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(schoolIDStr)) {
                Toast.makeText(LogInMain.this, "Please complete required field", Toast.LENGTH_SHORT).show();
                return;
            }

            int schoolID = Integer.parseInt(schoolIDStr);

            // Show loading dialog for checking database connection
            ProgressDialog loadingDialog = new ProgressDialog(LogInMain.this);
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
                    new StudentTask(username, password, schoolID).execute();
                } else {
                    runOnUiThread(() -> {
                        // Database connection failed
                        Toast.makeText(LogInMain.this, "Unable to establish database connection", Toast.LENGTH_SHORT).show();
                        showRetryDialog();
                    });
                }
            }, dbUrl, dbUsername, dbPassword);

            connectionChecker.startConnectionCheck();
        });

        // Set click listener for "Teacher Proceed" button
        teacher_proceed.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String schoolIDStr = schoolIdEditText.getText().toString();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(schoolIDStr)) {
                Toast.makeText(LogInMain.this, "Please complete required field", Toast.LENGTH_SHORT).show();
                return;
            }

            int schoolID = Integer.parseInt(schoolIDStr);

            // Show loading dialog for checking database connection
            ProgressDialog loadingDialog = new ProgressDialog(LogInMain.this);
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
                    new TeacherTask(username, password, schoolID).execute();
                } else {
                    runOnUiThread(() -> {
                        // Database connection failed
                        Toast.makeText(LogInMain.this, "Unable to establish database connection", Toast.LENGTH_SHORT).show();
                        showRetryDialog();
                    });
                }
            }, dbUrl, dbUsername, dbPassword);

            connectionChecker.startConnectionCheck();
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
            Toast.makeText(LogInMain.this, "Maximum retry count reached", Toast.LENGTH_SHORT).show();
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

    public static void verifyStoragePermissionWithDelay(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivity(intent);
            }
        }
    }














    private class StudentTask extends AsyncTask<Void, Void, Boolean> {
        private final String username;
        private final String password;
        private final int schoolID;

        private StudentTask(String username, String password, int schoolID) {
            this.username = username;
            this.password = password;
            this.schoolID = schoolID;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");

                // Check if the user exists
                String checkUserSql = "SELECT COUNT(*) FROM (SELECT username, password, school_id FROM studtask_user_teacher UNION ALL SELECT username, password, school_id FROM studtask_user_student) u WHERE username = ? AND password = ? AND school_id = ?";

                // Establish database connection and check if username exists
                try (Connection conn = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));
                     PreparedStatement stmt = conn.prepareStatement(checkUserSql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setInt(3, schoolID);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 1) {
                            // User found, retrieve first name and last name
                            String retrieveUserSql = "SELECT firstname, lastname FROM (SELECT username, password, school_id, firstname, lastname FROM studtask_user_teacher UNION ALL SELECT username, password, school_id, firstname, lastname FROM studtask_user_student) u WHERE username = ? AND password = ? AND school_id = ?";
                            try (PreparedStatement retrieveStmt = conn.prepareStatement(retrieveUserSql)) {
                                retrieveStmt.setString(1, username);
                                retrieveStmt.setString(2, password);
                                retrieveStmt.setInt(3, schoolID);

                                try (ResultSet userRs = retrieveStmt.executeQuery()) {
                                    if (userRs.next()) {
                                        String firstname = userRs.getString("firstname");
                                        String lastname = userRs.getString("lastname");

                                        // Save first name and last name to SharedPreferences
                                        saveToSharedPreferences(firstname, lastname);

                                        return true;
                                    }
                                }
                            } catch (SQLException e) {
                                // Handle the exception
                                return false;
                            }
                        }
                    }
                } catch (SQLException e) {
                    // Handle the exception
                    return false;
                }
            } catch (ClassNotFoundException e) {
                // Handle the exception
                return false;
            }

            return false;
        }

        private void saveToSharedPreferences(String firstname, String lastname) {
            // Save first name and last name to SharedPreferences

            SharedPreferences sharedPreferences = getSharedPreferences("ChatBasedName", Context.MODE_PRIVATE);

            // Get the SharedPreferences editor for editing
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Save the first name and last name to SharedPreferences
            editor.putString("Firstname", firstname);
            editor.putString("Lastname", lastname);

            // Commit the changes
            editor.apply();
        }



        @Override
        protected void onPostExecute(Boolean isValidUser) {
            if (isValidUser != null && isValidUser.booleanValue()) {

                // store the data temporarily in shared preferences
                SharedPreferences.Editor editor = getSharedPreferences("student_pref", MODE_PRIVATE).edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.putString("school_id", String.valueOf(schoolID));
                editor.apply();

                // Start the next activity
                Intent intent = new Intent(LogInMain.this, LogInStudents.class);
                // Clear the EditText fields
                usernameEditText.setText("");
                passwordEditText.setText("");
                schoolIdEditText.setText("");

                startActivity(intent);

            } else {
                // Display a toast message
                Toast.makeText(LogInMain.this, "Invalid user", Toast.LENGTH_SHORT).show();
            }

        }
    }


















    private class TeacherTask extends AsyncTask<Void, Void, Boolean> {
        private final String username;
        private final String password;
        private final int schoolID;

        private TeacherTask(String username, String password, int schoolID) {
            this.username = username;
            this.password = password;
            this.schoolID = schoolID;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");

                // Check if the user exists
                String checkUserSql = "SELECT COUNT(*) FROM (SELECT username, password, school_id FROM studtask_user_teacher UNION ALL SELECT username, password, school_id FROM studtask_user_student) u WHERE username = ? AND password = ? AND school_id = ?";

                // Establish database connection and check if username exists
                try (Connection conn = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));
                     PreparedStatement stmt = conn.prepareStatement(checkUserSql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setInt(3, schoolID);


                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 1) {
                            // User found, retrieve first name and last name
                            String retrieveUserSql = "SELECT firstname, lastname FROM (SELECT username, password, school_id, firstname, lastname FROM studtask_user_teacher UNION ALL SELECT username, password, school_id, firstname, lastname FROM studtask_user_student) u WHERE username = ? AND password = ? AND school_id = ?";
                            try (PreparedStatement retrieveStmt = conn.prepareStatement(retrieveUserSql)) {
                                retrieveStmt.setString(1, username);
                                retrieveStmt.setString(2, password);
                                retrieveStmt.setInt(3, schoolID);

                                try (ResultSet userRs = retrieveStmt.executeQuery()) {
                                    if (userRs.next()) {
                                        String firstname = userRs.getString("firstname");
                                        String lastname = userRs.getString("lastname");

                                        // Save first name and last name to SharedPreferences
                                        saveToSharedPreferences(firstname, lastname);

                                        return true;
                                    }
                                }
                            } catch (SQLException e) {
                                // Handle the exception
                                return false;
                            }
                        }
                    }


                } catch (SQLException e) {
                    // Handle the exception
                    return false;
                }
            } catch (ClassNotFoundException e) {
                // Handle the exception
                return false;
            }

            return false;
        }

        private void saveToSharedPreferences(String firstname, String lastname) {
            // Save first name and last name to SharedPreferences

            SharedPreferences sharedPreferences = getSharedPreferences("ChatBasedName", Context.MODE_PRIVATE);

            // Get the SharedPreferences editor for editing
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Save the first name and last name to SharedPreferences
            editor.putString("Firstname", firstname);
            editor.putString("Lastname", lastname);

            // Commit the changes
            editor.apply();
        }



        @Override
        protected void onPostExecute(Boolean isValidUser) {

            if (isValidUser != null && isValidUser.booleanValue()) {
                // store the data temporarily in shared preferences
                SharedPreferences.Editor editor = getSharedPreferences("teacher_pref", MODE_PRIVATE).edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.putString("school_id", String.valueOf(schoolID));
                editor.apply();

                // Check if the current activity is still running before starting the next activity
                if (!isFinishing()) {
                    Intent intent = new Intent(LogInMain.this, LogInTeacher.class);
                    usernameEditText.setText("");
                    passwordEditText.setText("");
                    schoolIdEditText.setText("");
                    startActivity(intent);

                }
            } else {
                // Display a toast message
                Toast.makeText(LogInMain.this, "Invalid user", Toast.LENGTH_SHORT).show();
            }




        }
    }








}
