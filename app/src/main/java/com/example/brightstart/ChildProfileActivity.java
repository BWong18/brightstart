package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChildProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText etName, etParentName, etDOB, etAge;
    private Spinner spinnerGender;
    private Button btnDone, btnAddAnotherChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find views
        etName = findViewById(R.id.etChildName);
        etParentName = findViewById(R.id.etParentName);
        etDOB = findViewById(R.id.etDateOfBirth);
        etAge = findViewById(R.id.etAge);
        spinnerGender = findViewById(R.id.spGender);
        btnDone = findViewById(R.id.btnDone);
        btnAddAnotherChild = findViewById(R.id.btnAddAnotherChild);

        // Set up the gender dropdown (spinner)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Retrieve and auto-fill parent's name from Firestore
        retrieveParentName();

        // Set up Done button click listener to save and navigate away
        btnDone.setOnClickListener(v -> saveChildProfileAndNavigate());

        // Set up Add Another Child button click listener to save and clear fields for another entry
        btnAddAnotherChild.setOnClickListener(v -> saveChildProfileAndClearFields());
    }

    private void retrieveParentName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail(); // Retrieve the current user's email

            // Find the user in the "users" collection by their email
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String parentName = querySnapshot.getDocuments().get(0).getString("name");
                            etParentName.setText(parentName); // Auto-fill parent's name
                        } else {
                            Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error fetching user profile.", Toast.LENGTH_SHORT).show());
        }
    }

    private void saveChildProfileAndNavigate() {
        if (saveChildProfile()) {
            // Navigate to the Dashboard or another activity after saving
            Intent intent = new Intent(ChildProfileActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void saveChildProfileAndClearFields() {
        if (saveChildProfile()) {
            // Clear fields after saving, so user can add another child
            clearFields();
        }
    }

    private boolean saveChildProfile() {
        String childName = etName.getText().toString().trim();
        String parentName = etParentName.getText().toString().trim(); // Auto-filled from parent profile
        String dob = etDOB.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        // Input validation with error handling
        if (TextUtils.isEmpty(childName)) {
            etName.setError("Child's name is required");
            etName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(dob)) {
            etDOB.setError("Date of Birth is required");
            etDOB.requestFocus();
            return false;
        }
        if (!isValidDateOfBirth(dob)) {
            etDOB.setError("Invalid date format (use DD/MM/YYYY)");
            etDOB.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(ageStr)) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return false;
        }
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            etAge.setError("Age must be a number");
            etAge.requestFocus();
            return false;
        }
        if (gender.equals("Choose")) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Create a map of the child profile data
        Map<String, Object> childProfile = new HashMap<>();
        childProfile.put("childName", childName);
        childProfile.put("parentName", parentName);
        childProfile.put("dateOfBirth", dob);
        childProfile.put("age", age);  // Store age as an integer
        childProfile.put("gender", gender);
        childProfile.put("parentId", currentUser.getUid());

        // Save to Firestore under "childrenProfiles"
        db.collection("childrenProfiles")
                .add(childProfile)
                .addOnSuccessListener(documentReference -> Toast.makeText(ChildProfileActivity.this, "Child Profile Saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ChildProfileActivity.this, "Error saving profile", Toast.LENGTH_SHORT).show());

        return true;
    }

    private boolean isValidDateOfBirth(String dob) {
        return dob.matches("\\d{2}/\\d{2}/\\d{4}");
    }

    private void clearFields() {
        etName.setText("");
        etDOB.setText("");
        etAge.setText("");
        spinnerGender.setSelection(0);
    }
}
