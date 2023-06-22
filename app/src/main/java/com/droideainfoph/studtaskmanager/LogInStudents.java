package com.droideainfoph.studtaskmanager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LogInStudents extends AppCompatActivity implements DatabaseConnectionChecker.ConnectionCheckListener{

    private EditText Edittext_lrn;
    private Button log_in_students;
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
        setContentView(R.layout.activity_log_in_students);

        log_in_students = findViewById(R.id.login_student_btn);
        Edittext_lrn = findViewById(R.id.lrn);




        log_in_students.setOnClickListener(view -> {


            // Get SharedPreferences instance
            input_extension = getSharedPreferences("student_pref", MODE_PRIVATE);

            String username = input_extension.getString("username", "");
            String password = input_extension.getString("password", "");
            String school_id = input_extension.getString("school_id","");

            String my_lrn = Edittext_lrn.getText().toString();


            if (my_lrn.length() != 12) {
                Toast.makeText(LogInStudents.this, "Your LRN is invalid", Toast.LENGTH_SHORT).show();
            } else {



                // Show loading dialog for checking database connection
                ProgressDialog loadingDialog = new ProgressDialog(LogInStudents.this);
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
                        new LogInStudents.LrnCheckTask(username, password, school_id, my_lrn).execute();

                    } else {
                        runOnUiThread(() -> {
                            // Database connection failed
                            Toast.makeText(LogInStudents.this, "Unable to establish database connection", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(LogInStudents.this, "Maximum retry count reached", Toast.LENGTH_SHORT).show();
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


























    // AsyncTask to check if username exists in database
    @SuppressLint("StaticFieldLeak")
    private class LrnCheckTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final String password;
        private final String school_id;
        private final String lrn;

        private LrnCheckTask(String username, String password, String school_id, String my_lrn) {
            this.username = username;
            this.password = password;
            this.school_id = school_id;
            this.lrn = my_lrn;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver", true, getClass().getClassLoader());
                String sql = "SELECT COUNT(*) FROM studtask_user_student WHERE username = ? AND password = ? AND school_id = ? AND lrn = ?";

                // Establish database connection and check if username exists
                try (Connection conn = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, school_id);
                    stmt.setString(4, lrn);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 1) {
                            // User found, retrieve first name and last name
                            String retrieveUserSql = "SELECT lrn FROM studtask_user_student where username = ? AND password = ?";
                            try (PreparedStatement retrieveStmt = conn.prepareStatement(retrieveUserSql)) {
                                retrieveStmt.setString(1, username);
                                retrieveStmt.setString(2, password);

                                try (ResultSet userRs = retrieveStmt.executeQuery()) {
                                    if (userRs.next()) {
                                        String getLRN = userRs.getString("lrn");

                                        // Save first name and last name to SharedPreferences
                                        saveToSharedPreferences(getLRN);

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

        private void saveToSharedPreferences(String getLRN) {
            // Save first name and last name to SharedPreferences

            SharedPreferences sharedPreferences = getSharedPreferences("ChatBasedName", Context.MODE_PRIVATE);

            // Get the SharedPreferences editor for editing
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Save the first name and last name to SharedPreferences
            editor.putString("senderChatCode", getLRN);

            Log.d("Debug", "Sender Code: " + getLRN);

            // Commit the changes
            editor.apply();
        }

        @Override
        protected void onPostExecute(Boolean isValidUser) {
            if (isValidUser != null && isValidUser) {
                Toast.makeText(LogInStudents.this, "Welcome Back", Toast.LENGTH_SHORT).show();
                String userRole = "student";

                // Start the next activity
                Intent intent = new Intent(LogInStudents.this, MainActivity.class);
                intent.putExtra("USER_ROLE", userRole); // Add the userRole value as an extra with a key "USER_ROLE"

                SharedPreferences sharedPreferences = getSharedPreferences("students_lrn", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("lrn", lrn);
                editor.apply();

                // Clear the data from SharedPreferences
                editor = input_extension.edit();
                editor.clear();
                editor.apply();

                startActivity(intent);
                finish();
            } else {
                // Display a toast message
                Toast.makeText(LogInStudents.this, "Invalid user", Toast.LENGTH_SHORT).show();
            }
        }

    }





}