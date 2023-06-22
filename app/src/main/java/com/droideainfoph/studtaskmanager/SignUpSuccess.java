package com.droideainfoph.studtaskmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SignUpSuccess extends AppCompatActivity {


    private static final String TAG = "SignUpSuccess";
    TextView lastnameShow;
    TextView firstnameShow;
    TextView usernameShow;
    TextView passwordShow;
    TextView schoolIDShow;

    TextView userTypeShow;

    TextView userSexShow;
    TextView selectedGradeLevelShow;
    TextView lrnShow;
    TextView birthDay;

    Button proceedButton;

    private static final int DELAY_TIME_MS = 2000; //




    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_success);


        lastnameShow = findViewById(R.id.success_lastname);
        firstnameShow = findViewById(R.id.success_firstname);
        usernameShow = findViewById(R.id.success_username);
        passwordShow = findViewById(R.id.success_password);
        schoolIDShow = findViewById(R.id.success_school_id);
        userTypeShow = findViewById(R.id.success_user_type);
        userSexShow = findViewById(R.id.success_user_sex);
        selectedGradeLevelShow = findViewById( R.id.success_selected_grade);
        lrnShow = findViewById(R.id.success_lrn);
        birthDay = findViewById(R.id.success_birthday);
        proceedButton = findViewById(R.id.success_proceed_to_logIn);


        // Delayed execution using a Handler
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                captureScreenshotWithPermissionCheck();
            }
        }, DELAY_TIME_MS);
    }

    private void captureScreenshotWithPermissionCheck() {
        verifyStoragePermission(this);
        captureScreenshot(findViewById(android.R.id.content), "your_credential");

        proceedButton.setOnClickListener(view -> {

            // Open a new activity using Intent
            Intent intent = new Intent(SignUpSuccess.this, LogInMain.class);
            startActivity(intent);

        });



        // Retrieve the data from the intent
        String userType = getIntent().getStringExtra("userType");

        if (userType.equals("Teacher")) {
            // Retrieve the data from the intent
            String lastname = getIntent().getStringExtra("lastname");
            String firstname = getIntent().getStringExtra("firstname");
            String username = getIntent().getStringExtra("username");
            String password = getIntent().getStringExtra("password");
            String schoolId = getIntent().getStringExtra("schoolId");
            String teacherCode = getIntent().getStringExtra("teacherCode");



            // Create a SpannableStringBuilder for each TextView
            SpannableStringBuilder lastnameBuilder = new SpannableStringBuilder();
            SpannableStringBuilder firstnameBuilder = new SpannableStringBuilder();
            SpannableStringBuilder usernameBuilder = new SpannableStringBuilder();
            SpannableStringBuilder passwordBuilder = new SpannableStringBuilder();
            SpannableStringBuilder schoolIDBuilder = new SpannableStringBuilder();
            SpannableStringBuilder userTypeBuilder = new SpannableStringBuilder();
            SpannableStringBuilder teacherCodeBuilder = new SpannableStringBuilder();



            // Set the labels with bold style
            String lastnameLabelText = "Lastname: ";
            SpannableString lastnameLabelSpannable = new SpannableString(lastnameLabelText);
            lastnameLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, lastnameLabelText.length(), 0);
            lastnameBuilder.append(lastnameLabelSpannable);

            // Set the value with italic style
            SpannableString lastnameValueSpannable = new SpannableString(lastname);
            lastnameValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, lastname.length(), 0);
            lastnameBuilder.append(lastnameValueSpannable);

            lastnameShow.setText(lastnameBuilder);

            // Repeat the same pattern for the other TextViews
            String firstnameLabelText = "Firstname: ";
            SpannableString firstnameLabelSpannable = new SpannableString(firstnameLabelText);
            firstnameLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, firstnameLabelText.length(), 0);
            firstnameBuilder.append(firstnameLabelSpannable);
            SpannableString firstnameValueSpannable = new SpannableString(firstname);
            firstnameValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, firstname.length(), 0);
            firstnameBuilder.append(firstnameValueSpannable);
            firstnameShow.setText(firstnameBuilder);

            String usernameLabelText = "Username: ";
            SpannableString usernameLabelSpannable = new SpannableString(usernameLabelText);
            usernameLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, usernameLabelText.length(), 0);
            usernameBuilder.append(usernameLabelSpannable);
            SpannableString usernameValueSpannable = new SpannableString(username);
            usernameValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, username.length(), 0);
            usernameBuilder.append(usernameValueSpannable);
            usernameShow.setText(usernameBuilder);

            String passwordLabelText = "Password: ";
            SpannableString passwordLabelSpannable = new SpannableString(passwordLabelText);
            passwordLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, passwordLabelText.length(), 0);
            passwordBuilder.append(passwordLabelSpannable);
            SpannableString passwordValueSpannable = new SpannableString(password);
            passwordValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, password.length(), 0);
            passwordBuilder.append(passwordValueSpannable);
            passwordShow.setText(passwordBuilder);

            String schoolIDLabelText = "School ID: ";
            SpannableString schoolIDLabelSpannable = new SpannableString(schoolIDLabelText);
            schoolIDLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, schoolIDLabelText.length(), 0);
            schoolIDBuilder.append(schoolIDLabelSpannable);
            SpannableString schoolIDValueSpannable = new SpannableString(schoolId);
            schoolIDValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, schoolId.length(), 0);
            schoolIDBuilder.append(schoolIDValueSpannable);
            schoolIDShow.setText(schoolIDBuilder);

            String userTypeLabelText = "User Role: ";
            SpannableString userTypeLabelSpannable = new SpannableString(userTypeLabelText);
            userTypeLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, userTypeLabelText.length(), 0);
            userTypeBuilder.append(userTypeLabelSpannable);
            SpannableString userTypeValueSpannable = new SpannableString(userType);
            userTypeValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, userType.length(), 0);
            userTypeBuilder.append(userTypeValueSpannable);
            userTypeShow.setText(userTypeBuilder);


            String teacherCodeLabelText = "Teacher Code: ";
            SpannableString teacherCodeLabelSpannable = new SpannableString(teacherCodeLabelText);
            teacherCodeLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, teacherCodeLabelText.length(), 0);
            teacherCodeBuilder.append(teacherCodeLabelSpannable);
            SpannableString teacherCodeValueSpannable = new SpannableString(teacherCode);
            teacherCodeValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, teacherCode.length(), 0);
            teacherCodeBuilder.append(teacherCodeValueSpannable);
            userSexShow.setText(teacherCodeBuilder);




            Log.d(TAG, "Teacher data: lastname=" + lastname + ", firstname=" + firstname);
            Log.d(TAG, "Teacher data: username=" + username + ", password=" + password);
            Log.d(TAG, "Teacher data: schoolId=" + schoolId + ", teacherCode=" + teacherCode);
        } else if (userType.equals("Student")) {
            // Retrieve the data from the intent
            String lastname = getIntent().getStringExtra("lastname");
            String firstname = getIntent().getStringExtra("firstname");
            String username = getIntent().getStringExtra("username");
            String password = getIntent().getStringExtra("password");
            String schoolId = getIntent().getStringExtra("schoolId");
            String userSex = getIntent().getStringExtra("userSex");
            String gradeLevel = getIntent().getStringExtra("gradeLevel");
            String lrn = getIntent().getStringExtra("lrn");
            String birthday = getIntent().getStringExtra("birthday");


            String lastnameLabelText = "Lastname: ";
            SpannableString lastnameLabelSpannable = new SpannableString(lastnameLabelText);
            lastnameLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, lastnameLabelText.length(), 0);
            lastnameShow.setText(lastnameLabelSpannable);

            SpannableString lastnameValueSpannable = new SpannableString(lastname);
            lastnameValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, lastname.length(), 0);
            lastnameShow.append(lastnameValueSpannable);

            String firstnameLabelText = "Firstname: ";
            SpannableString firstnameLabelSpannable = new SpannableString(firstnameLabelText);
            firstnameLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, firstnameLabelText.length(), 0);
            firstnameShow.setText(firstnameLabelSpannable);

            SpannableString firstnameValueSpannable = new SpannableString(firstname);
            firstnameValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, firstname.length(), 0);
            firstnameShow.append(firstnameValueSpannable);

            String usernameLabelText = "Username: ";
            SpannableString usernameLabelSpannable = new SpannableString(usernameLabelText);
            usernameLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, usernameLabelText.length(), 0);
            usernameShow.setText(usernameLabelSpannable);

            SpannableString usernameValueSpannable = new SpannableString(username);
            usernameValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, username.length(), 0);
            usernameShow.append(usernameValueSpannable);

            String passwordLabelText = "Password: ";
            SpannableString passwordLabelSpannable = new SpannableString(passwordLabelText);
            passwordLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, passwordLabelText.length(), 0);
            passwordShow.setText(passwordLabelSpannable);

            SpannableString passwordValueSpannable = new SpannableString(password);
            passwordValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, password.length(), 0);
            passwordShow.append(passwordValueSpannable);

            String schoolIdLabelText = "School ID: ";
            SpannableString schoolIdLabelSpannable = new SpannableString(schoolIdLabelText);
            schoolIdLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, schoolIdLabelText.length(), 0);
            schoolIDShow.setText(schoolIdLabelSpannable);

            SpannableString schoolIdValueSpannable = new SpannableString(schoolId);
            schoolIdValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, schoolId.length(), 0);
            schoolIDShow.append(schoolIdValueSpannable);

            String userSexLabelText = "Sex: ";
            SpannableString userSexLabelSpannable = new SpannableString(userSexLabelText);
            userSexLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, userSexLabelText.length(), 0);
            userSexShow.setText(userSexLabelSpannable);

            SpannableString userSexValueSpannable = new SpannableString(userSex);
            userSexValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, userSex.length(), 0);
            userSexShow.append(userSexValueSpannable);

            String gradeLevelLabelText = "Grade Level: ";
            SpannableString gradeLevelLabelSpannable = new SpannableString(gradeLevelLabelText);
            gradeLevelLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, gradeLevelLabelText.length(), 0);
            selectedGradeLevelShow.setText(gradeLevelLabelSpannable);

            SpannableString gradeLevelValueSpannable = new SpannableString(gradeLevel);
            gradeLevelValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, gradeLevel.length(), 0);
            selectedGradeLevelShow.append(gradeLevelValueSpannable);

            String lrnLabelText = "LRN: ";
            SpannableString lrnLabelSpannable = new SpannableString(lrnLabelText);
            lrnLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, lrnLabelText.length(), 0);
            lrnShow.setText(lrnLabelSpannable);

            SpannableString lrnValueSpannable = new SpannableString(lrn);
            lrnValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, lrn.length(), 0);
            lrnShow.append(lrnValueSpannable);

            String birthdayLabelText = "Birthdate: ";
            SpannableString birthdayLabelSpannable = new SpannableString(birthdayLabelText);
            birthdayLabelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, birthdayLabelText.length(), 0);
            birthDay.setText(birthdayLabelSpannable);

            SpannableString birthdayValueSpannable = new SpannableString(birthday);
            birthdayValueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, birthday.length(), 0);
            birthDay.append(birthdayValueSpannable);



            Log.d(TAG, "Student data: lastname=" + lastname + ", firstname=" + firstname);
            Log.d(TAG, "Student data: username=" + username + ", password=" + password);
            Log.d(TAG, "Student data: schoolId=" + schoolId + ", userSex=" + userSex);
            Log.d(TAG, "Student data: gradeLevel=" + gradeLevel + ", lrn=" + lrn);
            Log.d(TAG, "Student data: birthday=" + birthday);
        }



        // Trigger screenshot function after 8 seconds
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Capture the screenshot


        }, 3000); // 8 seconds delay
    }



    private static File captureScreenshot(View view, String filename) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String formattedDate = dateFormat.format(date);

        try {
            File fileDir = new File(view.getContext().getExternalFilesDir(null), "StudenTask_Screenshot");
            if (!fileDir.exists()) {
                boolean mkdir = fileDir.mkdir();
            }

            String path = fileDir.getPath() + "/" + filename + "-" + formattedDate + ".png";

            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);

            File imageFile = new File(path);
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            return imageFile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public static void verifyStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivity(intent);
            }
        }
    }






}