package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brightstart.adapter.DailyReportAdapter;
import com.example.brightstart.data.DailyReport;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DailyReportActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView dailyReportRecyclerView;
    private DailyReportAdapter dailyReportAdapter;
    private List<DailyReport> dailyReports;

    private String parentName;
    private List<String> childNames;
    private ImageView signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);

        db = FirebaseFirestore.getInstance();
        dailyReportRecyclerView = findViewById(R.id.dailyReportRecyclerView);

        dailyReports = new ArrayList<>();
        dailyReportAdapter = new DailyReportAdapter(dailyReports);
        dailyReportRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dailyReportRecyclerView.setAdapter(dailyReportAdapter);

        // Fetch the parent's name first
        fetchParentName();

        // Handle back button click
        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        signoutButton = findViewById(R.id.ivSignOut);

        signoutButton.setOnClickListener(v -> signOutUser());



        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(DailyReportActivity.this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.navigation_chat) {
                startActivity(new Intent(DailyReportActivity.this, ChatActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(DailyReportActivity.this, NavigationProfile.class));
                return true;
            }
            return false;
        });
    }

    private void fetchParentName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            parentName = documentSnapshot.getString("name");
                            if (parentName != null) {
                                fetchChildNamesByParent(parentName);
                            } else {
                                Toast.makeText(this, "Parent name not found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Parent profile not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch parent profile.", Toast.LENGTH_SHORT).show();
                        Log.e("DailyReportActivity", "Error fetching parent profile", e);
                    });
        } else {
            Toast.makeText(this, "Please sign in to view reports.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchChildNamesByParent(String parentName) {
        childNames = new ArrayList<>();

        db.collection("childrenProfiles")
                .whereEqualTo("parentName", parentName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot childDoc : queryDocumentSnapshots.getDocuments()) {
                            String childName = childDoc.getString("childName");
                            if (childName != null) {
                                childNames.add(childName);
                            }
                        }

                        if (!childNames.isEmpty()) {
                            fetchDailyReportsByChildNames(childNames);
                        } else {
                            Toast.makeText(this, "No child profiles found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No child profiles found for this parent.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch child profiles.", Toast.LENGTH_SHORT).show();
                    Log.e("DailyReportActivity", "Error fetching child profiles", e);
                });
    }

    private void fetchDailyReportsByChildNames(List<String> childNames) {
        if (childNames == null || childNames.isEmpty()) {
            Toast.makeText(this, "No children found for this parent.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("dailyReports")
                .whereIn("childName", childNames)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dailyReports.clear();
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String childName = document.getString("childName");
                                String date = document.getString("date");
                                String homework = document.getString("homework");
                                String imageUrl0 = document.getString("imageUrl_0");
                                String imageUrl1 = document.getString("imageUrl_1");
                                String meals = document.getString("meals");
                                String mood = document.getString("mood");
                                String performance = document.getString("performance");
                                String tuition = document.getString("tuition");

                                if (childName != null && date != null) {
                                    DailyReport dailyReport = new DailyReport(childName, date, homework, imageUrl0, imageUrl1, meals, mood, performance, tuition);
                                    dailyReports.add(dailyReport);
                                }
                            }
                            dailyReportAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No daily reports found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch daily reports.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(DailyReportActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
