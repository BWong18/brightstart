package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brightstart.adapter.ChildAdapter;
import com.example.brightstart.data.ChildProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AttendanceActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private RecyclerView recyclerViewChildren;
    private ChildAdapter childAdapter;
    private List<ChildProfile> childList;
    private TextView dateTextView;
    private ImageView notification, signoutButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        // Initialize Firestore and RecyclerView
        firestore = FirebaseFirestore.getInstance();
        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the list and adapter
        childList = new ArrayList<>();
        childAdapter = new ChildAdapter(childList);
        recyclerViewChildren.setAdapter(childAdapter);

        // Set up date display
        dateTextView = findViewById(R.id.date_text); // Ensure this ID matches your layout file
        setDate();

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        notification = findViewById(R.id.ivNotification);
        notification.setOnClickListener(v -> openActivity(NotificationMenuActivity.class));

        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());


        // Convert the age field if necessary
        convertAgeFieldToInt();

        // Fetch and display the child's details
        fetchEmail();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(AttendanceActivity.this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.navigation_chat) {
                startActivity(new Intent(AttendanceActivity.this, ChatActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(AttendanceActivity.this, NavigationProfile.class));
                return true;
            }
            return false;
        });
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(AttendanceActivity.this, activityClass);
        startActivity(intent);
    }

    private void setDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());
        dateTextView.setText(currentDate); // Display the date in the TextView
    }

    private void fetchEmail() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("name");

                        if (email != null && !email.isEmpty()) {
                            fetchChildrenByEmail(email);
                        } else {
                            Toast.makeText(this, "Email is not available in Firestore.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to retrieve email.", Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", e.getMessage());
                });
    }

    private void fetchChildrenByEmail(String email) {
        firestore.collection("childrenProfiles")
                .whereEqualTo("parentName", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        childList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            ChildProfile childProfile = document.toObject(ChildProfile.class);

                            if (childProfile != null) {
                                childProfile.setId(document.getId());
                                childList.add(childProfile);
                            }
                        }
                        childAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(AttendanceActivity.this, "No child data found for this email.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch children data.", Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", e.getMessage());
                });
    }

    private void convertAgeFieldToInt() {
        firestore.collection("childrenProfiles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Object ageField = document.get("age");

                        // Check if the age field is a String and not empty
                        if (ageField instanceof String && !((String) ageField).isEmpty()) {
                            try {
                                int ageInt = Integer.parseInt((String) ageField);
                                // Update the age field in Firestore as an integer
                                document.getReference().update("age", ageInt)
                                        .addOnSuccessListener(aVoid -> Log.d("FirestoreUpdate", "Updated age for document ID: " + document.getId()))
                                        .addOnFailureListener(e -> Log.e("FirestoreUpdate", "Failed to update age for document ID: " + document.getId(), e));
                            } catch (NumberFormatException e) {
                                Log.e("FirestoreError", "Error parsing age for document ID: " + document.getId(), e);
                            }
                        }
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to fetch documents", e);
                });
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(AttendanceActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
