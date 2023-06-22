package com.droideainfoph.studtaskmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class SignUpTeacher extends AppCompatActivity implements DatabaseConnectionChecker.ConnectionCheckListener{

    private Button generateTeacherCodeButton;
    private Button sign_up_btn;
    private TextView teacherCodeTextView;
    private SharedPreferences input_extension;
    String userType = "Teacher";
    private String generatedCode;





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
        setContentView(R.layout.activity_sign_up_teacher);


        teacherCodeTextView = findViewById(R.id.teacher_code_text_view);
        sign_up_btn = findViewById(R.id.sign_up_teacher);

        // Call generateUniqueCode() method to generate a unique code when the activity is created
        generateUniqueCode();



        sign_up_btn.setOnClickListener(view -> {

            // Get SharedPreferences instance
            input_extension = getSharedPreferences("teacher_preferences", MODE_PRIVATE);

            String lastname = input_extension.getString("lastname", "");
            String firstname = input_extension.getString("firstname", "");
            String username = input_extension.getString("username", "");
            String password = input_extension.getString("password", "");
            String school_id = input_extension.getString("school_id", "");

            String t_code = generatedCode;






            // Show loading dialog for checking database connection
            ProgressDialog loadingDialog = new ProgressDialog(SignUpTeacher.this);
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
                    new SignUpTeacher.SendDataToDatabaseTask().execute(lastname, firstname, username, password, school_id, t_code);

                } else {
                    runOnUiThread(() -> {
                        // Database connection failed
                        Toast.makeText(SignUpTeacher.this, "Unable to establish database connection", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(SignUpTeacher.this, "Maximum retry count reached", Toast.LENGTH_SHORT).show();
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
















    private void generateUniqueCode() {
        // Generate a random 4-digit code
        Random random = new Random();
        final int code = random.nextInt(10000 - 1000) + 1000;

        // Check if the code already exists in the database
        new CodeCheckTask().execute(String.valueOf(code));
    }



    @SuppressLint("StaticFieldLeak")
    private class SendDataToDatabaseTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            // Get selected grade from params
            String lastname = params[0];
            String firstname = params[1];
            String username = params[2];
            String password = params[3];
            String school_id = params[4];
            String t_code = params[5];


            // Check if code already exists in database
            try {
                Class.forName(getString(R.string.db_load_mysql), true, getClass().getClassLoader());
                // establish a connection to the MySQL database
                Connection conn = DriverManager.getConnection(getString(R.string.db_url_mysql), "studtask_app", "studtask123");


                // execute an insert query to add the data to the appropriate table
                String query = "INSERT INTO studtask_user_teacher (lastname, firstname, username, password, school_id, user_type, teacher_code) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStmt = conn.prepareStatement(query);

                preparedStmt.setString(1, lastname);
                preparedStmt.setString(2, firstname);
                preparedStmt.setString(3, username);
                preparedStmt.setString(4, password);
                preparedStmt.setString(5, school_id);
                preparedStmt.setString(6, userType);
                preparedStmt.setString(7, t_code);

                preparedStmt.execute();
                // close the connection
                conn.close();

                // Show success message on the UI thread
                runOnUiThread(() -> Toast.makeText(SignUpTeacher.this, "Sign Up Succeed!!!", Toast.LENGTH_SHORT).show());


                // Create an Intent to start the SignUpSuccess activity
                Intent intent = new Intent(SignUpTeacher.this, SignUpSuccess.class);
                // Pass the relevant data as extras
                intent.putExtra("lastname", lastname);
                intent.putExtra("firstname", firstname);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                intent.putExtra("schoolId", school_id);
                intent.putExtra("userType", userType);
                intent.putExtra("teacherCode", t_code);
                startActivity(intent);
                finish();


            } catch (SQLException e) {
                // Show failure message on the UI thread
                runOnUiThread(() -> Toast.makeText(SignUpTeacher.this, "Sign Up Failed!!!", Toast.LENGTH_SHORT).show());

                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class CodeCheckTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            // Check if the code already exists in the database
            boolean codeExists = true;
            while (codeExists) {
                // Perform database check here
                // Replace "myDatabase" with your actual database name
                String query = "SELECT COUNT(*) FROM studtask_user_teacher WHERE teacher_code = " + params[0];
                // Execute the query and check the result
                int count = executeQueryAndGetCount(query);
                codeExists = (count > 0);
                // Generate a new code if the current code already exists
                if (codeExists) {
                    int code = new Random().nextInt(10000 - 1000) + 1000;
                    params[0] = String.valueOf(code);
                } else {
                    // Save the generated code to the class-level variable
                    generatedCode = params[0];
                }
            }
            // Return the result of the database check
            return true;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Boolean result) {
            // Display the unique code in the TextView
            if (result) {
                teacherCodeTextView.setText("Your code is : " + generatedCode);
            } else {
                // Handle the case where a unique code could not be generated
                // This could happen if all possible codes are already in use
                // You could display an error message, or take some other action
            }
        }
    }

    private int executeQueryAndGetCount(String query) {
        int count = 0;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // Create a connection to the database
            conn = DriverManager.getConnection(getString(R.string.db_url_mysql), "studtask_app", "studtask123");

            // Create a statement for executing the query
            stmt = conn.createStatement();

            // Execute the query and get the result set
            rs = stmt.executeQuery(query);

            // Iterate over the result set and count the number of rows
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the result set, statement, and connection
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

        return count;
    }









}