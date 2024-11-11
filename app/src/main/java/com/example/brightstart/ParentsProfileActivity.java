package com.example.brightstart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ParentsProfileActivity extends AppCompatActivity {

    private EditText etParentName, etOccupation, etEmergencyContact, etAddress;
    private Spinner spRelationshipWithChild;
    private FirebaseFirestore db; // Firebase Firestore instance

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parents_profile);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Find views by ID
        etParentName = findViewById(R.id.etParentName);
        etOccupation = findViewById(R.id.etOccupation);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        etAddress = findViewById(R.id.etAddress);
        spRelationshipWithChild = findViewById(R.id.spRelationshipWithChild);
        Button btnNext = findViewById(R.id.btnNext);

        // Set up the relationship Spinner with data
        setupRelationshipSpinner();

        // Set up the next button click event to save the profile and go to next screen
        btnNext.setOnClickListener(v -> saveParentProfile());
    }

    // Method to populate the Spinner with relationship data
    private void setupRelationshipSpinner() {
        String[] relationshipOptions = {"Choose", "Father", "Mother", "Guardian"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, relationshipOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRelationshipWithChild.setAdapter(adapter);
    }

    // Method to save the parent profile details in Firestore
    private void saveParentProfile() {
        String name = etParentName.getText().toString().trim();
        String occupation = etOccupation.getText().toString().trim();
        String emergencyContact = etEmergencyContact.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String relationshipWithChild = spRelationshipWithChild.getSelectedItem().toString();

        // Validate input fields
        if (name.isEmpty()) {
            etParentName.setError("Name is required");
            etParentName.requestFocus();
            return;
        }
        if (occupation.isEmpty()) {
            etOccupation.setError("Occupation is required");
            etOccupation.requestFocus();
            return;
        }
        if (relationshipWithChild.equals("Choose")) {
            Toast.makeText(this, "Please select a relationship", Toast.LENGTH_SHORT).show();
            return;
        }
        if (emergencyContact.isEmpty()) {
            etEmergencyContact.setError("Emergency contact is required");
            etEmergencyContact.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return;
        }

        // Prepare data to be saved in Firestore
        Map<String, Object> parentProfile = new HashMap<>();
        parentProfile.put("name", name);
        parentProfile.put("occupation", occupation);
        parentProfile.put("relationshipWithChild", relationshipWithChild);
        parentProfile.put("emergencyContact", emergencyContact);
        parentProfile.put("address", address);

        // Save data to Firebase Firestore under the "parentsProfiles" collection
        db.collection("parentsProfiles")
                .add(parentProfile)
                .addOnSuccessListener(documentReference -> {
                    // Log success and navigate
                    Log.d("ParentsProfileActivity", "Parent profile saved successfully");
                    Toast.makeText(ParentsProfileActivity.this, "Parent profile saved successfully", Toast.LENGTH_SHORT).show();

                    // Navigate to the next screen (Child Profile Activity)
                    Log.d("ParentsProfileActivity", "Navigating to Child Profile");
                    Intent intent = new Intent(ParentsProfileActivity.this, ChildProfileActivity.class);
                    startActivity(intent);

                })
                .addOnFailureListener(e -> {
                    // Log failure
                    Log.e("ParentsProfileActivity", "Error saving parent profile", e);
                    Toast.makeText(ParentsProfileActivity.this, "Error saving parent profile", Toast.LENGTH_SHORT).show();
                });



    }
}

