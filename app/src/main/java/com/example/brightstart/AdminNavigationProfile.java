package com.example.brightstart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AdminNavigationProfile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private EditText etEmail, etAdminName, etPhone;
    private ImageView profileImage, btnEdit, signoutButton;
    private Button btnSave, btnResetPassword;
    private boolean isEditing = false;
    private String currentAdminEmail;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_navigation_profile);

        // Initialize Firestore, FirebaseAuth, and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etAdminName = findViewById(R.id.et_admin_name);
        etPhone = findViewById(R.id.et_phone);
        profileImage = findViewById(R.id.profile_image);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btn_save);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up Edit button functionality
        btnEdit.setOnClickListener(v -> toggleEditMode());

        // Set up Save button functionality
        btnSave.setOnClickListener(v -> {
            if (isEditing) {
                saveProfileChanges();
            }
        });

        // Set up Profile Image Click Listener for image upload
        profileImage.setOnClickListener(v -> {
            if (isEditing) {
                openImagePicker();
            }
        });

        // Set up Reset Password button functionality
        btnResetPassword.setOnClickListener(v -> resetAdminPassword());

        // Fetch and display admin profile data
        fetchAdminProfileData();

        signoutButton = findViewById(R.id.ivSignOut);

        signoutButton.setOnClickListener(v -> signOutUser());

        // Initialize and set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.admin_navigation_home) {
            // Navigate to AdminDashboardActivity
            Intent homeIntent = new Intent(AdminNavigationProfile.this, AdminDashboardActivity.class);
            startActivity(homeIntent);
            finish();  // Close current activity
            return true;
        } else if (item.getItemId() == R.id.admin_navigation_chat) {
            Intent homeIntent = new Intent(AdminNavigationProfile.this, AdminActivity.class);
            startActivity(homeIntent);
            finish();  // Close current activity
            return true;
        } else if (item.getItemId() == R.id.admin_navigation_profile) {
            // Stay on the current activity (Profile screen)
            return true;
        }
        return false;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).circleCrop().into(profileImage);  // Display selected image
        }
    }

    private void toggleEditMode() {
        isEditing = !isEditing;
        toggleFields(isEditing);
        btnEdit.setImageResource(isEditing ? R.drawable.ic_edit_cancel : R.drawable.ic_edit);
    }

    private void toggleFields(boolean isEditable) {
        etAdminName.setEnabled(isEditable);
        etPhone.setEnabled(isEditable);
        btnSave.setEnabled(isEditable);
    }

    private void fetchAdminProfileData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && "admin".equalsIgnoreCase(documentSnapshot.getString("userType"))) {
                        String email = documentSnapshot.getString("email");
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        etEmail.setText(email);
                        etAdminName.setText(name);
                        etPhone.setText(phone);
                        currentAdminEmail = email;

                        if (!TextUtils.isEmpty(profileImageUrl)) {
                            Glide.with(this).load(profileImageUrl).circleCrop().into(profileImage);
                        }
                        toggleFields(false);
                    } else {
                        Toast.makeText(this, "No admin profile found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminNavigationProfile", "Error fetching admin profile", e));
    }

    private void saveProfileChanges() {
        String updatedName = etAdminName.getText().toString().trim();
        String updatedPhone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(updatedName)) {
            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(updatedPhone)) {
            Toast.makeText(this, "Phone number cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            uploadProfileImage(updatedName, updatedPhone);
        } else {
            updateAdminProfileInFirestore(updatedName, updatedPhone, null);
        }
    }
    private void uploadProfileImage(String updatedName, String updatedPhone) {
        StorageReference imageRef = storage.getReference("profile_images/" + UUID.randomUUID().toString() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    updateAdminProfileInFirestore(updatedName, updatedPhone, imageUrl);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminNavigationProfile.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                    Log.e("AdminNavigationProfile", "Error uploading image", e);
                });
    }

    private void updateAdminProfileInFirestore(String updatedName, String updatedPhone, String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .update(
                        "name", updatedName,
                        "phone", updatedPhone,
                        "profileImageUrl", imageUrl
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminNavigationProfile.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                    toggleEditMode(); // Exit edit mode
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminNavigationProfile.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                    Log.e("AdminNavigationProfile", "Error updating profile", e);
                });
    }

    private void resetAdminPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset instructions sent to your email!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send reset instructions: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(AdminNavigationProfile.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
