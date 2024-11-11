package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminConfirmPaymentActivity extends AppCompatActivity {

    private TextView tvStudentName, tvMonthYear, tvTuitionFee, tvMealsFee, tvTransportationFee, tvResourceFee, tvTotalFee, tvPaymentStatus;
    private Button btnConfirmPaid;
    private FirebaseFirestore db;
    private String studentDocumentId;  // This will store the auto-generated document ID
    private ImageView backButton, signoutButton;      // Add a back button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_confirm_payment);

        // Get the document ID and student name passed from PaymentConfirmationActivity
        studentDocumentId = getIntent().getStringExtra("studentDocumentId");
        String studentName = getIntent().getStringExtra("studentName");

        // Initialize views
        tvStudentName = findViewById(R.id.tv_student_name);
        tvMonthYear = findViewById(R.id.tv_month_year);
        tvTuitionFee = findViewById(R.id.tv_tuition_fee);
        tvMealsFee = findViewById(R.id.tv_meals_fee);
        tvTransportationFee = findViewById(R.id.tv_transportation_fee);
        tvResourceFee = findViewById(R.id.tv_resource_fee);
        tvTotalFee = findViewById(R.id.tv_total_fee);
        tvPaymentStatus = findViewById(R.id.tv_payment_status);
        btnConfirmPaid = findViewById(R.id.btn_confirm_paid);
        backButton = findViewById(R.id.back_button);
        signoutButton = findViewById(R.id.ivSignOut);// Initialize the back button

        // Set the student name (passed from previous activity)
        tvStudentName.setText(studentName);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch billing details and payment status from Firestore
        fetchBillingDetails();
        fetchPaymentStatus();

        // Confirm payment when the admin clicks the button
        btnConfirmPaid.setOnClickListener(v -> confirmPayment());

        // Handle the back button to go back to the PaymentConfirmationActivity
        backButton.setOnClickListener(v -> onBackPressed());

        signoutButton.setOnClickListener(v -> signOutUser());

        // Initialize and set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.admin_navigation_home) {
            // Navigate to AdminDashboardActivity
            Intent homeIntent = new Intent(AdminConfirmPaymentActivity.this, AdminDashboardActivity.class);
            startActivity(homeIntent);
            finish();  // Close current activity
            return true;
        } else if (item.getItemId() == R.id.admin_navigation_chat) {
            Intent homeIntent = new Intent(AdminConfirmPaymentActivity.this, AdminActivity.class);
            startActivity(homeIntent);
            finish();  // Close current activity
            return true;
        } else if (item.getItemId() == R.id.admin_navigation_profile) {
            // Navigate to AdminNavigationProfile activity
            Intent profileIntent = new Intent(AdminConfirmPaymentActivity.this, AdminNavigationProfile.class);
            startActivity(profileIntent);
            finish();  // Close current activity
            return true;
        }
        return false;
    }

    private void fetchBillingDetails() {
        db.collection("billingDetails").document(studentDocumentId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String monthYear = documentSnapshot.getString("monthYear");
                double tuitionFee = documentSnapshot.getDouble("tuitionFees");
                double mealsFee = documentSnapshot.getDouble("meals");
                double transportationFee = documentSnapshot.getDouble("transportation");
                double resourceFee = documentSnapshot.getDouble("resourceFees");

                if (monthYear != null) {
                    tvMonthYear.setText(monthYear);
                }
                tvTuitionFee.setText(String.format("%.2f", tuitionFee));
                tvMealsFee.setText(String.format("%.2f", mealsFee));
                tvTransportationFee.setText(String.format("%.2f", transportationFee));
                tvResourceFee.setText(String.format("%.2f", resourceFee));

                double total = tuitionFee + mealsFee + transportationFee + resourceFee;
                tvTotalFee.setText(String.format("RM%.2f", total));
            } else {
                Toast.makeText(this, "No billing details found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch billing details.", Toast.LENGTH_SHORT).show());
    }

    private void fetchPaymentStatus() {
        db.collection("billingDetails").document(studentDocumentId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String paymentStatus = documentSnapshot.getString("paymentStatus");
                if (paymentStatus != null) {
                    tvPaymentStatus.setText(paymentStatus.equals("Paid") ? "Payment Done" : "Payment Pending");
                }
            } else {
                Toast.makeText(this, "No payment status found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch payment status.", Toast.LENGTH_SHORT).show());
    }

    private void confirmPayment() {
        db.collection("billingDetails").document(studentDocumentId).update("paymentStatus", "confirmPaid").addOnSuccessListener(aVoid -> {
            db.collection("billingDetails").document(studentDocumentId).update(
                    "tuitionFees", 0,
                    "meals", 0,
                    "transportation", 0,
                    "resourceFees", 0
            ).addOnSuccessListener(aVoid1 -> {
                Toast.makeText(AdminConfirmPaymentActivity.this, "Payment confirmed and billing reset.", Toast.LENGTH_SHORT).show();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("paymentConfirmed", true);
                resultIntent.putExtra("studentDocumentId", studentDocumentId);
                setResult(RESULT_OK, resultIntent);

                finish();
            }).addOnFailureListener(e -> Toast.makeText(AdminConfirmPaymentActivity.this, "Failed to reset billing.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(AdminConfirmPaymentActivity.this, "Failed to confirm payment.", Toast.LENGTH_SHORT).show());
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(AdminConfirmPaymentActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
