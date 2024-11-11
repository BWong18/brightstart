package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String parentEmail;
    private TextView tvStudentName, tvReportDate, tvHomework, tvMeals, tvTuition, tvMood, tvPerformance;
    private LinearLayout imagesContainer;
    private Spinner childSpinner;
    private String selectedChildName;
    private ImageView signoutButton, notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvStudentName = findViewById(R.id.tvStudentName);
        tvReportDate = findViewById(R.id.tvReportDate);
        tvHomework = findViewById(R.id.tvHomework);
        tvMeals = findViewById(R.id.tvMeals);
        tvTuition = findViewById(R.id.tvTuition);
        tvMood = findViewById(R.id.tvMood);
        tvPerformance = findViewById(R.id.tvPerformance);
        imagesContainer = findViewById(R.id.images_container);
        childSpinner = findViewById(R.id.child_spinner);

        // Fetch report data from Firestore
        fetchParentEmail();

        notification = findViewById(R.id.ivNotification);
        notification.setOnClickListener(v -> openActivity(NotificationMenuActivity.class));

        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());

        // Set up back button functionality
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up Spinner selection listener to load selected child’s report
        childSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedChildName = (String) parent.getItemAtPosition(position);
                fetchDailyReport(selectedChildName);  // Load the report for the selected child
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });

        // Initialize and set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_home) {
            Intent homeIntent = new Intent(ReportActivity.this, DashboardActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.navigation_chat) {
            startActivity(new Intent(ReportActivity.this, ChatActivity.class));
            return true;
        } else if (itemId == R.id.navigation_profile) {
            Intent profileIntent = new Intent(ReportActivity.this, NavigationProfile.class);
            startActivity(profileIntent);
            finish();
            return true;
        }
        return false;
    }

    private void fetchParentEmail() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            parentEmail = currentUser.getEmail();
            if (!TextUtils.isEmpty(parentEmail)) {
                Log.d("ReportActivity", "Logged-in parent's email: " + parentEmail);
                fetchParentNameUsingEmail(parentEmail);
            } else {
                Toast.makeText(this, "Parent email not found.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No logged-in user found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchParentNameUsingEmail(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(usersSnapshot -> {
                    if (!usersSnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = usersSnapshot.getDocuments().get(0);
                        String parentName = userDoc.getString("name");
                        if (!TextUtils.isEmpty(parentName)) {
                            Log.d("ReportActivity", "Fetched parent's name: " + parentName);
                            fetchChildrenList(parentName);
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
                    Log.e("ReportActivity", "Error fetching user profile", e);
                });
    }

    private void fetchChildrenList(String parentName) {
        db.collection("childrenProfiles")
                .whereEqualTo("parentName", parentName)
                .get()
                .addOnSuccessListener(childrenSnapshot -> {
                    List<String> childNames = new ArrayList<>();

                    for (DocumentSnapshot childDoc : childrenSnapshot.getDocuments()) {
                        String childName = childDoc.getString("childName");
                        if (childName != null) {
                            childNames.add(childName);
                        }
                    }

                    if (childNames.isEmpty()) {
                        Toast.makeText(this, "No children found for this parent.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    setupChildSpinner(childNames);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch children list.", Toast.LENGTH_SHORT).show());
    }

    private void setupChildSpinner(List<String> childNames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, childNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childSpinner.setAdapter(adapter);

        if (!childNames.isEmpty()) {
            selectedChildName = childNames.get(0);
            fetchDailyReport(selectedChildName);
        }
    }

    private void fetchDailyReport(String childName) {
        db.collection("dailyReports")
                .whereEqualTo("childName", childName)
                .get()
                .addOnSuccessListener(reportQuerySnapshot -> {
                    if (!reportQuerySnapshot.isEmpty()) {
                        DocumentSnapshot reportDocumentSnapshot = reportQuerySnapshot.getDocuments().get(0);

                        tvStudentName.setText(childName + "'s Report");
                        tvReportDate.setText(reportDocumentSnapshot.getString("date"));
                        tvHomework.setText(reportDocumentSnapshot.getString("homework"));
                        tvMeals.setText(reportDocumentSnapshot.getString("meals"));
                        tvTuition.setText(reportDocumentSnapshot.getString("tuition"));
                        tvMood.setText(reportDocumentSnapshot.getString("mood"));
                        tvPerformance.setText(reportDocumentSnapshot.getString("performance"));

                        imagesContainer.removeAllViews();
                        int imageCount = 0;
                        while (reportDocumentSnapshot.contains("imageUrl_" + imageCount)) {
                            String imageUrl = reportDocumentSnapshot.getString("imageUrl_" + imageCount);
                            if (!TextUtils.isEmpty(imageUrl)) {
                                ImageView imageView = new ImageView(this);
                                imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        400));

                                Glide.with(this)
                                        .load(imageUrl)
                                        .into(imageView);

                                imagesContainer.addView(imageView);
                            }
                            imageCount++;
                        }
                    } else {
                        Toast.makeText(this, "No report found for this student.", Toast.LENGTH_SHORT).show();
                        Log.d("ReportActivity", "No report found matching the criteria.");
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch report data", Toast.LENGTH_SHORT).show();
                    Log.e("ReportActivity", "Error fetching report data", e);
                });
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(ReportActivity.this, activityClass);
        startActivity(intent);
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(ReportActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
