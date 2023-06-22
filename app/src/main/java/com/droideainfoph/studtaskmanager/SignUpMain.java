package com.droideainfoph.studtaskmanager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import java.sql.Statement;

public class SignUpMain extends AppCompatActivity implements DatabaseConnectionChecker.ConnectionCheckListener {


    private TextView have_account;
    private EditText Editlastname;
    private EditText Editfirstname;
    private EditText EditSchoolID;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText password_confirm_EditText;

    private Button student_sign_up;
    private Button teacher_sign_up;
    private SharedPreferences input_extension;










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
        setContentView(R.layout.activity_sign_up_main);

        Editlastname = findViewById(R.id.lastname);
        Editfirstname = findViewById(R.id.firstname);
        usernameEditText = findViewById(R.id.sign_up_username);

        passwordEditText = findViewById(R.id.password);
        password_confirm_EditText = findViewById(R.id.confirm_pass);
        student_sign_up = findViewById(R.id.proceed_student_btn1);
        teacher_sign_up = findViewById(R.id.proceed_teacher_btn1);
        have_account = findViewById(R.id.textViewAlreadyHaveAccount);
        EditSchoolID = findViewById(R.id.sign_up_school_id);


        Editlastname.setSingleLine(true);
        Editfirstname.setSingleLine(true);
        usernameEditText.setSingleLine(true);
        passwordEditText.setSingleLine(true);
        password_confirm_EditText.setSingleLine(true);
        EditSchoolID.setSingleLine(true);


        // Set the input type to show password characters as dots
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password_confirm_EditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);


        EditSchoolID.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
        EditSchoolID.setInputType(InputType.TYPE_CLASS_NUMBER);



        have_account.setOnClickListener(v -> {
            // Create an Intent object that points to the SignUpMain activity
            Intent intent = new Intent(this, LogInMain.class);

            // Start the SignUpMain activity
            startActivity(intent);

        });


        // Get SharedPreferences instance
        input_extension = getSharedPreferences("my_preferences", MODE_PRIVATE);





        student_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lastname = Editlastname.getText().toString().trim();
                String firstname = Editfirstname.getText().toString().trim();
                String schoolID = EditSchoolID.getText().toString().trim();
                String user = usernameEditText.getText().toString().trim();
                String pass = passwordEditText.getText().toString().trim();
                String conPass = password_confirm_EditText.getText().toString().trim();


                if (lastname.isEmpty() ||
                        TextUtils.isEmpty(firstname) ||
                        TextUtils.isEmpty(schoolID) ||

                        TextUtils.isEmpty(user) ||
                        TextUtils.isEmpty(pass)) {
                    Editlastname.setError("Please enter Lastname.");
                    Editfirstname.setError("Please enter Firstname.");
                    EditSchoolID.setError("Please enter School ID.");
                    usernameEditText.setError("Please enter a Valid Username.");
                    passwordEditText.setError("Please enter Password.");
                } else {

                    if (pass.equals(conPass)) {

                        if (EditSchoolID.length() != 6) {
                            Toast.makeText(SignUpMain.this, "Your School ID is invalid", Toast.LENGTH_SHORT).show();
                        } else {

                            // Show loading dialog for checking database connection
                            ProgressDialog loadingDialog = new ProgressDialog(SignUpMain.this);
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
                                    new SignUpMain.CheckUsernameTask(lastname, firstname, schoolID, user, pass).execute();

                                } else {
                                    runOnUiThread(() -> {
                                        // Database connection failed
                                        Toast.makeText(SignUpMain.this, "Unable to establish database connection", Toast.LENGTH_SHORT).show();
                                        showRetryDialog();
                                    });
                                }
                            }, dbUrl, dbUsername, dbPassword);

                            connectionChecker.startConnectionCheck();

                        }

                    } else {
                        // Passwords don't match, show a toast message
                        Toast.makeText(SignUpMain.this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
                    }



                }
            }
        });







        teacher_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lastname = Editlastname.getText().toString().trim();
                String firstname = Editfirstname.getText().toString().trim();
                String schoolID = EditSchoolID.getText().toString().trim();
                String user = usernameEditText.getText().toString().trim();
                String pass = passwordEditText.getText().toString().trim();
                String conPass = password_confirm_EditText.getText().toString().trim();


                if (lastname.isEmpty() ||
                        TextUtils.isEmpty(firstname) ||
                        TextUtils.isEmpty(schoolID) ||

                        TextUtils.isEmpty(user) ||
                        TextUtils.isEmpty(pass)) {
                    Editlastname.setError("Please enter Lastname.");
                    Editfirstname.setError("Please enter Firstname.");
                    EditSchoolID.setError("Please enter School ID.");
                    usernameEditText.setError("Please enter a Valid Username.");
                    passwordEditText.setError("Please enter Password.");
                } else {

                    if (pass.equals(conPass)) {

                        if (EditSchoolID.length() != 6) {
                            Toast.makeText(SignUpMain.this, "Your School ID is invalid", Toast.LENGTH_SHORT).show();
                        } else {
                            // Show loading dialog for checking database connection
                            ProgressDialog loadingDialog = new ProgressDialog(SignUpMain.this);
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
                                    new SignUpMain.CheckUsernameTask2(lastname, firstname, schoolID, user, pass).execute();

                                } else {
                                    runOnUiThread(() -> {
                                        // Database connection failed
                                        Toast.makeText(SignUpMain.this, "Unable to establish database connection", Toast.LENGTH_SHORT).show();
                                        showRetryDialog();
                                    });
                                }
                            }, dbUrl, dbUsername, dbPassword);

                            connectionChecker.startConnectionCheck();

                        }

                    } else {


                        // Passwords don't match, show a toast message
                        Toast.makeText(SignUpMain.this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
                    }



                }
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
            Toast.makeText(SignUpMain.this, "Maximum retry count reached", Toast.LENGTH_SHORT).show();
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
    private class CheckUsernameTask extends AsyncTask<Void, Void, Boolean> {
        private final String lastname;
        private final String firstname;
        private final String schoolID;
        private final String user;
        private final String pass;


        public CheckUsernameTask(String lastname, String firstname, String schoolID, String user, String pass ) {
            this.lastname = lastname;
            this.firstname = firstname;
            this.schoolID = schoolID;
            this.user = user;
            this.pass = pass;

        }

        @Override
        protected Boolean doInBackground(Void... voids) {


            try {
                Class.forName("com.mysql.jdbc.Driver");

                String sql = "SELECT COUNT(*) FROM (SELECT username FROM studtask_user_teacher UNION ALL SELECT username FROM studtask_user_student) u WHERE username = ?";
                try (Connection conn = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, user);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next() && rs.getInt(1) > 0;
                    }
                } catch (SQLException e) {
                    // Handle the exception
                    return false;
                }



            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }



        }

        @Override
        protected void onPostExecute(Boolean exists) {
            if (exists) {

                // Username already taken
                Toast.makeText(SignUpMain.this, "Username is already taken", Toast.LENGTH_SHORT).show();
            } else {

                // store the data temporarily in shared preferences
                SharedPreferences.Editor editor = getSharedPreferences("student_preferences", MODE_PRIVATE).edit();
                editor.putString("lastname", lastname);
                editor.putString("firstname", firstname);
                editor.putString("username", user);
                editor.putString("school_id", schoolID);
                editor.putString("password", pass);

                editor.apply();

                // Check if the current activity is still running before starting the next activity
                if (!isFinishing()) {
                    Intent intent = new Intent(SignUpMain.this, SignUpStudents.class);
                    // Clear the EditText fields
                    Editfirstname.setText("");
                    Editlastname.setText("");
                    usernameEditText.setText("");
                    passwordEditText.setText("");
                    EditSchoolID.setText("");
                    password_confirm_EditText.setText("");
                    startActivity(intent);
                }
            }
        }
    }



    // AsyncTask to check if username exists in database
    @SuppressLint("StaticFieldLeak")
    private class CheckUsernameTask2 extends AsyncTask<Void, Void, Boolean> {
        private final String lastname;
        private final String firstname;
        private final String schoolID;
        private final String user;
        private final String pass;


        public CheckUsernameTask2(String lastname, String firstname, String schoolID, String user, String pass ) {
            this.lastname = lastname;
            this.firstname = firstname;
            this.schoolID = schoolID;
            this.user = user;
            this.pass = pass;

        }

        @Override
        protected Boolean doInBackground(Void... voids) {


            try {
                Class.forName("com.mysql.jdbc.Driver");

                String sql = "SELECT COUNT(*) FROM (SELECT username FROM studtask_user_teacher UNION ALL SELECT username FROM studtask_user_student) u WHERE username = ?";
                try (Connection conn = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, user);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next() && rs.getInt(1) > 0;
                    }
                } catch (SQLException e) {
                    // Handle the exception
                    return false;
                }





            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }



        }

        @Override
        protected void onPostExecute(Boolean exists) {
            if (exists) {

                // Username already taken
                Toast.makeText(SignUpMain.this, "Username is already taken", Toast.LENGTH_SHORT).show();
            } else {

                // store the data temporarily in shared preferences
                SharedPreferences.Editor editor = getSharedPreferences("teacher_preferences", MODE_PRIVATE).edit();
                editor.putString("lastname", lastname);
                editor.putString("firstname", firstname);
                editor.putString("username", user);
                editor.putString("school_id", schoolID);
                editor.putString("password", pass);

                editor.apply();

                // Check if the current activity is still running before starting the next activity
                if (!isFinishing()) {
                    Intent intent = new Intent(SignUpMain.this, SignUpTeacher.class);

                    // Clear the EditText fields
                    Editfirstname.setText("");
                    Editlastname.setText("");
                    usernameEditText.setText("");
                    passwordEditText.setText("");
                    EditSchoolID.setText("");
                    password_confirm_EditText.setText("");

                    startActivity(intent);
                }
            }
        }
    }

}