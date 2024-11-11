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

import com.example.brightstart.adapter.BillingAdapter;
import com.example.brightstart.BillingDetail;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class BillingHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView billingRecyclerView;
    private BillingAdapter billingAdapter;
    private List<BillingDetail> billingDetails;
    private String userId;
    private ImageView signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing_history);

        db = FirebaseFirestore.getInstance();
        billingRecyclerView = findViewById(R.id.billingRecyclerView);

        billingDetails = new ArrayList<>();
        billingAdapter = new BillingAdapter(billingDetails);
        billingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        billingRecyclerView.setAdapter(billingAdapter);

        // Get the current user's ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch billing history for the current user
        fetchBillingHistory();

        // Handle back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    startActivity(new Intent(BillingHistoryActivity.this, DashboardActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_chat) {
                    startActivity(new Intent(BillingHistoryActivity.this, ChatActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    startActivity(new Intent(BillingHistoryActivity.this, NavigationProfile.class));
                    return true;
                }

                return false;
            }

        });
    }

    private void fetchBillingHistory() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch the parent profile based on the user's email
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(parentDocument -> {
                    if (parentDocument.exists()) {
                        String parentName = parentDocument.getString("name");

                        // Now fetch the childrenProfiles associated with this parent
                        db.collection("childrenProfiles")
                                .whereEqualTo("parentName", parentName)
                                .get()
                                .addOnSuccessListener(childSnapshots -> {
                                    if (!childSnapshots.isEmpty()) {
                                        List<String> childNames = new ArrayList<>();
                                        for (DocumentSnapshot childDoc : childSnapshots) {
                                            String childName = childDoc.getString("childName");
                                            if (childName != null) {
                                                childNames.add(childName);
                                            }
                                        }
                                        // Now fetch billing details for these children
                                        if (!childNames.isEmpty()) {
                                            fetchBillingDetails(childNames);
                                        } else {
                                            Toast.makeText(this, "No children found for this parent.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(this, "No child profiles found for this parent.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("BillingHistory", "Error fetching children profiles", e);
                                });
                    } else {
                        Toast.makeText(this, "Parent profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BillingHistory", "Error fetching parent profile", e);
                });
    }

    private void fetchBillingDetails(List<String> childNames) {
        db.collection("billingDetails")
                .whereIn("childName", childNames)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        billingDetails.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String childName = document.getString("childName");
                            String monthYear = document.getString("monthYear");
                            String paymentStatus = document.getString("paymentStatus");
                            long meals = document.getLong("meals") != null ? document.getLong("meals") : 0;
                            long resourceFees = document.getLong("resourceFees") != null ? document.getLong("resourceFees") : 0;
                            long transportation = document.getLong("transportation") != null ? document.getLong("transportation") : 0;
                            long tuitionFees = document.getLong("tuitionFees") != null ? document.getLong("tuitionFees") : 0;

                            // Create a BillingDetail object and add it to the list
                            BillingDetail billingDetail = new BillingDetail(childName, monthYear, paymentStatus, meals, resourceFees, transportation, tuitionFees);
                            billingDetails.add(billingDetail);
                        }
                        billingAdapter.notifyDataSetChanged();
                    } else {
                        Log.w("BillingHistory", "Error getting billing details", task.getException());
                        Toast.makeText(this, "Failed to fetch billing details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(BillingHistoryActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
