package com.droideainfoph.studtaskmanager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class SplashScreen extends AppCompatActivity implements DatabaseConnectionChecker.ConnectionCheckListener {

    private static final int SPLASH_SCREEN_DURATION = 5000;
    private static final int MAX_RETRY_COUNT = 0;
    private static final int RETRY_DELAY = 0;

    // Variables
    Animation topAnim, bottomAnim;
    ImageView logo_img;
    TextView title, version;
    ProgressDialog loadingDialog;
    AlertDialog retryDialog;

    private int retryCount;

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    private Handler handler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        // Animation
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_anim);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim);

        // Hooks
        logo_img = findViewById(R.id.imageLogo);
        title = findViewById(R.id.textTitle);
        version = findViewById(R.id.textVersion);

        // Assign animation
        logo_img.setAnimation(topAnim);
        title.setAnimation(bottomAnim);
        version.setAnimation(bottomAnim);

        // Provide the database connection information
        dbUrl = getString(R.string.db_url_mysql);
        dbUsername = getString(R.string.db_username);
        dbPassword = getString(R.string.db_password);

        // Create loading dialog
        createLoadingDialog();

        // Create retry dialog
        createRetryDialog();

        // Delayed handler for opening new activity
        handler.postDelayed(() -> {
            startConnectionCheck();
        }, SPLASH_SCREEN_DURATION);
    }

    private void createLoadingDialog() {
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Checking database connection...");
        loadingDialog.setCancelable(false);
    }

    private void createRetryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connection Failed")
                .setMessage("Unable to establish database connection. Do you want to retry?")
                .setPositiveButton("Try Again", (dialog, which) -> {
                    dialog.dismiss();
                    startConnectionCheck();
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    dialog.dismiss();
                    finishAffinity(); // Exit the app completely
                });

        retryDialog = builder.create();
        retryDialog.setCancelable(false);
    }

    private void startConnectionCheck() {
        retryCount = 1;
        attemptConnectionCheck();
    }

    private void attemptConnectionCheck() {
        showLoadingDialog();
        DatabaseConnectionChecker connectionChecker = new DatabaseConnectionChecker(this, dbUrl, dbUsername, dbPassword);
        connectionChecker.startConnectionCheck();

        // Schedule a delayed task to handle the timeout
        handler.postDelayed(() -> {
            boolean isConnected = connectionChecker.checkConnectionStatus(); // Check the connection status using an appropriate method in DatabaseConnectionChecker
            if (!isConnected) {
                hideLoadingDialog();
                showRetryDialog();
                connectionChecker.stopConnectionCheck(); // If there is a method to stop the connection check, call it here
            }
        }, 3000); // Change the timeout duration here (in milliseconds)
    }

    private void retryConnectionCheck() {
        retryCount++;
        if (retryCount <= MAX_RETRY_COUNT) {
            handler.postDelayed(() -> {
                attemptConnectionCheck();
            }, RETRY_DELAY);
        } else {
            // Connection retries exhausted, show retry dialog
            hideLoadingDialog();
            showRetryDialog();
        }
    }

    private void showLoadingDialog() {
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showRetryDialog() {
        if (!retryDialog.isShowing()) {
            runOnUiThread(() -> retryDialog.show());
        }
    }


    private void hideRetryDialog() {
        if (retryDialog.isShowing()) {
            retryDialog.dismiss();
        }
    }

    @Override
    public void onConnectionCheckComplete(boolean isConnected) {
        hideLoadingDialog();
        if (isConnected) {
            // Database connection is successful
            // Proceed with the app flow
            proceedToNextActivity();
        } else {
            // Database connection failed
            //retryConnectionCheck();
        }
    }

    private void proceedToNextActivity() {
        Intent intent = new Intent(SplashScreen.this, LogInMain.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
