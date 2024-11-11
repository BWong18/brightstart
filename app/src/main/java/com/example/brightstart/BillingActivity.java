package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BillingActivity extends AppCompatActivity {

    private TextView tvTuitionFee, tvMealsFee, tvTransportationFee, tvResourceFee, tvTotal;
    private TextView tvStudentName, tvMonthYear, paymentMethodTitle, tvPaymentStatus;
    private RadioGroup paymentMethodGroup;
    private RadioButton rbCreditCard, rbOnlineBanking, rbEWallet;
    private Button btnPayNow;
    private Spinner childSpinner;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String parentEmail;
    private String documentId;
    private String selectedChildName;
    private String monthYear;
    private ImageView signoutButton, backButton, notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            parentEmail = currentUser.getEmail();
        } else {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvTuitionFee = findViewById(R.id.tv_tuition_fee);
        tvMealsFee = findViewById(R.id.tv_meals_fee);
        tvTransportationFee = findViewById(R.id.tv_transportation_fee);
        tvResourceFee = findViewById(R.id.tv_resource_fee);
        tvTotal = findViewById(R.id.tv_total);
        tvStudentName = findViewById(R.id.tv_student_name);
        tvMonthYear = findViewById(R.id.tv_month_year);
        tvPaymentStatus = findViewById(R.id.tv_payment_status);
        paymentMethodGroup = findViewById(R.id.payment_method_group);
        rbCreditCard = findViewById(R.id.rb_credit_card);
        rbOnlineBanking = findViewById(R.id.rb_online_banking);
        rbEWallet = findViewById(R.id.rb_e_wallet);
        btnPayNow = findViewById(R.id.btn_pay_now);
        paymentMethodTitle = findViewById(R.id.payment_method_title);
        childSpinner = findViewById(R.id.child_spinner);
        backButton = findViewById(R.id.back_button);
        notification = findViewById(R.id.ivNotification);

        notification.setOnClickListener(v -> openActivity(NotificationMenuActivity.class));

        // Back Button functionality
        backButton = findViewById(R.id.back_button);
        backButton.setVisibility(View.VISIBLE); // Make sure the button is visible
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up Pay Now button functionality
        btnPayNow.setOnClickListener(v -> {
            int selectedMethodId = paymentMethodGroup.getCheckedRadioButtonId();
            String paymentMethod = "";
            if (selectedMethodId == R.id.rb_credit_card) {
                paymentMethod = "Debit/ Credit Card";
            } else if (selectedMethodId == R.id.rb_online_banking) {
                paymentMethod = "Online Banking";
            } else if (selectedMethodId == R.id.rb_e_wallet) {
                paymentMethod = "E-Wallet";
            } else {
                Toast.makeText(this, "Please select a payment method.", Toast.LENGTH_SHORT).show();
                return;
            }
            processPayment(paymentMethod);
        });

        // Fetch children list and set up the Spinner
        fetchChildrenList();

        signoutButton = findViewById(R.id.ivSignOut);

        signoutButton.setOnClickListener(v -> signOutUser());


        // Initialize and set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(BillingActivity.this, activityClass);
        startActivity(intent);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_home) {
            // Navigate to DashboardActivity
            Intent homeIntent = new Intent(BillingActivity.this, DashboardActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.navigation_chat) {
            Intent homeIntent = new Intent(BillingActivity.this, ChatActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.navigation_profile) {
            // Navigate to NavigationProfile activity
            Intent profileIntent = new Intent(BillingActivity.this, NavigationProfile.class);
            startActivity(profileIntent);
            finish();
            return true;
        }
        return false;
    }

    private void fetchChildrenList() {
        db = FirebaseFirestore.getInstance();

        // Fetch the parent profile based on the parent's email
        db.collection("users")
                .whereEqualTo("email", parentEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot parentDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String parentName = parentDoc.getString("name");

                        // Query the childrenProfiles collection using the parent's name
                        db.collection("childrenProfiles")
                                .whereEqualTo("parentName", parentName)
                                .get()
                                .addOnSuccessListener(childSnapshots -> {
                                    List<String> childNames = new ArrayList<>();
                                    for (QueryDocumentSnapshot childDoc : childSnapshots) {
                                        String childName = childDoc.getString("childName");
                                        childNames.add(childName);
                                    }

                                    setupChildSpinner(childNames);
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch children list.", Toast.LENGTH_SHORT).show());
    }

    private void setupChildSpinner(List<String> childNames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, childNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childSpinner.setAdapter(adapter);

        childSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedChildName = childNames.get(position);
                fetchBillingDetailsByChildName(selectedChildName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    private void fetchBillingDetailsByChildName(String childName) {
        db.collection("billingDetails")
                .whereEqualTo("childName", childName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot billingDoc = queryDocumentSnapshots.getDocuments().get(0);
                        documentId = billingDoc.getId();

                        Double tuitionFee = billingDoc.getDouble("tuitionFees");
                        Double mealsFee = billingDoc.getDouble("meals");
                        Double transportationFee = billingDoc.getDouble("transportation");
                        Double resourceFee = billingDoc.getDouble("resourceFees");

                        tuitionFee = (tuitionFee != null) ? tuitionFee : 0.0;
                        mealsFee = (mealsFee != null) ? mealsFee : 0.0;
                        transportationFee = (transportationFee != null) ? transportationFee : 0.0;
                        resourceFee = (resourceFee != null) ? resourceFee : 0.0;

                        monthYear = billingDoc.getString("monthYear");
                        if (monthYear != null) {
                            tvMonthYear.setText(monthYear);
                        } else {
                            tvMonthYear.setText("Month/Year not available");
                        }

                        tvStudentName.setText(childName);
                        String paymentStatus = billingDoc.getString("paymentStatus");

                        // Reset UI before updating based on payment status
                        resetPaymentUI();

                        if ("confirmPaid".equals(paymentStatus)) {
                            showOnlyPaymentDoneMessage();
                        } else if ("Paid".equals(paymentStatus)) {
                            showPaymentSuccessWithFees(tuitionFee, mealsFee, transportationFee, resourceFee);
                        } else {
                            double totalFee = tuitionFee + mealsFee + transportationFee + resourceFee;
                            tvTuitionFee.setText(String.format("RM%.2f", tuitionFee));
                            tvMealsFee.setText(String.format("RM%.2f", mealsFee));
                            tvTransportationFee.setText(String.format("RM%.2f", transportationFee));
                            tvResourceFee.setText(String.format("RM%.2f", resourceFee));
                            tvTotal.setText(String.format("RM%.2f", totalFee));

                            paymentMethodTitle.setVisibility(View.VISIBLE);
                            paymentMethodGroup.setVisibility(View.VISIBLE);
                            btnPayNow.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this, "No billing details found for this child.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch billing details.", Toast.LENGTH_SHORT).show());
    }

    private void resetPaymentUI() {
        tvPaymentStatus.setVisibility(View.GONE);
        tvTuitionFee.setVisibility(View.VISIBLE);
        tvMealsFee.setVisibility(View.VISIBLE);
        tvTransportationFee.setVisibility(View.VISIBLE);
        tvResourceFee.setVisibility(View.VISIBLE);
        tvTotal.setVisibility(View.VISIBLE);
        paymentMethodTitle.setVisibility(View.VISIBLE);
        paymentMethodGroup.setVisibility(View.VISIBLE);
        btnPayNow.setVisibility(View.VISIBLE);
    }

    private void processPayment(String paymentMethod) {
        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(this, "No document ID available to process payment.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("billingDetails").document(documentId)
                .update("paymentStatus", "Paid")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BillingActivity.this, "Payment successful!", Toast.LENGTH_SHORT).show();
                    tvPaymentStatus.setText("Payment Done for " + monthYear);
                    showOnlyPaymentDoneMessage();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Payment failed.", Toast.LENGTH_SHORT).show());
    }

    private void showOnlyPaymentDoneMessage() {
        tvPaymentStatus.setText("Payment Done for " + monthYear);
        tvPaymentStatus.setVisibility(View.VISIBLE);
        tvTuitionFee.setVisibility(View.GONE);
        tvMealsFee.setVisibility(View.GONE);
        tvTransportationFee.setVisibility(View.GONE);
        tvResourceFee.setVisibility(View.GONE);
        tvTotal.setVisibility(View.GONE);
        paymentMethodTitle.setVisibility(View.GONE);
        paymentMethodGroup.setVisibility(View.GONE);
        btnPayNow.setVisibility(View.GONE);
    }

    private void showPaymentSuccessWithFees(Double tuitionFee, Double mealsFee, Double transportationFee, Double resourceFee) {
        tvPaymentStatus.setText("Payment Done for " + monthYear);
        tvPaymentStatus.setVisibility(View.VISIBLE);

        tvTuitionFee.setText(String.format("RM%.2f", tuitionFee));
        tvMealsFee.setText(String.format("RM%.2f", mealsFee));
        tvTransportationFee.setText(String.format("RM%.2f", transportationFee));
        tvResourceFee.setText(String.format("RM%.2f", resourceFee));

        double totalFee = tuitionFee + mealsFee + transportationFee + resourceFee;
        tvTotal.setText(String.format("RM%.2f", totalFee));

        paymentMethodTitle.setVisibility(View.GONE);
        paymentMethodGroup.setVisibility(View.GONE);
        btnPayNow.setVisibility(View.GONE);
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(BillingActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
