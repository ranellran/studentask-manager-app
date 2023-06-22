package com.droideainfoph.studtaskmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class TeacherDashboard extends AppCompatActivity {

    // Define the request code constant
    private static final int REQUEST_CODE_ADD_GRADE_LEVEL = 1;
    private LinearLayout gradeLevelLayout;
    private Button addGradeLevelButton;

    // Define the GradeLevel class
    private static class GradeLevel {
        private int id;
        private String gradeLevel;
        private String section;
        private String adviserName;
        private String salutation;
        private String dateCreated;



        public GradeLevel(int id, String gradeLevel, String section, String adviserName, String salutation, String dateCreated) {
            this.id = id;
            this.gradeLevel = gradeLevel;
            this.section = section;
            this.adviserName = adviserName;
            this.salutation = salutation;
            this.dateCreated = dateCreated;
        }



        public int getId() {
            return id;
        }

        public String getGradeLevel() {
            return gradeLevel;
        }

        public String getSection() {
            return section;
        }

        public String getAdviserName() {
            return adviserName;
        }

        public String getSalutation() {
            return salutation;
        }

        public String getDateCreated() {
            return dateCreated;
        }

        public int getGradeLevelId() { return id; }

    }

    // Inside the onActivityResult() method of TeacherDashboardActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_GRADE_LEVEL && resultCode == RESULT_OK) {
            // Get the data returned from the AddGradeLevelActivity
            String gradeLevel = data.getStringExtra("grade_level");
            String section = data.getStringExtra("section");
            String adviserName = data.getStringExtra("adviser_name");
            String salutation = data.getStringExtra("salutation");
            String dateCreated = data.getStringExtra("date_created");

            // Pass the data to the addGradeLevelToDatabase() method
            new AddGradeLevelTask(gradeLevel, section, adviserName, salutation, dateCreated).execute();

        }
    }



    //Main UI thread here
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard2);



        gradeLevelLayout = findViewById(R.id.grade_level_layout);
        addGradeLevelButton = findViewById(R.id.add_grade_level_button);
        // Execute the AsyncTask to retrieve the list of grade levels
        new RefreshGradeLevelUIAsyncTask().execute();

        addGradeLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TeacherDashboard.this, AddGradeLevelActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_GRADE_LEVEL);
            }
        });




        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Perform the back button action

            }
        });






    }

    private void deleteGradeLevel(int gradeLevelId) {
        DeleteGradeLevelTask task = new DeleteGradeLevelTask();
        task.execute(gradeLevelId);
    }


    private class DeleteGradeLevelTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            int gradeLevelId = params[0];
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to delete the grade level with the given ID
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM teacher_dashboard_glevel_data WHERE id = ?");
                statement.setInt(1, gradeLevelId);
                statement.executeUpdate();

                // Close the database resources
                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    private void findCardViewGradeLevel(int gradeLevelId) {
        FindCardViewGradeLevelTask task = new FindCardViewGradeLevelTask();
        task.execute(gradeLevelId);
    }

    private class FindCardViewGradeLevelTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            int gradeLevelId = params[0];

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to retrieve the grade level data with the given ID
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, grade_level, date_created FROM teacher_dashboard_glevel_data WHERE id = ?");
                statement.setInt(1, gradeLevelId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Retrieve the data from the result set
                    int id = resultSet.getInt("id");
                    String gradeLevel = resultSet.getString("grade_level");
                    String dateCreated = resultSet.getString("date_created");

                    // Pass the individual data as separate extras in the Intent
                    Intent intent = new Intent(TeacherDashboard.this, TeacherSubjectView.class);
                    intent.putExtra("id", id);
                    intent.putExtra("gradeLevel", gradeLevel);
                    intent.putExtra("dateCreated", dateCreated);
                    startActivity(intent);
                }

                // Close the database resources
                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }




    /**
     * Adds a new grade level to the database and refreshes the UI
     */
    public class AddGradeLevelTask extends AsyncTask<Void, Void, Boolean> {

        private String mGradeLevel;
        private String mSection;
        private String mAdviserName;
        private String mSalutation;
        private String mDateCreated;

        public AddGradeLevelTask(String gradeLevel, String section, String adviserName, String salutation, String dateCreated) {
            mGradeLevel = gradeLevel;
            mSection = section;
            mAdviserName = adviserName;
            mSalutation = salutation;
            mDateCreated = dateCreated;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                // Connect to the MySQL database
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to insert a new grade level
                String sql = "INSERT INTO teacher_dashboard_glevel_data "
                        + "(grade_level, section, adviser_name, salutation, date_created) "
                        + "VALUES (?, ?, ?, ?, ?)";

                statement = connection.prepareStatement(sql);
                statement.setString(1, mGradeLevel);
                statement.setString(2, mSection);
                statement.setString(3, mAdviserName);
                statement.setString(4, mSalutation);
                statement.setString(5, mDateCreated);

                int rowsAffected = statement.executeUpdate();

                // Check if the insertion was successful
                if (rowsAffected > 0) {
                    success = true;
                }
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            } finally {
                // Close the database resources
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Refresh the UI to display the new grade level
                refreshGradeLevelUI();
            }
        }
    }


    /**
     * Refreshes the UI to display the list of grade levels
     */


    private void refreshGradeLevelUI() {
        // Clear the existing grade level views
        gradeLevelLayout.removeAllViews();

        // Retrieve the updated grade levels from the database and display them again
        new RefreshGradeLevelUIAsyncTask().execute();
    }



    private class RefreshGradeLevelUIAsyncTask extends AsyncTask<Void, Void, List<GradeLevel>> {




        @Override
        protected List<GradeLevel> doInBackground(Void... voids) {
            List<GradeLevel> gradeLevels = new ArrayList<>();


            try {
                // Connect to the MySQL database
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to retrieve the list of grade levels
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM teacher_dashboard_glevel_data");

                // Iterate through the result set and add each grade level to the list
                while (resultSet.next()) {
                    int gradeLevelId = resultSet.getInt("id");
                    final String gradeLevel = resultSet.getString("grade_level");
                    final String section = resultSet.getString("section");
                    final String adviserName = resultSet.getString("adviser_name");
                    final String salutation = resultSet.getString("salutation");
                    final String dateCreated = resultSet.getString("date_created");

                    gradeLevels.add(new GradeLevel(gradeLevelId, gradeLevel, section, adviserName, salutation, dateCreated));
                }

                // Close the database resources
                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return gradeLevels;
        }

        @Override
        protected void onPostExecute(List<GradeLevel> gradeLevels) {
            if (gradeLevels == null) {
                // Handle the null case
                return;
            }

            // Iterate through the list of grade levels and add each one to the UI
            for (GradeLevel gradeLevel : gradeLevels) {

                // Create a new CardView to represent the grade level
                CardView gradeLevelCardView = new CardView(TeacherDashboard.this);
                CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(16, 16, 16, 16);
                gradeLevelCardView.setLayoutParams(layoutParams);
                gradeLevelCardView.setRadius(20);
                gradeLevelCardView.setCardElevation(20);
                gradeLevelCardView.setPadding(20, 20, 20, 20);
                gradeLevelCardView.setCardBackgroundColor(getColor(R.color.blue_300));

                // Create a new RelativeLayout to hold the button and TextViews
                RelativeLayout layout = new RelativeLayout(TeacherDashboard.this);
                layout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                layout.setPadding(20, 20, 20, 20);

                // Create the three-dot menu
                ImageView menuImageView = new ImageView(TeacherDashboard.this);
                menuImageView.setId(View.generateViewId());
                RelativeLayout.LayoutParams menuParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                menuParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                menuParams.addRule(RelativeLayout.CENTER_VERTICAL);
                layout.addView(menuImageView, menuParams);
                menuImageView.setImageResource(R.drawable.more_vert_3dots);

                menuImageView.setTag(gradeLevel.getGradeLevelId());

                // Set the layout parameters for the button
                menuParams = (RelativeLayout.LayoutParams) menuImageView.getLayoutParams();
                menuParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                menuImageView.setLayoutParams(menuParams);

                // Create a new TextView to display the grade level, section, adviser name, salutation, and date created
                TextView gradeLevelTextView = new TextView(TeacherDashboard.this);
                gradeLevelTextView.setId(View.generateViewId());
                RelativeLayout.LayoutParams gradeLevelParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                gradeLevelParams.addRule(RelativeLayout.RIGHT_OF, menuImageView.getId());
                gradeLevelParams.setMargins(20, 0, 0, 0);
                gradeLevelTextView.setLayoutParams(gradeLevelParams);

                gradeLevelTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                gradeLevelTextView.setTextSize(20);

                // Create a SpannableStringBuilder for the grade level value with different text size and font
                SpannableStringBuilder gradeLevelValueBuilder = new SpannableStringBuilder(gradeLevel.getGradeLevel());
                gradeLevelValueBuilder.setSpan(new AbsoluteSizeSpan(20, true), 0, gradeLevel.getGradeLevel().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                gradeLevelValueBuilder.setSpan(new TypefaceSpan("serif"), 0, gradeLevel.getGradeLevel().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Create a SpannableStringBuilder for the other information with different text size and font
                SpannableStringBuilder otherInfoBuilder = new SpannableStringBuilder(String.format("\n%s\n%s %s\nCreated on %s", gradeLevel.getSection(), gradeLevel.getSalutation(), gradeLevel.getAdviserName(), gradeLevel.getDateCreated()));
                otherInfoBuilder.setSpan(new AbsoluteSizeSpan(15, true), 0, otherInfoBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                otherInfoBuilder.setSpan(new TypefaceSpan("sans-serif"), 0, otherInfoBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Combine the two builders and set it as the text for the gradeLevelTextView
                CharSequence gradeLevelText = TextUtils.concat(gradeLevelValueBuilder, otherInfoBuilder);
                gradeLevelTextView.setText(gradeLevelText);

                // Add the TextView to the layout
                layout.addView(gradeLevelTextView);

                // Add the layout to the gradeLevelCardView
                gradeLevelCardView.addView(layout);

                // Add the CardView to the LinearLayout
                gradeLevelLayout.addView(gradeLevelCardView);

                gradeLevelCardView.setTag(gradeLevel.getGradeLevelId());
                // Set an OnClickListener for the CardView
                gradeLevelCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int gradeLevelId = (int) v.getTag();

                        // Retrieve the user value from SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("ChatBasedName", Context.MODE_PRIVATE);
                        String username = sharedPreferences.getString("teacher_username", "");
                        String password = sharedPreferences.getString("teacher_password", "");
                        String code = sharedPreferences.getString("senderChatCode", "");

                        // Perform user permission check asynchronously
                        new UserPermissionTask(sharedPreferences, gradeLevelId).execute(username, password, code);
                    }
                });

                // Set a click listener for the three-dot menu
                menuImageView.setOnClickListener(view -> {
                    PopupMenu popupMenu = new PopupMenu(TeacherDashboard.this, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_grade_level_more, popupMenu.getMenu());

                    // Set a click listener for the menu items
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {

                            case R.id.menu_delete_grade:
                                int gradeLevelId = gradeLevel.getGradeLevelId();

                                // Create an AlertDialog
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TeacherDashboard.this);
                                alertDialogBuilder.setTitle("Enter Credentials");


                                // Inflate the layout for the dialog
                                View dialogView = getLayoutInflater().inflate(R.layout.dialog_password_authenticator, null);
                                alertDialogBuilder.setView(dialogView);

                                // Find the EditText fields in the dialog layout
                                final EditText usernameEditText = dialogView.findViewById(R.id.usernameEditTextPasswordAuthenticator);
                                final EditText passwordEditText = dialogView.findViewById(R.id.passwordEditTextPasswordAuthenticator);

                                // Set the input type to show password characters as dots
                                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);


                                // Set the Proceed button
                                alertDialogBuilder.setPositiveButton("Proceed", (dialog, which) -> {
                                    String username = usernameEditText.getText().toString();
                                    String password = passwordEditText.getText().toString();

                                    // Execute the AsyncTask to check the user
                                    UserCheckTask userCheckTask = new UserCheckTask(gradeLevelId);
                                    userCheckTask.execute(username, password);
                                });

                                // Show the AlertDialog
                                alertDialogBuilder.show();


                                return true;
                            case R.id.menu_lock_grade:

                                int gradeLevelIDLocking = gradeLevel.getGradeLevelId();

                                // Create an AlertDialog
                                AlertDialog.Builder alertDialogBuilderLockGrade = new AlertDialog.Builder(TeacherDashboard.this);
                                alertDialogBuilderLockGrade.setTitle("Locking Grade Level");
                                alertDialogBuilderLockGrade.setMessage("By locking this grade level, other teacher can't navigate through it. Unless you give them permission");

                                // Inflate the layout for the dialog
                                View dialogViewLockGrade = getLayoutInflater().inflate(R.layout.dialog_password_authenticator, null);
                                alertDialogBuilderLockGrade.setView(dialogViewLockGrade);


                                // Find the EditText fields in the dialog layout
                                final EditText usernameEditTextLockGrade = dialogViewLockGrade.findViewById(R.id.usernameEditTextPasswordAuthenticator);
                                final EditText passwordEditTextLockGrade = dialogViewLockGrade.findViewById(R.id.passwordEditTextPasswordAuthenticator);

                                // Set the input type to show password characters as dots
                                passwordEditTextLockGrade.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                                // Set the Proceed button
                                alertDialogBuilderLockGrade.setPositiveButton("Proceed", (dialog, which) -> {
                                    String username = usernameEditTextLockGrade.getText().toString();
                                    String password = passwordEditTextLockGrade.getText().toString();


                                    // Execute the AsyncTask to check the user
                                    UserCheckTaskLockingGrades userCheckTaskLockingGrades = new UserCheckTaskLockingGrades(gradeLevelIDLocking);
                                    userCheckTaskLockingGrades.execute(username, password);

                                });

                                // Show the AlertDialog
                                alertDialogBuilderLockGrade.show();

                                return true;

                            default:
                                return false;
                        }
                    });

                    // Show the popup menu
                    popupMenu.show();
                });



            }
        }
    }






















    private class UserPermissionTask extends AsyncTask<String, Void, Boolean> {
        private SharedPreferences sharedPreferences;
        private int gradeLevelId;

        private ProgressDialog progressDialog;


        public UserPermissionTask(SharedPreferences sharedPreferences, int gradeLevelId) {
            this.sharedPreferences = sharedPreferences;
            this.gradeLevelId = gradeLevelId;
        }
        private void showLoadingDialog() {
            progressDialog = new ProgressDialog(TeacherDashboard.this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        private void dismissLoadingDialog() {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingDialog();
        }


        @Override
        protected Boolean doInBackground(String... params) {



            // Retrieve the saved data from SharedPreferences
            String senderChatCode = sharedPreferences.getString("senderChatCode", "");
            String teacherUsername = sharedPreferences.getString("teacher_username", "");
            String teacherPassword = sharedPreferences.getString("teacher_password", "");

            // Perform user permission check using the retrieved data
            return checkUserPermission(senderChatCode, teacherUsername, teacherPassword);
        }


        @Override
        protected void onPostExecute(Boolean hasPermission) {
            dismissLoadingDialog();
            if (hasPermission) {
                // User has permission, proceed with the operation
                findCardViewGradeLevel(gradeLevelId);
            } else {
                // User doesn't have permission, display AlertDialog for requesting access
                AlertDialog.Builder builder = new AlertDialog.Builder(TeacherDashboard.this);
                builder.setTitle("Access Restricted");
                builder.setMessage("This content is prohibited. Do you want to request access?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Send a permission request to the database and owner
                        new SendPermissionRequestTask(sharedPreferences, gradeLevelId).execute();


                        // Optionally, you can show a progress dialog or notify the user about the request being sent
                        Toast.makeText(TeacherDashboard.this, "Request sent", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel the operation
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }

        private boolean checkUserPermission(String senderChatCode, String teacherUsername, String teacherPassword) {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                // Establish a connection to the MySQL database
                connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                // Prepare the SQL statement with placeholders for parameters
                String sql = "SELECT COUNT(*) FROM studtask_grade_level_user_permissions WHERE username = ? AND password = ? AND teacher_code = ? AND grade_level_" + gradeLevelId + " = 'granted'";
                statement = connection.prepareStatement(sql);

                // Set the parameter values
                statement.setString(1, teacherUsername);
                statement.setString(2, teacherPassword);
                statement.setString(3, senderChatCode);

                // Execute the query
                resultSet = statement.executeQuery();

                // Check if the query returned any results
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0; // User has permission if count > 0
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Close the resources
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return false; // Default to false if an exception occurs or no results found
        }

    }



    private class SendPermissionRequestTask extends AsyncTask<Void, Void, Void> {
        private SharedPreferences sharedPreferences;
        private int gradeLevelId;

        public SendPermissionRequestTask(SharedPreferences sharedPreferences, int gradeLevelId) {
            this.sharedPreferences = sharedPreferences;
            this.gradeLevelId = gradeLevelId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Retrieve the saved data from SharedPreferences
            String senderChatCode = sharedPreferences.getString("senderChatCode", "");
            String teacherUsername = sharedPreferences.getString("teacher_username", "");
            String teacherPassword = sharedPreferences.getString("teacher_password", "");

            // Perform database queries to gather necessary information
            String ownerCredentials = getOwnerUsername(gradeLevelId);

            // Split the ownerTeacherName into first name and last name
            String[] ownerCredential = ownerCredentials.split("\\|");
            String username = ownerCredential[0];
            String password = ownerCredential[1];
            String teacherCode = ownerCredential[2];
            String recipientCode = teacherCode;

            if (username != null && password != null && teacherCode != null) {

                String ownerTeacherName = getOwnerTeacherName(username, password, teacherCode);
                // Split the ownerTeacherName into first name and last name
                String[] ownerTeacherNameParts = ownerTeacherName.split("\\|");
                String firstname = ownerTeacherNameParts[0];
                String lastname = ownerTeacherNameParts[1];

                if (firstname != null && lastname != null) {

                    String gradeLevelDataInfo = getGradeLevelInfo(gradeLevelId);
                    // Split the ownerTeacherName into first name and last name
                    String[] gradeLevelDataList = gradeLevelDataInfo.split("\\|");
                    String gradeLevelName = gradeLevelDataList[0];
                    String gradeLevelSection = gradeLevelDataList[1];

                    if (gradeLevelName != null && gradeLevelSection != null) {

                        // Prepare the message to send
                        String message = createRequestMessage(firstname, lastname, gradeLevelName, gradeLevelSection, teacherCode, senderChatCode, gradeLevelId);

                        if (senderChatCode != null && recipientCode != null && message != null) {

                            // Send the permission request to the database
                            sendPermissionRequest(senderChatCode, recipientCode, message);
                        }


                    }

                }
            }

            return null;
        }



        private String capitalizeFirstLetter(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }


        private String getOwnerUsername(int gradeLevelID) {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                // Establish a connection to the MySQL database
                connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                // Prepare the SQL statement with placeholders for parameters
                String gradeLevelColumn = "grade_level_" + gradeLevelId;
                String sql = "SELECT username, password, teacher_code FROM studtask_grade_level_user_permissions WHERE " + gradeLevelColumn + " = 'granted' AND owner_username IS NOT NULL";
                statement = connection.prepareStatement(sql);

                // Execute the query
                resultSet = statement.executeQuery();

                // Check if the query returned any results
                if (resultSet.next()) {
                    String username = resultSet.getString("username");
                    String password = resultSet.getString("password");
                    String teacherCode = resultSet.getString("teacher_code");


                    // Retrieve the owner's username and last name using the teacher code
                    String ownerCredentials = username + "|" + password + "|" + teacherCode;

                    if (ownerCredentials != null) {
                        // Return the owner's username
                        return ownerCredentials;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Close the resources
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return null; // Default to null if an exception occurs or no results found
        }


        private String getOwnerTeacherName(String username, String password, String teacherCode) {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                // Establish a connection to the MySQL database
                connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                // Prepare the SQL statement with placeholders for parameters
                String sql = "SELECT firstname, lastname FROM studtask_user_teacher WHERE username = ? AND password = ? AND teacher_code = ?";
                statement = connection.prepareStatement(sql);

                // Set the parameter values
                statement.setString(1, username);
                statement.setString(2, password);
                statement.setString(3, teacherCode);

                // Execute the query
                resultSet = statement.executeQuery();

                // Check if the query returned any results
                if (resultSet.next()) {
                    String firstname = resultSet.getString("firstname");
                    String lastname = resultSet.getString("lastname");

                    String ownerTeacherName = firstname + "|" + lastname;
                    return ownerTeacherName;
                }


            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Close the resources
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return null; // Default to null if an exception occurs or no results found
        }






        private String getGradeLevelInfo(int gradeLevelId) {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                // Establish a connection to the MySQL database
                connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                // Prepare the SQL statement with placeholders for parameters
                String sql = "SELECT grade_level, section FROM teacher_dashboard_glevel_data WHERE id = ?";
                statement = connection.prepareStatement(sql);

                // Set the parameter value
                statement.setInt(1, gradeLevelId);

                // Execute the query
                resultSet = statement.executeQuery();

                // Check if the query returned any results
                if (resultSet.next()) {
                    String gradeLevelName = resultSet.getString("grade_level");
                    String sectionGradeLevel = resultSet.getString("section");

                    String gradeLevelDataInfo = gradeLevelName + "|" + sectionGradeLevel;
                    return gradeLevelDataInfo;

                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Close the resources
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return null; // Default to null if an exception occurs or no results found
        }





        private String createRequestMessage(String firstname, String lastname, String gradeLevelName, String gradeLevelSection, String teacherCode, String senderChatCode, int gradeLevelId) {

            String sectionGradeLevel = "";
            // Format section to remove the number and any hyphens
            sectionGradeLevel = gradeLevelSection.replaceAll("\\d+", "").replaceAll("-", "").trim();

            sectionGradeLevel = capitalizeFirstLetter(sectionGradeLevel);
            String recipientFirstname = capitalizeFirstLetter(firstname);
            String recipientLastname = capitalizeFirstLetter(lastname);
            String recipientName = recipientFirstname + " " + recipientLastname;
            String gradeLevel = capitalizeFirstLetter(gradeLevelName);
            String recipientCode = teacherCode;
            String senderCode = senderChatCode;
            int requestingGradeLevelID = gradeLevelId;

            // Prepare the message
            String message = "<<<requestaccess>>>gdwtedfiwfiowbcbhvuedtedjshydtfVTDQExrfwdgqkcopociehwubf2qojwshfbh<<<requestaccess>>><gradeLevel><teacherRequesting>"+recipientName+"<requestingToHaveAccessID>"+requestingGradeLevelID+"<gradeLevelName>"+gradeLevel+"<section>"+sectionGradeLevel+"<";

            return message;
        }




        private void sendPermissionRequest(String senderChatCode, String recipientCode, String message) {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                // Establish a connection to the MySQL database
                connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                // Prepare the SQL statement with placeholders for parameters
                String sql = "INSERT INTO chat_messages (sender_code, recipient_code, message) VALUES (?, ?, ?)";
                statement = connection.prepareStatement(sql);

                // Set the parameter values
                statement.setString(1, senderChatCode);
                statement.setString(2, recipientCode);
                statement.setString(3, message);

                // Execute the query
                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Close the resources
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Notify the user about the request being sent
            Toast.makeText(TeacherDashboard.this, "Permission request sent", Toast.LENGTH_SHORT).show();
        }
    }















    private class UserCheckTask extends AsyncTask<String, Void, Boolean> {

        private int gradeLevelId;

        public UserCheckTask(int gradeLevelId) {
            this.gradeLevelId = gradeLevelId;
        }

        @Override
        protected Boolean doInBackground(String... credentials) {
            String username = credentials[0];
            String password = credentials[1];

            // Perform the user check logic here, e.g., query the database
            // You need to replace the sample logic with your own implementation
            int count = performUserCheck(username, password);

            // Return true if the user exists, false otherwise
            return count > 0;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            runOnUiThread(() -> {
                if (result) {
                    // User exists, perform deletion
                    deleteGradeLevel(gradeLevelId);
                    refreshGradeLevelUI();
                } else {
                    // User doesn't exist, show a toast message
                    Toast.makeText(getApplicationContext(), "Deletion Failed\n\nIncorrect Username or Password", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    private int performUserCheck(String username, String password) {
        int count = 0;

        try {
            // Connect to the MySQL database
            Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

            try {
                // Create the SQL query
                String query = "SELECT COUNT(*) FROM studtask_user_teacher WHERE username = ? AND password = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, username);
                statement.setString(2, password);

                // Execute the query
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }

                // Close the resources
                resultSet.close();
                statement.close();
            } finally {
                // Close the connection
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }






    private class UserCheckTaskLockingGrades extends AsyncTask<String, Void, Boolean> {

        private int gradeLevelIdLock;

        public UserCheckTaskLockingGrades(int gradeLevelIDLocking) {
            this.gradeLevelIdLock = gradeLevelIDLocking;
        }

        @Override
        protected Boolean doInBackground(String... credentials) {
            String username = credentials[0];
            String password = credentials[1];

            // Perform the user check logic here, e.g., query the database
            // You need to replace the sample logic with your own implementation
            int count = performUserCheck(username, password);

            // Return true if the user exists, false otherwise
            return count > 0;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            runOnUiThread(() -> {
                if (result) {
                    LockingClass lockingClass = new LockingClass(TeacherDashboard.this);
                    lockingClass.executeLockingProcess(gradeLevelIdLock);
                } else {
                    // User doesn't exist, show a toast message
                    Toast.makeText(getApplicationContext(), "Incorrect Username or Password", Toast.LENGTH_SHORT).show();
                }
            });
        }


















    }

    public class LockingClass {
        private Context context;

        public LockingClass(Context context) {
            this.context = context;
        }

        public void executeLockingProcess(int gradeLevelIdLock) {
            // Retrieve the user values from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("ChatBasedName", Context.MODE_PRIVATE);
            String username = sharedPreferences.getString("teacher_username", "");
            String password = sharedPreferences.getString("teacher_password", "");
            String code = sharedPreferences.getString("senderChatCode", "");

            CheckLockStatusTask task = new CheckLockStatusTask(new LockStatusListener() {
                @Override
                public void onLockStatusChecked(boolean isLocked) {
                    // Handle the lock status value here
                    if (isLocked) {
                        // Grade level is locked
                        // Perform actions for locked grade level
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Grade level already been locked by other teacher.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Grade level is not locked
                        // Perform actions for unlocked grade level
                        LockingProcessExecutor executor = new LockingProcessExecutor();
                        executor.executeLockingProcessAsync(gradeLevelIdLock, username, password, code)
                                .exceptionally(throwable -> {
                                    // Handle any exceptions that occurred during the asynchronous operation
                                    throwable.printStackTrace();
                                    return null;
                                });
                    }
                }
            });

            task.execute(gradeLevelIdLock);
        }
    }


    private interface LockStatusListener {
        void onLockStatusChecked(boolean isLocked);
    }

    private class CheckLockStatusTask extends AsyncTask<Integer, Void, Boolean> {

        private LockStatusListener listener; // Callback listener

        public CheckLockStatusTask(LockStatusListener listener) {
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            int gradeLevelIdLock = params[0];
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                // Establish a connection to the MySQL database
                connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                String gradeLevelColumn = "grade_level_" + gradeLevelIdLock;

                // Prepare the SQL statement with placeholders for parameters
                String sql = "SELECT 1 FROM studtask_grade_level_user_permissions WHERE " + gradeLevelColumn + " = 'denied' AND owner_username IS NOT NULL";
                statement = connection.prepareStatement(sql);

                // Execute the query
                resultSet = statement.executeQuery();

                // Check if the query returned any results
                if (resultSet.next()) {
                    return true; // Grade level is locked
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Close the resources
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return false; // Grade level is not locked or an exception occurred
        }

        @Override
        protected void onPostExecute(Boolean isLocked) {
            // Call the listener's method with the lock status value
            listener.onLockStatusChecked(isLocked);
        }
    }











    public class LockingProcessExecutor {
        private final Executor executor = Executors.newSingleThreadExecutor();
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        public CompletableFuture<Void> executeLockingProcessAsync(int gradeLevelIdLock, String username, String password, String code) {
            return CompletableFuture.supplyAsync(() -> {



                Connection connection = null;
                try {
                    // Establish a database connection
                    connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                    // Check if there are new teachers
                    String checkTeachersSql = "SELECT COUNT(*) AS teacherCount FROM studtask_user_teacher WHERE user_type = 'Teacher'";
                    try (PreparedStatement checkTeachersStatement = connection.prepareStatement(checkTeachersSql)) {
                        try (ResultSet teachersCountResult = checkTeachersStatement.executeQuery()) {
                            teachersCountResult.next();
                            int teacherCount = teachersCountResult.getInt("teacherCount");

                            // Check if there are new grade levels
                            String checkGradeLevelsSql = "SELECT COUNT(*) AS gradeLevelCount FROM teacher_dashboard_glevel_data";
                            try (PreparedStatement checkGradeLevelsStatement = connection.prepareStatement(checkGradeLevelsSql)) {
                                try (ResultSet gradeLevelsCountResult = checkGradeLevelsStatement.executeQuery()) {
                                    gradeLevelsCountResult.next();
                                    int gradeLevelCount = gradeLevelsCountResult.getInt("gradeLevelCount");

                                    if (teacherCount > 0 || gradeLevelCount > 0) {
                                        // Fetch all existing teachers
                                        List<String> existingTeachers = new ArrayList<>();
                                        String fetchExistingTeachersSql = "SELECT username FROM studtask_grade_level_user_permissions";
                                        try (PreparedStatement fetchExistingTeachersStatement = connection.prepareStatement(fetchExistingTeachersSql)) {
                                            try (ResultSet existingTeachersResult = fetchExistingTeachersStatement.executeQuery()) {
                                                while (existingTeachersResult.next()) {
                                                    existingTeachers.add(existingTeachersResult.getString("username"));
                                                }
                                            }
                                        }

                                        // Fetch all existing grade levels
                                        List<Integer> existingGradeLevels = new ArrayList<>();
                                        String fetchExistingGradeLevelsSql = "SELECT id FROM teacher_dashboard_glevel_data";
                                        try (PreparedStatement fetchExistingGradeLevelsStatement = connection.prepareStatement(fetchExistingGradeLevelsSql)) {
                                            try (ResultSet existingGradeLevelsResult = fetchExistingGradeLevelsStatement.executeQuery()) {
                                                while (existingGradeLevelsResult.next()) {
                                                    existingGradeLevels.add(existingGradeLevelsResult.getInt("id"));
                                                }
                                            }
                                        }

                                        // Query all teachers
                                        String queryTeachersSql = "SELECT * FROM studtask_user_teacher WHERE user_type = 'Teacher'";
                                        try (PreparedStatement queryTeachersStatement = connection.prepareStatement(queryTeachersSql)) {
                                            try (ResultSet teachersResult = queryTeachersStatement.executeQuery()) {
                                                // Iterate over the teachers
                                                while (teachersResult.next()) {
                                                    String teacherUsername = teachersResult.getString("username");
                                                    String teacherPassword = teachersResult.getString("password");
                                                    String teacherCode = teachersResult.getString("teacher_code");

                                                    // Insert an entry for the teacher only if it doesn't exist already
                                                    if (!existingTeachers.contains(teacherUsername)) {
                                                        String insertTeacherSql = "INSERT INTO studtask_grade_level_user_permissions " +
                                                                "(username, password, teacher_code) " +
                                                                "VALUES (?, ?, ?)";
                                                        try (PreparedStatement insertTeacherStatement = connection.prepareStatement(insertTeacherSql)) {
                                                            insertTeacherStatement.setString(1, teacherUsername);
                                                            insertTeacherStatement.setString(2, teacherPassword);
                                                            insertTeacherStatement.setString(3, teacherCode);
                                                            insertTeacherStatement.executeUpdate();
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Fetch all existing grade level columns
                                        List<String> existingGradeLevelColumns = new ArrayList<>();
                                        DatabaseMetaData metadata = connection.getMetaData();
                                        ResultSet columnsResult = metadata.getColumns(null, null, "studtask_grade_level_user_permissions", null);
                                        while (columnsResult.next()) {
                                            String columnName = columnsResult.getString("COLUMN_NAME");
                                            if (columnName.startsWith("grade_level_")) {
                                                existingGradeLevelColumns.add(columnName);
                                            }
                                        }

                                        // Iterate over the grade levels and create the corresponding columns
                                        try (PreparedStatement existingGradeLevelsStatement = connection.prepareStatement(fetchExistingGradeLevelsSql)) {
                                            try (ResultSet existingGradeLevelsResult = existingGradeLevelsStatement.executeQuery()) {
                                                while (existingGradeLevelsResult.next()) {
                                                    int gradeLevelId = existingGradeLevelsResult.getInt("id");
                                                    String gradeLevelColumn = "grade_level_" + gradeLevelId;

                                                    // Add the column only if it doesn't exist already
                                                    if (!existingGradeLevelColumns.contains(gradeLevelColumn)) {
                                                        String addColumnSql = "ALTER TABLE studtask_grade_level_user_permissions " +
                                                                "ADD COLUMN " + gradeLevelColumn + " VARCHAR(10) NOT NULL DEFAULT 'granted'";
                                                        try (PreparedStatement addColumnStatement = connection.prepareStatement(addColumnSql)) {
                                                            addColumnStatement.executeUpdate();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } //else{

                                    //}

                                    try {
                                        String gradeLevelIDLockColumn = "grade_level_" + gradeLevelIdLock;

                                        // Update the permission status and owner for the grade level
                                        String updatePermissionsSql = "UPDATE studtask_grade_level_user_permissions SET `" + gradeLevelIDLockColumn + "` = ?, owner_username = ? WHERE username = ?";
                                        try (PreparedStatement updatePermissionsStatement = connection.prepareStatement(updatePermissionsSql)) {
                                            updatePermissionsStatement.setString(1, "granted"); // Assuming the permission status is always granted for the owner
                                            updatePermissionsStatement.setString(2, username); // Set the owner_username to the username used
                                            updatePermissionsStatement.setString(3, username); // Update the row for the specific username
                                            updatePermissionsStatement.executeUpdate();

                                            // Update the permission status for other users
                                            String updateUserPermissionSql = "UPDATE studtask_grade_level_user_permissions SET `" + gradeLevelIDLockColumn + "` = 'denied' WHERE username != ?";
                                            try (PreparedStatement updateUserPermissionStatement = connection.prepareStatement(updateUserPermissionSql)) {
                                                updateUserPermissionStatement.setString(1, username);
                                                updateUserPermissionStatement.executeUpdate();

                                            }
                                        }
                                        // Display a toast or appropriate message to indicate success
                                        Handler mainHandler = new Handler(Looper.getMainLooper());
                                        mainHandler.post(() -> Toast.makeText(TeacherDashboard.this, "Grade level locked successfully.", Toast.LENGTH_SHORT).show());

                                    } catch (SQLException e) {
                                        e.printStackTrace();

                                        // Display a toast or appropriate message to indicate failure
                                        Handler mainHandler = new Handler(Looper.getMainLooper());
                                        mainHandler.post(() -> Toast.makeText(TeacherDashboard.this, "Error locking the grade level.", Toast.LENGTH_SHORT).show());
                                    }
                                }
                            }
                        }
                    }


                } catch (SQLException e) {
                    e.printStackTrace();
                    // Return a failure indicator
                    return false;
                } finally {
                    // Close the database connection
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            // Return a failure indicator
                            return false;
                        }
                    }
                }
                return null;
            }, executor).thenAcceptAsync(success -> {
                if (success != null && success) {
                    // Display a toast or appropriate message to indicate success
                    showToast("Grade level locked successfully.");
                } else {
                    // Display a toast or appropriate message to indicate failure
                    showToast("Error locking the grade level.");
                }
            }, new Executor() {
                @Override
                public void execute(Runnable command) {
                    mainThreadHandler.post(command);
                }
            });
        }


        private void showToast(String message) {
            // Display the toast message on the UI thread
            mainThreadHandler.post(() -> {
                Toast.makeText(TeacherDashboard.this, message, Toast.LENGTH_SHORT).show();
            });
        }
    }








}


