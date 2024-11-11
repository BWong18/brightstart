package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

/** @noinspection ALL */
public class DashboardActivity extends AppCompatActivity {
    private ImageView signoutButton,notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        signoutButton = findViewById(R.id.ivSignOut);
        notification = findViewById(R.id.ivNotificationBell);

        signoutButton.setOnClickListener(v -> signOutUser());
        notification.setOnClickListener(v -> openActivity(NotificationMenuActivity.class));


        // Find views for the grid items
        findViewById(R.id.ivTimetable).setOnClickListener(v -> openActivity(TimetableActivity.class));
        findViewById(R.id.ivAttendance).setOnClickListener(v -> openActivity(AttendanceActivity.class));
        findViewById(R.id.ivKidsInfo).setOnClickListener(v -> openActivity(KidsInfoActivity.class));
        findViewById(R.id.ivBilling).setOnClickListener(v -> openActivity(BillingActivity.class));
        findViewById(R.id.ivCalendar).setOnClickListener(v -> openActivity(CalendarActivity.class));
        findViewById(R.id.ivReport).setOnClickListener(v -> openActivity(ReportActivity.class));

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                openActivity(DashboardActivity.class);
                return true;
            } else if (itemId == R.id.navigation_chat) {
                openActivity(ChatActivity.class);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                openActivity(NavigationProfile.class); // Open NavigationProfileActivity on profile click
                return true;
            }

            return false;
        });
    }

    // Method to open new activities
    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(DashboardActivity.this, activityClass);
        startActivity(intent);
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
