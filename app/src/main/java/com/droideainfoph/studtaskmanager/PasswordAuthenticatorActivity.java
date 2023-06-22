package com.droideainfoph.studtaskmanager;

import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordAuthenticatorActivity extends AppCompatActivity {

    public void authenticateCredentialsAsync(String username, String password, AuthenticationListener listener) {
        new AuthenticationTask(listener).execute(username, password);
    }

    private class AuthenticationTask extends AsyncTask<String, Void, Boolean> {
        private AuthenticationListener listener;

        public AuthenticationTask(AuthenticationListener listener) {
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String username = params[0];
            String password = params[1];

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(getString(R.string.db_url_mysql), getString(R.string.db_username), getString(R.string.db_password));

                String query = "SELECT COUNT(*) FROM studtask_user_teacher WHERE username = ? AND password = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);

                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                int count = resultSet.getInt(1);

                resultSet.close();
                preparedStatement.close();
                connection.close();

                return count > 0;
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isValidCredentials) {
            listener.onAuthenticationComplete(isValidCredentials);
        }
    }

    public interface AuthenticationListener {
        void onAuthenticationComplete(boolean isValidCredentials);
    }

    // Other methods and activity lifecycle callbacks...
}
