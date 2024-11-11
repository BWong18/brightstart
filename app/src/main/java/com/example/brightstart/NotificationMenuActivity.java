package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class NotificationMenuActivity extends AppCompatActivity {

    private ImageView signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_menu);

        ImageView backButton = findViewById(R.id.back_button);

        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());

        backButton.setOnClickListener(v -> finish());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    startActivity(new Intent(NotificationMenuActivity.this, DashboardActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_chat) {
                    startActivity(new Intent(NotificationMenuActivity.this, ChatActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    startActivity(new Intent(NotificationMenuActivity.this, NavigationProfile.class));
                    return true;
                }
                return false;
            }
        });
    }

    // Method for the back button in the header
    public void goBack(View view) {
        finish(); // This will close the current activity and return to the previous one
    }

    public void openBilling(View view) {
        startActivity(new Intent(this, BillingHistoryActivity.class));
    }

    public void openDailyReport(View view) {
        startActivity(new Intent(this, DailyReportActivity.class));
    }

    public void openEventCalendar(View view) {
        startActivity(new Intent(this, CalendarHistoryActivity.class));
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(NotificationMenuActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
