package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

/** @noinspection ALL*/
public class AdminDashboardActivity extends AppCompatActivity {

    private ImageView signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        signoutButton = findViewById(R.id.ivSignOut);

        signoutButton.setOnClickListener(v -> signOutUser());




        // Find views for the grid items
        findViewById(R.id.ivAdminTimetable).setOnClickListener(v -> openActivity(AdminTimetableActivity.class));
        findViewById(R.id.ivAdminAttendance).setOnClickListener(v -> openActivity(AdminAttendanceActivity.class));
        findViewById(R.id.ivAdminStudentInfo).setOnClickListener(v -> openActivity(StudentInfoName.class));
        findViewById(R.id.ivAdminBilling).setOnClickListener(v -> openActivity(AdminBillingActivity.class));
        findViewById(R.id.ivAdminCalendar).setOnClickListener(v -> openActivity(AdminCalendarActivity.class));
        findViewById(R.id.ivAdminReport).setOnClickListener(v -> openActivity(StudentNameReport.class));

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.admin_navigation_home) {
                openActivity(AdminDashboardActivity.class);
                return true;
            } else if (itemId == R.id.admin_navigation_chat) {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminActivity.class);
                startActivity(intent);
                return true;
            }
            else if (itemId == R.id.admin_navigation_profile) {
                openActivity(AdminNavigationProfile.class);
                return true;
            }

            return false;
        });
    }

    // Method to open new activities
    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(AdminDashboardActivity.this, activityClass);
        startActivity(intent);
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}