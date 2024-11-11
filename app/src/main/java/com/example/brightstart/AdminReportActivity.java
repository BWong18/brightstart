package com.example.brightstart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AdminReportActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private String childName, documentId;
    private TextView tvStudentNameAdmin, tvReportDateAdmin;
    private EditText etHomework, etMeals, etTuition, etMood, etPerformance;
    private LinearLayout imagesContainer;
    private Button btnSaveReport, btnEditReport, btnAddImage;
    private ArrayList<Uri> imageUriList = new ArrayList<>();
    private ImageView signoutButton;

    private boolean isEditing = false;  // Flag to track if we are in "edit mode"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_report);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Retrieve childName and documentId from Intent
        Intent intent = getIntent();
        childName = intent.getStringExtra("childName");
        documentId = intent.getStringExtra("documentId");

        if (childName == null || documentId == null) {
            Toast.makeText(this, "No student data found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvStudentNameAdmin = findViewById(R.id.tvStudentNameAdmin);
        tvReportDateAdmin = findViewById(R.id.tvReportDateAdmin);
        etHomework = findViewById(R.id.etHomework);
        etMeals = findViewById(R.id.etMeals);
        etTuition = findViewById(R.id.etTuition);
        etMood = findViewById(R.id.etMood);
        etPerformance = findViewById(R.id.etPerformance);
        imagesContainer = findViewById(R.id.images_container);
        btnSaveReport = findViewById(R.id.btnSaveReport);
        btnEditReport = findViewById(R.id.btnEditReport);
        btnAddImage = findViewById(R.id.btnAddImage);

        // Set the student's name in the UI
        tvStudentNameAdmin.setText(childName + "'s Report");

        // Set current date for the report
        setCurrentDate();

        // Initially, make fields non-editable
        toggleFields(false);

        // Set up click listener for the Edit button
        btnEditReport.setOnClickListener(v -> {
            isEditing = !isEditing; // Toggle edit mode
            toggleFields(isEditing); // Make fields editable if in edit mode
        });

        // Handle image upload for activity picture
        btnAddImage.setOnClickListener(v -> openImagePicker());

        // Handle save report button
        btnSaveReport.setOnClickListener(v -> saveOrUpdateReport());

        // Set up back button functionality
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // Fetch existing images and other data if report already exists
        fetchReportData();

        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());

        // Initialize and set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.admin_navigation_home) {
            Intent homeIntent = new Intent(AdminReportActivity.this, AdminDashboardActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_chat) {
            Intent homeIntent = new Intent(AdminReportActivity.this, AdminActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_profile) {
            Intent profileIntent = new Intent(AdminReportActivity.this, AdminNavigationProfile.class);
            startActivity(profileIntent);
            finish();
            return true;
        }
        return false;
    }

    private void toggleFields(boolean isEditable) {
        etHomework.setEnabled(isEditable);
        etMeals.setEnabled(isEditable);
        etTuition.setEnabled(isEditable);
        etMood.setEnabled(isEditable);
        etPerformance.setEnabled(isEditable);
        btnSaveReport.setEnabled(isEditable);
        btnEditReport.setText(isEditable ? "Cancel" : "Edit");
    }

    private void setCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        String currentDate = android.text.format.DateFormat.format("EEEE, dd MMMM yyyy", calendar).toString();
        tvReportDateAdmin.setText(currentDate);
    }

    private void openImagePicker() {
        // Intent to allow image selection from both the Android device and computer (via file picker)
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Allow image files
        intent.addCategory(Intent.CATEGORY_OPENABLE); // Openable for local storage (computer files)
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            imageUriList.add(selectedImageUri);  // Add selected image URI to the list
            displayImage(selectedImageUri);  // Display the newly added image
            Toast.makeText(this, "Image added. Total images: " + imageUriList.size(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayImage(Uri imageUri) {
        if (imageUri == null) return;

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400)); // Set height to a fixed size or wrap content

        Glide.with(this)
                .load(imageUri)
                .into(imageView);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        params.setMargins(10, 10, 10, 10); // Optional margins around the image
        imageView.setLayoutParams(params);

        imagesContainer.addView(imageView);
    }

    private void fetchReportData() {
        DocumentReference docRef = db.collection("dailyReports").document(documentId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                etHomework.setText(documentSnapshot.getString("homework"));
                etMeals.setText(documentSnapshot.getString("meals"));
                etTuition.setText(documentSnapshot.getString("tuition"));
                etMood.setText(documentSnapshot.getString("mood"));
                etPerformance.setText(documentSnapshot.getString("performance"));

                imagesContainer.removeAllViews();
                for (int i = 0; ; i++) {
                    String imageUrlKey = "imageUrl_" + i;
                    if (documentSnapshot.contains(imageUrlKey)) {
                        String imageUrl = documentSnapshot.getString(imageUrlKey);
                        if (!TextUtils.isEmpty(imageUrl)) {
                            Uri imageUri = Uri.parse(imageUrl);
                            displayImage(imageUri);
                        }
                    } else {
                        break;
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch report data", Toast.LENGTH_SHORT).show();
            Log.e("AdminReportActivity", "Error fetching report data", e);
        });
    }

    private void saveOrUpdateReport() {
        String homework = etHomework.getText().toString().trim();
        String meals = etMeals.getText().toString().trim();
        String tuition = etTuition.getText().toString().trim();
        String mood = etMood.getText().toString().trim();
        String performance = etPerformance.getText().toString().trim();
        String date = tvReportDateAdmin.getText().toString();

        if (TextUtils.isEmpty(homework) || TextUtils.isEmpty(meals) || TextUtils.isEmpty(tuition)
                || TextUtils.isEmpty(mood) || TextUtils.isEmpty(performance) || imageUriList.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and add at least one image.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("homework", homework);
        reportData.put("meals", meals);
        reportData.put("tuition", tuition);
        reportData.put("mood", mood);
        reportData.put("performance", performance);
        reportData.put("date", date);
        reportData.put("childName", childName);

        for (int i = 0; i < imageUriList.size(); i++) {
            Uri imageUri = imageUriList.get(i);
            String imageName = "daily_reports/" + childName + "_" + date + "_image_" + i + ".jpg";
            StorageReference imageRef = storageRef.child(imageName);

            int finalI = i;
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        reportData.put("imageUrl_" + finalI, uri.toString());

                        if (finalI == imageUriList.size() - 1) {
                            DocumentReference docRef = db.collection("dailyReports").document(documentId);
                            docRef.set(reportData, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AdminReportActivity.this, "Report updated successfully!", Toast.LENGTH_SHORT).show();
                                        isEditing = false;
                                        toggleFields(false);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AdminReportActivity.this, "Failed to update report.", Toast.LENGTH_SHORT).show();
                                        Log.e("AdminReportActivity", "Error updating report", e);
                                    });
                        }
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("AdminReportActivity", "Image upload failed: ", e);
                    });
        }
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(AdminReportActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
