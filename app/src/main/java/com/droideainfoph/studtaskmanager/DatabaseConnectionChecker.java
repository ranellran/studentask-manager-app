package com.droideainfoph.studtaskmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DatabaseConnectionChecker {

    private ConnectionCheckListener listener;
    private Executor executor;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private volatile boolean stopConnectionCheck;

    public DatabaseConnectionChecker(ConnectionCheckListener listener, String dbUrl, String dbUsername, String dbPassword) {
        this.listener = listener;
        this.executor = Executors.newSingleThreadExecutor();
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.stopConnectionCheck = false;
    }

    public void startConnectionCheck() {
        executor.execute(new ConnectionCheckRunnable());
    }

    public void stopConnectionCheck() {
        stopConnectionCheck = true;
    }

    public boolean isConnected() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            connection.close();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkConnectionStatus() {
        long startTime = System.currentTimeMillis();

        while (!stopConnectionCheck && System.currentTimeMillis() - startTime <= 100) {
            boolean isConnected = isConnected();
            if (isConnected) {
                listener.onConnectionCheckComplete(true);
                return true;
            }
        }

        listener.onConnectionCheckComplete(false);
        return false;
    }

    private class ConnectionCheckRunnable implements Runnable {
        @Override
        public void run() {
            checkConnectionStatus();
        }
    }

    public interface ConnectionCheckListener {
        void onConnectionCheckComplete(boolean isConnected);
    }
}
