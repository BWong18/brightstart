package com.example.brightstart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class NavigationProfile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private EditText etEmail, etParentName, etPhone, etEmergencyContact, etAddress, etOccupation;
    private Spinner spRelationshipWithChild;
    private ImageView profileImage, btnEdit, signoutButton, notification;
    private Button btnSave, btnResetPassword;
    private String currentParentName, currentImageUrl;
    private String userId, parentDocumentId;
    private Uri imageUri;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_profile);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etParentName = findViewById(R.id.et_parent_name);
        etPhone = findViewById(R.id.et_phone);
        etEmergencyContact = findViewById(R.id.et_emergency_contact);
        etAddress = findViewById(R.id.et_address);
        etOccupation = findViewById(R.id.et_occupation);
        spRelationshipWithChild = findViewById(R.id.spRelationshipWithChild);
        profileImage = findViewById(R.id.profile_image);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btn_save);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        notification = findViewById(R.id.ivNotification);
        signoutButton = findViewById(R.id.ivSignOut);

        // Initialize dropdown adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.relationship_with_child_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRelationshipWithChild.setAdapter(adapter);

        fetchUserData();  // Fetch initial data

        // Back button setup
        ImageView backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }

        // Edit button setup
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> toggleEditMode());
        }

        // Notification button setup
        if (notification != null) {
            notification.setOnClickListener(v -> openActivity(NotificationMenuActivity.class));
        }

        // Sign-out button setup
        if (signoutButton != null) {
            signoutButton.setOnClickListener(v -> signOutUser());
        }

        // Save button setup
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                if (isEditing) {
                    saveProfileChanges();
                }
            });
        }

        // Profile image click for image picker
        if (profileImage != null) {
            profileImage.setOnClickListener(v -> {
                if (isEditing) {
                    openImagePicker();
                }
            });
        }

        // Reset password button setup
        if (btnResetPassword != null) {
            btnResetPassword.setOnClickListener(v -> resetUserPassword());
        }

        // Bottom navigation setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_home) {
            startActivity(new Intent(NavigationProfile.this, DashboardActivity.class));
            finish();
            return true;
        } else if (itemId == R.id.navigation_chat) {
            startActivity(new Intent(NavigationProfile.this, AdminActivity.class));
            return true;
        } else if (itemId == R.id.navigation_profile) {
            startActivity(new Intent(NavigationProfile.this, NavigationProfile.class));
            finish();
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
            Glide.with(this).load(imageUri).circleCrop().into(profileImage);
        }
    }

    private void fetchUserData() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    if (userSnapshot.exists()) {
                        String email = userSnapshot.getString("email");
                        String parentName = userSnapshot.getString("name");
                        String phone = userSnapshot.getString("phone");
                        String userType = userSnapshot.getString("userType");

                        etEmail.setText(email);
                        etParentName.setText(parentName);
                        etPhone.setText(phone);
                        currentParentName = parentName;

                        if ("admin".equals(userType)) {
                            etEmergencyContact.setVisibility(View.GONE);
                            etAddress.setVisibility(View.GONE);
                            etOccupation.setVisibility(View.GONE);
                            spRelationshipWithChild.setVisibility(View.GONE);
                        } else {
                            fetchParentProfileData(parentName);
                        }

                        if (userSnapshot.contains("profileImageUrl")) {
                            currentImageUrl = userSnapshot.getString("profileImageUrl");
                            if (!TextUtils.isEmpty(currentImageUrl)) {
                                Glide.with(this).load(currentImageUrl).circleCrop().into(profileImage);
                            }
                        }

                        toggleFields(false);
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("NavigationProfile", "Error fetching user profile", e));
    }

    private void fetchParentProfileData(String parentName) {
        db.collection("parentsProfiles")
                .whereEqualTo("name", parentName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot parentDoc = querySnapshot.getDocuments().get(0);
                        parentDocumentId = parentDoc.getId();

                        String emergencyContact = parentDoc.getString("emergencyContact");
                        String address = parentDoc.getString("address");
                        String occupation = parentDoc.getString("occupation");
                        String relationship = parentDoc.getString("relationshipWithChild");
                        currentImageUrl = parentDoc.getString("profileImageUrl");

                        etEmergencyContact.setText(emergencyContact);
                        etAddress.setText(address);
                        etOccupation.setText(occupation);
                        setRelationshipWithChildSelection(relationship);

                        if (!TextUtils.isEmpty(currentImageUrl)) {
                            Glide.with(this).load(currentImageUrl).circleCrop().into(profileImage);
                        }

                        toggleFields(false);
                    } else {
                        Toast.makeText(this, "Parent profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("NavigationProfile", "Error fetching parent profile", e));
    }

    private void setRelationshipWithChildSelection(String relationship) {
        if (relationship != null) {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spRelationshipWithChild.getAdapter();
            int position = adapter.getPosition(relationship);
            spRelationshipWithChild.setSelection(position >= 0 ? position : 0);
        } else {
            spRelationshipWithChild.setSelection(0);
        }
    }

    private void toggleEditMode() {
        isEditing = !isEditing;
        toggleFields(isEditing);
        btnEdit.setImageResource(isEditing ? R.drawable.ic_edit_cancel : R.drawable.ic_edit);
    }

    private void toggleFields(boolean isEditable) {
        etParentName.setEnabled(isEditable);
        etPhone.setEnabled(isEditable);
        etEmergencyContact.setEnabled(isEditable);
        etAddress.setEnabled(isEditable);
        etOccupation.setEnabled(isEditable);
        spRelationshipWithChild.setEnabled(isEditable);
        btnSave.setEnabled(isEditable);
    }

    private void saveProfileChanges() {
        if (parentDocumentId == null) {
            Toast.makeText(this, "Parent profile not found. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = etParentName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String emergencyContact = etEmergencyContact.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String occupation = etOccupation.getText().toString().trim();
        String relationshipWithChild = spRelationshipWithChild.getSelectedItem().toString();

        if (newName.isEmpty() || phone.isEmpty() || emergencyContact.isEmpty() || address.isEmpty() || occupation.isEmpty() || "Choose".equals(relationshipWithChild)) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            uploadImageToFirebase(imageUrl -> updateProfileData(newName, phone, emergencyContact, address, occupation, relationshipWithChild, imageUrl));
        } else {
            updateProfileData(newName, phone, emergencyContact, address, occupation, relationshipWithChild, currentImageUrl);
        }
    }

    private void uploadImageToFirebase(OnImageUploadCompleteListener listener) {
        String uniqueFileName = "profile_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference().child(uniqueFileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> listener.onComplete(uri.toString())))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                    Log.e("NavigationProfile", "Error uploading image", e);
                });
    }

    private void updateProfileData(String newName, String phone, String emergencyContact, String address, String occupation, String relationshipWithChild, String imageUrl) {
        db.collection("users").document(userId)
                .update("name", newName, "phone", phone)
                .addOnFailureListener(e -> Log.e("NavigationProfile", "Error updating user profile", e));

        db.collection("parentsProfiles").document(parentDocumentId)
                .update("name", newName, "emergencyContact", emergencyContact, "address", address, "occupation", occupation, "relationshipWithChild", relationshipWithChild, "profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                    if (!newName.equals(currentParentName)) {
                        updateChildrenParentName(currentParentName, newName);
                        currentParentName = newName;
                    }
                    isEditing = false;
                    toggleFields(isEditing);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                    Log.e("NavigationProfile", "Error updating parent profile", e);
                });
    }

    private void updateChildrenParentName(String oldParentName, String newParentName) {
        db.collection("childrenProfiles")
                .whereEqualTo("parentName", oldParentName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            document.getReference().update("parentName", newParentName)
                                    .addOnFailureListener(e -> Log.e("NavigationProfile", "Error updating child profile", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("NavigationProfile", "Error fetching children profiles to update parent name", e));
    }

    private void resetUserPassword() {
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

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(NavigationProfile.this, activityClass);
        startActivity(intent);
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(NavigationProfile.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }

    interface OnImageUploadCompleteListener {
        void onComplete(String imageUrl);
    }
}
