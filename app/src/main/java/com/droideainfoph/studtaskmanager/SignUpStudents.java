package com.droideainfoph.studtaskmanager;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SignUpStudents extends AppCompatActivity implements DatabaseConnectionChecker.ConnectionCheckListener{

    EditText lrn;
    EditText edittext_birthday;
    Spinner students_grade_level;
    Spinner sex_spinner;
    private Button sign_up_btn;
    private EditText editTextLRN;
    String birthdate ;
    private  String user_sex;
    private SharedPreferences input_extension;
    String userType = "Student";













    private ProgressDialog loadingDialog;
    private AlertDialog retryDialog;

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    private boolean isConnected = false;
    private int retryCount = 0;
    private Handler timeoutHandler = new Handler();

    public SignUpStudents() {
    }

    @SuppressLint({"MissingInflatedId", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_students);



        lrn = findViewById(R.id.students_lrn);
        students_grade_level = findViewById(R.id.spinnerGradeLevel);
        edittext_birthday = findViewById(R.id.edittext_select_birthday);
        sex_spinner = findViewById(R.id.spinnerSex);
        editTextLRN = findViewById(R.id.students_lrn);

        sign_up_btn = findViewById(R.id.students_sign_up);

        lrn.setFilters(new InputFilter[] {new InputFilter.LengthFilter(12)});
        lrn.setInputType(InputType.TYPE_CLASS_NUMBER);








        // Set a click listener on the birthdate EditText
        edittext_birthday.setOnClickListener(v -> {
            // Get the current date
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create a DatePickerDialog and show it
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // Set the selected date on the EditText field
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year1, monthOfYear, dayOfMonth);
                        Date date = selectedDate.getTime();

                        // Create a SimpleDateFormat object to format the date string
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        birthdate = dateFormat.format(date);

                        // Set the formatted date string on the EditText field
                        edittext_birthday.setText(birthdate);
                    }, year, month, day);
            datePickerDialog.show();
        });



        // Set Spinner listener to save selected grade to SharedPreferences
        students_grade_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected grade


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });




        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sex_selection, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sex_spinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.grade_levels, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        students_grade_level.setAdapter(adapter2);


        // Set Spinner listener to save selected sex
        sex_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected sex
                user_sex = parent.getItemAtPosition(position).toString();

                Toast.makeText(SignUpStudents.this, user_sex + " : are selected ", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });






        sign_up_btn.setOnClickListener(view -> {



            // Get SharedPreferences instance
            input_extension = getSharedPreferences("student_preferences", MODE_PRIVATE);

            String lastname = input_extension.getString("lastname", "");
            String firstname = input_extension.getString("firstname", "");
            String username = input_extension.getString("username", "");
            String password = input_extension.getString("password", "");
            String school_id = input_extension.getString("school_id", "");


            String lrn = editTextLRN.getText().toString();
            String birthday = edittext_birthday.getText().toString();
            String selectedGrade = students_grade_level.getSelectedItem().toString();
            String user_sex = sex_spinner.getSelectedItem().toString();


            if (lrn.length() != 12) {
                Toast.makeText(SignUpStudents.this, "Your LRN is invalid", Toast.LENGTH_SHORT).show();
            } else {

                // Show loading dialog for checking database connection
                ProgressDialog loadingDialog = new ProgressDialog(SignUpStudents.this);
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
                        new SignUpStudents.SendDataToDatabaseTask().execute(lastname, firstname, username, password, school_id, lrn, birthday, selectedGrade, user_sex);

                    } else {
                        runOnUiThread(() -> {
                            // Database connection failed
                            Toast.makeText(SignUpStudents.this, "Unable to establish database connection", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(SignUpStudents.this, "Maximum retry count reached", Toast.LENGTH_SHORT).show();
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
    private class SendDataToDatabaseTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            // Get selected grade from params
            String lastname = params[0];
            String firstname = params[1];
            String username = params[2];
            String password = params[3];
            String school_id = params[4];
            String lrn = params[5];

            String birthday = params[6];
            String selectedGrade = params[7];
            String user_sex = params[8];

            // Check if LRN already exists in database
            try {
                Class.forName(getString(R.string.db_load_mysql), true, getClass().getClassLoader());
                // establish a connection to the MySQL database
                Connection conn = DriverManager.getConnection(getString(R.string.db_url_mysql), "studtask_app", "studtask123");

                // execute a select query to check if the LRN already exists in the appropriate table
                String query = "SELECT * FROM studtask_user_student WHERE lrn = ?";

                PreparedStatement preparedStmt = conn.prepareStatement(query);

                preparedStmt.setString(1, lrn);

                ResultSet rs = preparedStmt.executeQuery();




                // If LRN already exists, show a message with the user's name and return
                if (rs.next()) {
                    String existingFirstname = rs.getString("firstname");
                    String existingLastname = rs.getString("lastname");

                    // close the connection
                    conn.close();

                    runOnUiThread(() -> Toast.makeText(SignUpStudents.this, "You are already registered as " + existingFirstname + " " + existingLastname, Toast.LENGTH_SHORT).show());

                    return false;
                }

                // close the result set
                rs.close();

                // execute an insert query to add the data to the appropriate table
                query = "INSERT INTO studtask_user_student (lastname, firstname, username, password, school_id, user_type, sex, grade_level, lrn, birthdate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                preparedStmt = conn.prepareStatement(query);
                preparedStmt.setString(1, lastname);
                preparedStmt.setString(2, firstname);
                preparedStmt.setString(3, username);
                preparedStmt.setString(4, password);
                preparedStmt.setString(5, school_id);
                preparedStmt.setString(6, userType);
                preparedStmt.setString(7, user_sex);
                preparedStmt.setString(8, selectedGrade);
                preparedStmt.setString(9, lrn);
                preparedStmt.setString(10, birthday);
                preparedStmt.execute();
                // close the connection
                conn.close();

                // Show success message on the UI thread
                runOnUiThread(() -> Toast.makeText(SignUpStudents.this, "Sign Up Succeed!!!", Toast.LENGTH_SHORT).show());


                // public SharedPreferences teacher_success;

                // Create an Intent to start the SignUpSuccess activity
                Intent intent = new Intent(SignUpStudents.this, SignUpSuccess.class);
                // Pass the relevant data as extras
                intent.putExtra("lastname", lastname);
                intent.putExtra("firstname", firstname);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                intent.putExtra("schoolId", school_id);
                intent.putExtra("userType", userType);
                intent.putExtra("userSex", user_sex);
                intent.putExtra("gradeLevel", selectedGrade);
                intent.putExtra("lrn", lrn);
                intent.putExtra("birthday", birthday);
                startActivity(intent);
                finish();


            } catch (SQLException e) {
                // Show failure message on the UI thread
                runOnUiThread(() -> Toast.makeText(SignUpStudents.this, "Sign Up Failed!!!", Toast.LENGTH_SHORT).show());

                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }













}