package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

public class AdminStudentInfo extends AppCompatActivity {

    private FirebaseFirestore db;
    private String documentId;
    private EditText etChildName, etParentName, etDob, etAge, etPhone, etEmergencyContact, etAddress;
    private Spinner spGender;
    private ImageView profileImage, signoutButton;
    private Button btnRemove;
    private String imageUrl;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_student_info_acitivity);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize views
        etChildName = findViewById(R.id.et_child_name);
        etParentName = findViewById(R.id.et_parent_name);
        etDob = findViewById(R.id.et_dob);
        etAge = findViewById(R.id.et_age);
        etPhone = findViewById(R.id.et_phone);
        etEmergencyContact = findViewById(R.id.et_emergency_contact);
        etAddress = findViewById(R.id.et_address);
        spGender = findViewById(R.id.spGender);
        profileImage = findViewById(R.id.profile_image);
        btnRemove = findViewById(R.id.btn_remove);

        // Disable gender Spinner to make it uneditable
        spGender.setEnabled(false);

        // Fetch child profile directly
        fetchChildProfileDirectly();

        // Set up Remove button functionality
        btnRemove.setOnClickListener(v -> confirmAndRemoveStudent());

        // Back button functionality
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());

        // Initialize and set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.admin_navigation_home) {
            Intent homeIntent = new Intent(AdminStudentInfo.this, AdminDashboardActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_chat) {
            Intent homeIntent = new Intent(AdminStudentInfo.this, AdminActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_profile) {
            Intent profileIntent = new Intent(AdminStudentInfo.this, AdminNavigationProfile.class);
            startActivity(profileIntent);
            finish();
            return true;
        }
        return false;
    }

    private void fetchChildProfileDirectly() {
        // Retrieve the document ID (could be passed from the previous activity as an Intent extra)
        documentId = getIntent().getStringExtra("documentId");

        if (TextUtils.isEmpty(documentId)) {
            Toast.makeText(this, "No document ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch the child profile directly using the document ID
        db.collection("childrenProfiles").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch child details
                        String childName = documentSnapshot.getString("childName");
                        String dob = documentSnapshot.getString("dateOfBirth");
                        Integer age = documentSnapshot.contains("age") ? documentSnapshot.getLong("age").intValue() : null;
                        String gender = documentSnapshot.getString("gender");
                        imageUrl = documentSnapshot.getString("profileImageUrl");
                        String parentName = documentSnapshot.getString("parentName");

                        // Set the fetched details in the corresponding views
                        etChildName.setText(childName);
                        etDob.setText(dob);
                        etParentName.setText(parentName);

                        // Handle age as integer
                        if (age != null) {
                            etAge.setText(String.valueOf(age));
                        } else {
                            Log.w("AdminStudentInfo", "Age field is missing or invalid for document ID: " + documentId);
                        }

                        // Set gender selection in Spinner based on the fetched value
                        setGenderSelection(gender);

                        // Load profile image if available
                        loadProfileImage();

                        // Fetch additional parent and user details
                        fetchAdditionalDetails(parentName);
                    } else {
                        Toast.makeText(this, "No child profile found with the provided ID.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch child profile.", Toast.LENGTH_SHORT).show();
                    Log.e("AdminStudentInfo", "Error fetching child profile", e);
                });
    }

    private void fetchAdditionalDetails(String parentName) {
        db.collection("parentsProfiles")
                .whereEqualTo("name", parentName)
                .get()
                .addOnSuccessListener(parentSnapshot -> {
                    if (!parentSnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : parentSnapshot) {
                            etAddress.setText(document.getString("address"));
                            etEmergencyContact.setText(document.getString("emergencyContact"));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminStudentInfo", "Error fetching parent profile", e));

        db.collection("users")
                .whereEqualTo("name", parentName)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    if (!userSnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : userSnapshot) {
                            etPhone.setText(document.getString("phone"));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminStudentInfo", "Error fetching user details", e));
    }

    private void setGenderSelection(String gender) {
        if (gender != null) {
            switch (gender) {
                case "Male":
                    spGender.setSelection(1);
                    break;
                case "Female":
                    spGender.setSelection(2);
                    break;
                case "Other":
                    spGender.setSelection(3);
                    break;
                default:
                    spGender.setSelection(0);
                    break;
            }
        } else {
            spGender.setSelection(0);
        }
    }

    private void loadProfileImage() {
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImage);
        }
    }

    private void confirmAndRemoveStudent() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Removal")
                .setMessage("Are you sure you want to remove this student?")
                .setPositiveButton("Yes", (dialog, which) -> removeStudent())
                .setNegativeButton("No", null)
                .show();
    }

    private void removeStudent() {
        db.collection("childrenProfiles").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminStudentInfo.this, "Student removed successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminStudentInfo.this, "Failed to remove student.", Toast.LENGTH_SHORT).show();
                    Log.e("AdminStudentInfo", "Error removing student", e);
                });
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(AdminStudentInfo.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
