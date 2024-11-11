package com.example.brightstart;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KidsInfoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseFirestore db;
    private String parentEmail, selectedChildId;
    private EditText etChildName, etParentName, etDob, etAge;
    private Spinner spGender, childSpinner;
    private ImageView profileImage, btnEdit, signoutButton, notification;
    private Button btnSave;
    private boolean isEditing = false;
    private String imageUrl;
    private Uri imageUri;
    private FirebaseStorage storage;

    private class Child {
        String name;
        String id;

        Child(String name, String id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kids_info);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        etChildName = findViewById(R.id.et_child_name);
        etParentName = findViewById(R.id.et_parent_name);
        etParentName.setEnabled(false);
        etDob = findViewById(R.id.et_dob);
        etAge = findViewById(R.id.et_age);
        spGender = findViewById(R.id.spGender);
        childSpinner = findViewById(R.id.child_spinner);
        profileImage = findViewById(R.id.profile_image);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btn_save);

        toggleFields(false);

        fetchParentEmail();

        btnEdit.setOnClickListener(v -> {
            isEditing = !isEditing;
            toggleFields(isEditing);
        });

        btnSave.setOnClickListener(v -> saveChildProfile());

        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());

        profileImage.setOnClickListener(v -> openImagePicker());

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        notification = findViewById(R.id.ivNotification);
        notification.setOnClickListener(v -> openActivity(NotificationMenuActivity.class));

        childSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Child selectedChild = (Child) parent.getItemAtPosition(position);
                selectedChildId = selectedChild.id;
                fetchChildProfile(selectedChildId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_home) {
            startActivity(new Intent(KidsInfoActivity.this, DashboardActivity.class));
            finish();
            return true;
        } else if (itemId == R.id.navigation_chat) {
            startActivity(new Intent(KidsInfoActivity.this, ChatActivity.class));
            return true;
        } else if (itemId == R.id.navigation_profile) {
            startActivity(new Intent(KidsInfoActivity.this, NavigationProfile.class));
            finish();
            return true;
        }
        return false;
    }

    private void toggleFields(boolean isEditable) {
        profileImage.setEnabled(isEditable);
        etChildName.setEnabled(isEditable);
        etDob.setEnabled(isEditable);
        etAge.setEnabled(isEditable);
        spGender.setEnabled(isEditable);

        btnEdit.setImageResource(isEditable ? R.drawable.ic_edit_cancel : R.drawable.ic_edit);

        btnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }

    private void fetchParentEmail() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            parentEmail = currentUser.getEmail();
            if (!TextUtils.isEmpty(parentEmail)) {
                fetchChildrenListUsingParentEmail(parentEmail);
            } else {
                Toast.makeText(this, "Parent email not found.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No logged-in user found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchChildrenListUsingParentEmail(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(usersSnapshot -> {
                    if (!usersSnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = usersSnapshot.getDocuments().get(0);
                        String parentName = userDoc.getString("name");
                        etParentName.setText(parentName);

                        if (!TextUtils.isEmpty(parentName)) {
                            fetchChildrenProfiles(parentName);
                        } else {
                            Toast.makeText(this, "Parent name not found.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "No user profile found with the email.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user profile.", Toast.LENGTH_SHORT).show();
                    Log.e("KidsInfoActivity", "Error fetching user profile", e);
                });
    }

    private void fetchChildrenProfiles(String parentName) {
        db.collection("childrenProfiles")
                .whereEqualTo("parentName", parentName)
                .get()
                .addOnSuccessListener(childrenSnapshot -> {
                    List<Child> children = new ArrayList<>();

                    for (DocumentSnapshot childDoc : childrenSnapshot.getDocuments()) {
                        String childName = childDoc.getString("childName");
                        String childId = childDoc.getId();

                        if (childName != null) {
                            children.add(new Child(childName, childId));
                        }
                    }

                    if (children.isEmpty()) {
                        Toast.makeText(this, "No children found for this parent.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    setupChildSpinner(children);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch children list.", Toast.LENGTH_SHORT).show());
    }

    private void setupChildSpinner(List<Child> children) {
        ArrayAdapter<Child> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, children);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childSpinner.setAdapter(adapter);

        if (!children.isEmpty()) {
            selectedChildId = children.get(0).id;
            fetchChildProfile(selectedChildId);
        }
    }

    private void fetchChildProfile(String childId) {
        db.collection("childrenProfiles")
                .document(childId)
                .get()
                .addOnSuccessListener(childDoc -> {
                    if (childDoc.exists()) {
                        etChildName.setText(childDoc.getString("childName"));
                        etDob.setText(childDoc.getString("dateOfBirth"));
                        Integer age = childDoc.contains("age") ? childDoc.getLong("age").intValue() : null;
                        if (age != null) {
                            etAge.setText(String.valueOf(age));
                        } else {
                            Log.w("KidsInfoActivity", "Age field is missing or invalid for document ID: " + childId);
                        }
                        setGenderSelection(childDoc.getString("gender"));
                        imageUrl = childDoc.getString("profileImageUrl");
                        loadProfileImage();
                    } else {
                        Toast.makeText(this, "No child profile found.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch child profile.", Toast.LENGTH_SHORT).show());
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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this)
                    .load(imageUri)
                    .circleCrop()
                    .into(profileImage);
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference profileImageRef = storage.getReference().child("profile_images/" + selectedChildId + ".jpg");
            profileImageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageUrl = uri.toString();
                        updateProfileImageUrl(imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(KidsInfoActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                        Log.e("KidsInfoActivity", "Error uploading image", e);
                    });
        }
    }

    private void updateProfileImageUrl(String imageUrl) {
        DocumentReference childProfileRef = db.collection("childrenProfiles").document(selectedChildId);
        childProfileRef.update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(KidsInfoActivity.this, "Profile image updated successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile image.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveChildProfile() {
        if (!isEditing) {
            return;
        }
        String childName = etChildName.getText().toString();
        String dob = etDob.getText().toString();
        String ageStr = etAge.getText().toString();
        String gender = spGender.getSelectedItem().toString();

        if (TextUtils.isEmpty(childName) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(ageStr) || "Choose".equals(gender)) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);

            Map<String, Object> childProfileData = new HashMap<>();
            childProfileData.put("childName", childName);
            childProfileData.put("dateOfBirth", dob);
            childProfileData.put("age", age);
            childProfileData.put("gender", gender);

            db.collection("childrenProfiles").document(selectedChildId)
                    .update(childProfileData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile saved successfully.", Toast.LENGTH_SHORT).show();
                        isEditing = false;
                        toggleFields(isEditing);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save profile.", Toast.LENGTH_SHORT).show();
                        Log.e("KidsInfoActivity", "Error saving profile", e);
                    });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid integer for age.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(KidsInfoActivity.this, activityClass);
        startActivity(intent);
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(KidsInfoActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
