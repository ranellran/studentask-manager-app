package com.droideainfoph.studtaskmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LogInTeacher extends AppCompatActivity implements DatabaseConnectionChecker.ConnectionCheckListener {

    Button log_in_teacher;
    EditText code4;
    private SharedPreferences input_extension;
    private SharedPreferences.Editor editor;

    private ProgressDialog loadingDialog;
    private AlertDialog retryDialog;

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    private boolean isConnected = false;
    private int retryCount = 0;
    private Handler timeoutHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_teacher);

        log_in_teacher = findViewById(R.id.login_teacher_btn);
        code4 = findViewById(R.id.teacher_code);



        log_in_teacher.setOnClickListener(view -> {

            String codeIDStr = code4.getText().toString();

            input_extension = getSharedPreferences("teacher_pref", MODE_PRIVATE);

            String username = input_extension.getString("username", "");
            String password = input_extension.getString("password", "");
            String school_id = input_extension.getString("school_id","");


            if (TextUtils.isEmpty(codeIDStr)) {
                code4.setError("Please enter code!!!");
                return;

            }
            int code = Integer.parseInt(codeIDStr);





            // Show loading dialog for checking database connection
            ProgressDialog loadingDialog = new ProgressDialog(LogInTeacher.this);
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
                    new LogInTeacher.CodeCheckTask(username, password, school_id, code).execute();

                } else {
                    runOnUiThread(() -> {
                        // Database connection failed
                        Toast.makeText(LogInTeacher.this, "Unable to establish database connection", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(LogInTeacher.this, "Maximum retry count reached", Toast.LENGTH_SHORT).show();
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


























    @SuppressLint("StaticFieldLeak")
    private class CodeCheckTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final String password;
        private final String school_id;
        private final int code;

        private CodeCheckTask(String username, String password, String school_id, int code) {
            this.username = username;
            this.password = password;
            this.school_id = school_id;
            this.code = code;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver", true, getClass().getClassLoader());
                String sql = "SELECT COUNT(*) FROM studtask_user_teacher WHERE username = ? AND password = ? AND school_id = ? AND teacher_code = ?";

                // Establish database connection and check if username exists
                try (Connection conn = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, school_id);
                    stmt.setInt(4, code);



                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 1) {
                            // User found, retrieve first name and last name
                            String retrieveUserSql = "SELECT teacher_code, username, password FROM studtask_user_teacher where username = ? AND password = ?";
                            try (PreparedStatement retrieveStmt = conn.prepareStatement(retrieveUserSql)) {
                                retrieveStmt.setString(1, username);
                                retrieveStmt.setString(2, password);

                                try (ResultSet userRs = retrieveStmt.executeQuery()) {
                                    if (userRs.next()) {
                                        String getTeacherCode = userRs.getString("teacher_code");
                                        String getUsername = userRs.getString("username");
                                        String getPassword = userRs.getString("password");

                                        // Save first name and last name to SharedPreferences
                                        saveToSharedPreferences(getTeacherCode, getUsername, getPassword);

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
            return null;
        }



        private void saveToSharedPreferences(String getTeacherCode, String getUsername, String getPassword) {
            // Save first name and last name to SharedPreferences

            SharedPreferences sharedPreferences = getSharedPreferences("ChatBasedName", Context.MODE_PRIVATE);

            // Get the SharedPreferences editor for editing
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Save the first name and last name to SharedPreferences
            editor.putString("senderChatCode", getTeacherCode);
            editor.putString("teacher_username", getUsername);
            editor.putString("teacher_password", getPassword);

            Log.d("Debug", "Sender Code: " + getTeacherCode);
            Log.d("Debug", "Teacher Username: " + getUsername);
            Log.d("Debug", "Teacher Password: " + getPassword);

            // Commit the changes
            editor.apply();
        }


        @Override
        protected void onPostExecute(Boolean isValidUser) {
            if (isValidUser != null && isValidUser.booleanValue()) {
                // Code for successful login
                Toast.makeText(LogInTeacher.this, "Welcome Back", Toast.LENGTH_SHORT).show();
                String userRole = "teacher";
                // Start the next activity
                Intent intent = new Intent(LogInTeacher.this, MainActivity.class);
                intent.putExtra("USER_ROLE", userRole); // Add the userRole value as an extra with a key "USER_ROLE"

                // Clear the data from SharedPreferences
                editor = input_extension.edit();
                editor.clear();
                editor.apply();

                startActivity(intent);
                finish();
            } else {
                // Code for unsuccessful login or null value
                Toast.makeText(LogInTeacher.this, "Invalid user", Toast.LENGTH_SHORT).show();
            }
        }

    }



}