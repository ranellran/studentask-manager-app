package com.droideainfoph.studtaskmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import android.view.MenuItem;
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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;


public class TeacherSubjectView extends AppCompatActivity {

    // Define the request code constant
    private static final int REQUEST_CODE_ADD_SUBJECT = 1;
    private LinearLayout subjectViewLayout;
    private Button addSubjectButton;



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


        public int getSubjectId() { return id; }

    }

    // Inside the onActivityResult() method of TeacherSubjectView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_CODE_ADD_SUBJECT && resultCode == RESULT_OK) {
            // Get the data returned from the AddSubjectActivity
            String subjectName = data.getStringExtra("subject_name");
            String subjectTeacherName = data.getStringExtra("subject_teacher_name");
            String semestral = data.getStringExtra("semestral");
            String semestralValue = data.getStringExtra("semestral_value");
            String quarterType = data.getStringExtra("quarter_type");
            String uniqueCode = data.getStringExtra("unique_code");
            String dateCreated = data.getStringExtra("date_created");


            // Retrieve the data from the Intent extras
            Intent intent = getIntent();
            int idG = intent.getIntExtra("id", 0);
            String gradeLevel = intent.getStringExtra("gradeLevel");
            String dateGCreated = intent.getStringExtra("dateCreated");


        }
    }

    //Main UI thread here
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_subject_view);




        subjectViewLayout = findViewById(R.id.subject_view_layout);
        addSubjectButton = findViewById(R.id.add_subject_button);

        // Execute the AsyncTask to retrieve the list of grade levels

        // Clear the existing grade level views
        subjectViewLayout.removeAllViews();




        // Retrieve the data from the Intent extras
        Intent intent = getIntent();
        int idG = intent.getIntExtra("id", 0);

        // Convert the integer value to a string
        String nextActivityGradeLevelID = String.valueOf(idG);





        addSubjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPreferences = getSharedPreferences("gradeLevelID", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("nextActivityGradeLevelID", nextActivityGradeLevelID);
                editor.apply();

                Intent intent = getIntent();
                int idG = intent.getIntExtra("id", 0);
                String gradeLevel = intent.getStringExtra("gradeLevel");
                String dateGCreated = intent.getStringExtra("dateCreated");

                Intent newIntent = new Intent(TeacherSubjectView.this, TeacherStudentSelectorActivity.class);
                newIntent.putExtra("id", idG);
                newIntent.putExtra("gradeLevel", gradeLevel);
                newIntent.putExtra("dateCreated", dateGCreated);
                startActivity(newIntent);

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


    @Override
    protected void onResume() {
        super.onResume();

        // Clear the existing grade level views
        subjectViewLayout.removeAllViews();

        // Execute the refreshSubjectViewUI() method to refresh the activity
        refreshSubjectViewUI();
    }








    private void deleteSubjectData(int subjectDataId) {
        DeleteSubjectDataTask task = new DeleteSubjectDataTask(getApplicationContext());
        task.execute(subjectDataId);
    }

    // In the DeleteSubjectDataTask class
    private class DeleteSubjectDataTask extends AsyncTask<Integer, Void, Boolean> {
        private Context context;

        public DeleteSubjectDataTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            int subjectDataId = params[0];
            boolean success = false;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to retrieve the unique code associated with the subject data
                PreparedStatement retrieveUniqueCodeStatement = connection.prepareStatement(
                        "SELECT unique_code FROM subject_list_data WHERE id = ?");
                retrieveUniqueCodeStatement.setInt(1, subjectDataId);
                ResultSet resultSet = retrieveUniqueCodeStatement.executeQuery();

                String uniqueCode = "";
                if (resultSet.next()) {
                    uniqueCode = resultSet.getString("unique_code");
                }

                // Generate the table name using the unique code
                String tableName = "subject_" + uniqueCode;

                // Execute a SQL query to delete the subject data with the given ID
                PreparedStatement deleteSubjectDataStatement = connection.prepareStatement(
                        "DELETE FROM subject_list_data WHERE id = ?");
                deleteSubjectDataStatement.setInt(1, subjectDataId);
                int rowsAffected = deleteSubjectDataStatement.executeUpdate();

                if (rowsAffected > 0) {
                    // Execute a SQL query to drop the table with the generated table name
                    PreparedStatement dropTableStatement = connection.prepareStatement(
                            "DROP TABLE IF EXISTS " + tableName);
                    dropTableStatement.executeUpdate();
                    success = true;
                }

                // Close the database resources
                resultSet.close();
                retrieveUniqueCodeStatement.close();
                deleteSubjectDataStatement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(context, "Subject data deleted successfully", Toast.LENGTH_SHORT).show();
                 // Execute refreshSubjectViewUI() after deletion
            } else {
                Toast.makeText(context, "Failed to delete subject data", Toast.LENGTH_SHORT).show();
            }
        }
    }

























    /**
     * Adds a new grade level to the database and refreshes the UI
     */



    /**
     * Refreshes the UI to display the list of grade levels
     */

    void refreshSubjectViewUI() {
        // Clear the existing grade level views
        subjectViewLayout.removeAllViews();

        // Retrieve the updated grade levels from the database and display them again
        new RefreshSubjectViewUIAsyncTask().execute();
    }

    private class RefreshSubjectViewUIAsyncTask extends AsyncTask<Void, Void, List<SubjectData>> {

        @Override
        protected List<SubjectData> doInBackground(Void... voids) {
            List<SubjectData> subjectData = new ArrayList<>();
            try {
                // Retrieve the data from the Intent extras
                Intent intent = getIntent();
                int idG = intent.getIntExtra("id", 0);
                String gradeLevel = intent.getStringExtra("gradeLevel");
                String dateGCreated = intent.getStringExtra("dateCreated");

                // Connect to the MySQL database
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to retrieve the subjects belonging to the specific grade level
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM subject_list_data WHERE grade_level_subject_belong = ? AND id_subject_belong = ? ");
                statement.setString(1, gradeLevel);
                statement.setInt(2, idG);

                ResultSet resultSet = statement.executeQuery();

                // Iterate through the result set and add each subject to the list
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    final String subjectName = resultSet.getString("subject_name");
                    final String teacherName = resultSet.getString("teacher_name");
                    final String semester = resultSet.getString("semestral");
                    final String semestralValue = resultSet.getString("semestral_value");
                    final String quarterType = resultSet.getString("quarter_type");
                    final String uniqueCode = resultSet.getString("unique_code");
                    final String dateCreated = resultSet.getString("date_created");

                    subjectData.add(new SubjectData(id, subjectName, teacherName, semester, semestralValue, quarterType, uniqueCode, dateCreated));
                }

                // Close the database resources
                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return subjectData;
        }

        // Rest of the code...
    @Override
        protected void onPostExecute(List<SubjectData> subjectsData) {
            if (subjectsData == null) {
                // Handle the null case
                return;
            }

            // Iterate through the list of grade levels and add each one to the UI
            for (SubjectData subjectData : subjectsData) {
                // Create a new CardView to represent the grade level
                CardView subjectViewListCardView = new CardView(TeacherSubjectView.this);
                CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(16, 16, 16, 16);
                subjectViewListCardView.setLayoutParams(layoutParams);
                subjectViewListCardView.setRadius(20);
                subjectViewListCardView.setCardElevation(20);
                subjectViewListCardView.setPadding(20, 20, 20, 20);
                subjectViewListCardView.setCardBackgroundColor(getColor(R.color.blue_300));

                // Create a new RelativeLayout to hold the button and TextViews
                RelativeLayout layout = new RelativeLayout(TeacherSubjectView.this);
                layout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                layout.setPadding(20, 20, 20, 20);

                // Create a new TextView to display the grade level, section, adviser name, salutation, and date created
                TextView subjectTextView = new TextView(TeacherSubjectView.this);
                subjectTextView.setId(View.generateViewId());
                RelativeLayout.LayoutParams subjectListParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                subjectListParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                subjectListParams.setMargins(0, 0, 0, 0);
                subjectTextView.setLayoutParams(subjectListParams);
                subjectTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                subjectTextView.setTextSize(20);

                // Create a SpannableStringBuilder for the grade level value with different text size and font
                SpannableStringBuilder subjectListValueBuilder = new SpannableStringBuilder(subjectData.getSubjectName());
                subjectListValueBuilder.setSpan(new AbsoluteSizeSpan(20, true), 0, subjectData.getSubjectName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                subjectListValueBuilder.setSpan(new TypefaceSpan("serif"), 0, subjectData.getSubjectName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Create a SpannableStringBuilder for the other information with different text size and font
                SpannableStringBuilder otherInfoBuilder = new SpannableStringBuilder(String.format("\n%s\nHave semester? %s\nSemester value: %s\nSubject code: %s\nCreated on %s", subjectData.getTeacherName(), subjectData.getSemestral(), subjectData.getSemestralValue(), subjectData.getUniqueCode(), subjectData.getDateCreated()));
                otherInfoBuilder.setSpan(new AbsoluteSizeSpan(15, true), 0, otherInfoBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                otherInfoBuilder.setSpan(new TypefaceSpan("sans-serif"), 0, otherInfoBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


                // Combine the two builders and set it as the text for the subjectTextView
                CharSequence gradeLevelText = TextUtils.concat(subjectListValueBuilder, otherInfoBuilder);
                subjectTextView.setText(gradeLevelText);
                // Add the TextView to the layout
                layout.addView(subjectTextView);

                // Add the layout to the subjectViewListCardView
                subjectViewListCardView.addView(layout);

                // Add the CardView to the LinearLayout
                subjectViewLayout.addView(subjectViewListCardView);





                subjectViewListCardView.setTag(subjectData.getSubjectId());

                // Set an OnClickListener for the CardView
                subjectViewListCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int subjectID = (int) v.getTag();

                        findCardViewSubject(subjectID);
                    }
                });


                // Create the three-dot menu
                ImageView menuImageView = new ImageView(TeacherSubjectView.this);
                menuImageView.setId(View.generateViewId());
                RelativeLayout.LayoutParams menuParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                menuParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                menuParams.addRule(RelativeLayout.CENTER_VERTICAL);
                layout.addView(menuImageView, menuParams);
                menuImageView.setImageResource(R.drawable.more_vert_3dots);

                // Set a click listener for the three-dot menu
                menuImageView.setOnClickListener(view -> {
                    PopupMenu popupMenu = new PopupMenu(TeacherSubjectView.this, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_subject_options, popupMenu.getMenu());

                    // Set a click listener for the menu items
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {

                            case R.id.menu_delete:
                                int subjectDataId = subjectData.getSubjectId();

                                // Create an AlertDialog
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TeacherSubjectView.this);
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
                                alertDialogBuilder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String username = usernameEditText.getText().toString();
                                        String password = passwordEditText.getText().toString();

                                        // Execute the AsyncTask to check the user
                                        TeacherSubjectView.UserCheckTask userCheckTask = new TeacherSubjectView.UserCheckTask(subjectDataId);
                                        userCheckTask.execute(username, password);
                                    }
                                });

                                // Show the AlertDialog
                                alertDialogBuilder.show();




                                return true;
                            case R.id.menu_copy_code:
                                // Get the unique code from subjectData
                                String uniqueCode = subjectData.getUniqueCode();

                                // Get the clipboard manager
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                                // Create a ClipData object to store the text to be copied
                                ClipData clip = ClipData.newPlainText("Unique Code", uniqueCode);

                                // Set the ClipData object to the clipboard
                                clipboard.setPrimaryClip(clip);

                                // Show a toast message indicating that the code has been copied
                                Toast.makeText(TeacherSubjectView.this, "Unique code copied to clipboard", Toast.LENGTH_SHORT).show();
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

    private void findCardViewSubject(int subjectID) {
        FindCardViewSubjectTask task = new FindCardViewSubjectTask();
        task.execute(subjectID);
    }

    private class FindCardViewSubjectTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            int subjectID = params[0];

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql),
                        getString(R.string.db_username), getString(R.string.db_password));

                // Execute a SQL query to retrieve the grade level data with the given ID
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT unique_code FROM subject_list_data WHERE id = ?");
                statement.setInt(1, subjectID);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Retrieve the data from the result set
                    String uniqueCode = resultSet.getString("unique_code");


                    // Create the table name using the unique code
                    String tableName = "subject_" + uniqueCode;
                    Intent getIntent = getIntent();
                    String gradeLevel = getIntent.getStringExtra("gradeLevel");

                    // Pass the table name as an extra in the Intent
                    Intent intent = new Intent(TeacherSubjectView.this, GradesPortalTeacher.class);
                    intent.putExtra("tableName", tableName);
                    intent.putExtra("gradeLevel", gradeLevel);
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

    private class UserCheckTask extends AsyncTask<String, Void, Boolean> {

        private int subjectDataId;

        public UserCheckTask(int subjectDataId) {
            this.subjectDataId = subjectDataId;
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
                    deleteSubjectData(subjectDataId);
                    refreshSubjectViewUI();
                } else {
                    // User doesn't exist, show a toast message
                    Toast.makeText(getApplicationContext(), "Incorrect Username or Password", Toast.LENGTH_SHORT).show();
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





}


