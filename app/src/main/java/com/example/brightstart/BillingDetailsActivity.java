package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BillingDetailsActivity extends AppCompatActivity {

    private TextView tvStudentName;
    private EditText etMonthYear, etTuitionFees, etMeals, etTransportation, etResourceFees;
    private Button btnSave;
    private ImageView btnEdit, btnBack, signoutButton;
    private FirebaseFirestore db;
    private String documentId; // Document ID instead of childName
    private boolean isEditing = false; // Flag to track if fields are in editing mode
    private DocumentReference documentRef; // Reference to the Firestore document for this child

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing_details);

        // Initialize views
        tvStudentName = findViewById(R.id.tvStudentName);
        etMonthYear = findViewById(R.id.etMonthYear);
        etTuitionFees = findViewById(R.id.etTuitionFees);
        etMeals = findViewById(R.id.etMeals);
        etTransportation = findViewById(R.id.etTransportation);
        etResourceFees = findViewById(R.id.etResourceFees);
        btnSave = findViewById(R.id.btnSave);
        btnEdit = findViewById(R.id.btnEdit);
        btnBack = findViewById(R.id.back_button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve documentId from Intent (passed from previous activity)
        documentId = getIntent().getStringExtra("documentId");
        String childName = getIntent().getStringExtra("childName");
        tvStudentName.setText(childName);

        // Get reference to the document for the specific student using the document ID
        documentRef = db.collection("billingDetails").document(documentId);

        // Initially, **DISABLE** EditText fields
        enableFields(false);

        // Load existing billing data (if it exists)
        loadBillingDetails();

        // Edit button functionality: toggle between editing and viewing mode
        btnEdit.setOnClickListener(v -> toggleEditingMode());

        // Save button to save the data to Firestore
        btnSave.setOnClickListener(v -> saveBillingDetails(childName));

        signoutButton = findViewById(R.id.ivSignOut);

        signoutButton.setOnClickListener(v -> signOutUser());


        // Back button functionality to go back to TuitionBillingActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(BillingDetailsActivity.this, TuitionBillingActivity.class);
            startActivity(intent);
            finish();
        });

        // Initialize and set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.admin_navigation_home) {
            // Navigate to AdminDashboardActivity
            Intent homeIntent = new Intent(BillingDetailsActivity.this, AdminDashboardActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_chat) {
            Intent homeIntent = new Intent(BillingDetailsActivity.this, AdminActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_profile) {
            // Navigate to AdminNavigationProfile activity
            Intent profileIntent = new Intent(BillingDetailsActivity.this, AdminNavigationProfile.class);
            startActivity(profileIntent);
            finish();
            return true;
        }
        return false;
    }

    // Method to enable or disable input fields
    private void enableFields(boolean enabled) {
        etMonthYear.setEnabled(enabled);
        etTuitionFees.setEnabled(enabled);
        etMeals.setEnabled(enabled);
        etTransportation.setEnabled(enabled);
        etResourceFees.setEnabled(enabled);
    }

    // Method to load existing billing details from Firestore
    private void loadBillingDetails() {
        documentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                etMonthYear.setText(documentSnapshot.getString("monthYear"));
                etTuitionFees.setText(String.valueOf(documentSnapshot.getDouble("tuitionFees")));
                etMeals.setText(String.valueOf(documentSnapshot.getDouble("meals")));
                etTransportation.setText(String.valueOf(documentSnapshot.getDouble("transportation")));
                etResourceFees.setText(String.valueOf(documentSnapshot.getDouble("resourceFees")));
            } else {
                Toast.makeText(this, "No billing details found, you can add them.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load billing details.", Toast.LENGTH_SHORT).show();
        });
    }

    // Method to save billing details to Firestore
    private void saveBillingDetails(String childName) {
        String monthYear = etMonthYear.getText().toString().trim();
        String tuitionFees = etTuitionFees.getText().toString().trim();
        String meals = etMeals.getText().toString().trim();
        String transportation = etTransportation.getText().toString().trim();
        String resourceFees = etResourceFees.getText().toString().trim();

        if (monthYear.isEmpty() || tuitionFees.isEmpty() || meals.isEmpty() || transportation.isEmpty() || resourceFees.isEmpty()) {
            Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> feesData = new HashMap<>();
        feesData.put("childName", childName);
        feesData.put("monthYear", monthYear);
        feesData.put("tuitionFees", Double.parseDouble(tuitionFees));
        feesData.put("meals", Double.parseDouble(meals));
        feesData.put("transportation", Double.parseDouble(transportation));
        feesData.put("resourceFees", Double.parseDouble(resourceFees));
        feesData.put("paymentStatus", "Pending");
        documentRef.set(feesData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BillingDetailsActivity.this, "Billing details saved", Toast.LENGTH_SHORT).show();
                    enableFields(false);
                    isEditing = false;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BillingDetailsActivity.this, "Failed to save billing details", Toast.LENGTH_SHORT).show();
                });
    }

    // Toggle between editing and non-editing mode
    // Toggle between editing and non-editing mode
    private void toggleEditingMode() {
        if (isEditing) {
            enableFields(false);
            isEditing = false;
            btnEdit.setImageResource(R.drawable.ic_edit); // Set to edit icon
        } else {
            enableFields(true);
            isEditing = true;
            btnEdit.setImageResource(R.drawable.ic_edit_cancel); // Set to cancel icon
        }
    }
    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(BillingDetailsActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }

}
