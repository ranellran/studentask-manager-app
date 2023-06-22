package com.droideainfoph.studtaskmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatNavigationSelector extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatUserAdaptor userAdapter;
    private List<String> userList;
    private List<String> uniqueCodes;

    private String senderCode; // Unique code identifier of the sender

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_navigation_selector);

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        uniqueCodes = new ArrayList<>();
        userAdapter = new ChatUserAdaptor(userList, uniqueCodes, uniqueCode -> {
            openChatUserRoom(uniqueCode);
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Perform the back button action
                finish();
            }
        });

        recyclerView.setAdapter(userAdapter);

        // Retrieve the sender's unique code from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("ChatBasedName", MODE_PRIVATE);
        senderCode = sharedPreferences.getString("senderChatCode", "");
        Log.d("Debug", "Sender Code: " + senderCode);

        // Retrieve user names from the database and populate the list
        retrieveUserNames();
    }

    private void retrieveUserNames() {
        new RetrieveUserNamesTask().execute();
    }

    private void openChatUserRoom(String uniqueCode) {
        Intent intent = new Intent(this, ChatUserRoom.class);
        intent.putExtra("senderCode", senderCode);
        intent.putExtra("recipientCode", uniqueCode);
        startActivity(intent);
    }

    private class RetrieveUserNamesTask extends AsyncTask<Void, Void, List<List<String>>> {
        private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        private String dbUrl;
        private String dbUsername;
        private String dbPassword;

        public RetrieveUserNamesTask() {
            dbUrl = getString(R.string.db_url_mysql);
            dbUsername = getString(R.string.db_username);
            dbPassword = getString(R.string.db_password);
        }

        @Override
        protected List<List<String>> doInBackground(Void... voids) {
            List<String> userList = new ArrayList<>();
            List<String> uniqueCodes = new ArrayList<>();

            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;

            try {
                Class.forName(JDBC_DRIVER);
                conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                stmt = conn.createStatement();

                // Retrieve user names from the student table, excluding the sender
                String studentQuery = "SELECT CONCAT(firstname, ' ', lastname, ' (Student)') AS UserName, lrn AS UniqueCode FROM studtask_user_student WHERE lrn <> ? ORDER BY Firstname ASC";
                PreparedStatement studentStmt = conn.prepareStatement(studentQuery);
                studentStmt.setString(1, senderCode);
                ResultSet studentRs = studentStmt.executeQuery();

                while (studentRs.next()) {
                    String userName = studentRs.getString("UserName");
                    String uniqueCode = studentRs.getString("UniqueCode");
                    userList.add(userName);
                    uniqueCodes.add(uniqueCode);
                }

                studentRs.close();
                studentStmt.close();

                // Retrieve user names from the teacher table, excluding the sender
                String teacherQuery = "SELECT CONCAT(firstname, ' ', lastname, ' (Teacher)') AS UserName, teacher_code AS UniqueCode FROM studtask_user_teacher WHERE teacher_code <> ? ORDER BY Firstname ASC";
                PreparedStatement teacherStmt = conn.prepareStatement(teacherQuery);
                teacherStmt.setString(1, senderCode);
                ResultSet teacherRs = teacherStmt.executeQuery();

                while (teacherRs.next()) {
                    String userName = teacherRs.getString("UserName");
                    String uniqueCode = teacherRs.getString("UniqueCode");
                    userList.add(userName);
                    uniqueCodes.add(uniqueCode);
                }

                teacherRs.close();
                teacherStmt.close();

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            } finally {
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

            return Arrays.asList(userList, uniqueCodes);
        }

        @Override
        protected void onPostExecute(List<List<String>> result) {
            List<String> userList = result.get(0);
            List<String> uniqueCodes = result.get(1);

            // Update the user list in the adapter
            userAdapter.setUserList(userList);

            // Update the uniqueCodes list in the adapter
            userAdapter.setUniqueCodes(uniqueCodes);
        }
    }
}
