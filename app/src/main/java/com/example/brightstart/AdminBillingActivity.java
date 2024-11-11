package com.example.brightstart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminBillingActivity extends AppCompatActivity {

    private ImageView backButton, signoutButton;
    private TextView tvTuitionBilling, tvPaymentConfirmation;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_billing);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        tvTuitionBilling = findViewById(R.id.tvTuitionBilling);
        tvPaymentConfirmation = findViewById(R.id.tvPaymentConfirmation);
        signoutButton = findViewById(R.id.ivSignOut);

        // Set up Back Button functionality
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminBillingActivity.this, AdminDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Set up Notification Button functionality

        // Handle Tuition Billing click
        tvTuitionBilling.setOnClickListener(v -> {
            Intent intent = new Intent(AdminBillingActivity.this, TuitionBillingActivity.class);
            startActivity(intent);
        });

        // Handle Payment Confirmation click
        tvPaymentConfirmation.setOnClickListener(v -> {
            Intent intent = new Intent(AdminBillingActivity.this, PaymentConfirmationActivity.class);
            startActivity(intent);
        });

        // Set up Sign Out Button functionality
        signoutButton.setOnClickListener(v -> signOutUser());

        // Initialize and set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.admin_navigation_home) {
            // Navigate to AdminDashboardActivity
            Intent homeIntent = new Intent(AdminBillingActivity.this, AdminDashboardActivity.class);
            startActivity(homeIntent);
            finish();  // Close current activity
            return true;
        } else if (itemId == R.id.admin_navigation_chat) {
            Intent homeIntent = new Intent(AdminBillingActivity.this, AdminActivity.class);
            startActivity(homeIntent);
            finish();  // Close current activity
            return true;
        } else if (itemId == R.id.admin_navigation_profile) {
            // Navigate to AdminNavigationProfile activity
            Intent profileIntent = new Intent(AdminBillingActivity.this, AdminNavigationProfile.class);
            startActivity(profileIntent);
            finish();  // Close current activity
            return true;
        }
        return false;
    }

    // Method to sign out the user
    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(AdminBillingActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
