package com.droideainfoph.studtaskmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddGradeLevelActivity extends AppCompatActivity {
    private Spinner gradeLevelSelector;
    private EditText sectionEditText;
    private EditText adviserNameEditText;
    private Spinner salutationSpinner;
    private TextView dateCreatedTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_grade_level);

        // Initialize the views
        gradeLevelSelector = findViewById(R.id.spinnerAddGrade);
        sectionEditText = findViewById(R.id.section_edit_text);
        adviserNameEditText = findViewById(R.id.adviser_name_edit_text);
        salutationSpinner = findViewById(R.id.spinnerSalutation);
        dateCreatedTextView = findViewById(R.id.date_created_text_view);

        // Set up the salutation spinner with options "Mr.", "Ms.", and "Mrs."
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.salutation_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        salutationSpinner.setAdapter(adapter);

        // Set up the salutation spinner with options "Mr.", "Ms.", and "Mrs."
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.grade_level_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeLevelSelector.setAdapter(adapter2);



        // Set a click listener for a "Save" button to return the data to the calling activity
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gather the data from the views
                String gradeLevel = gradeLevelSelector.getSelectedItem().toString();
                String section = sectionEditText.getText().toString();
                String adviserName = adviserNameEditText.getText().toString();
                String salutation = salutationSpinner.getSelectedItem().toString();
                String dateCreated = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

                // Check if all fields have a value
                if (gradeLevel.isEmpty() || section.isEmpty() || adviserName.isEmpty() || salutation.isEmpty()) {
                    Toast.makeText(AddGradeLevelActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                } else {
                    // Create an Intent to return the data to the calling activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("grade_level", gradeLevel);
                    resultIntent.putExtra("section", section);
                    resultIntent.putExtra("adviser_name", adviserName);
                    resultIntent.putExtra("salutation", salutation);
                    resultIntent.putExtra("date_created", dateCreated);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

    }
}
