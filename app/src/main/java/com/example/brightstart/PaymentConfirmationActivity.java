package com.example.brightstart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PaymentConfirmationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PaymentAdapter paymentAdapter;
    private FirebaseFirestore db;
    private List<Student> studentList = new ArrayList<>();
    private List<Student> allStudents = new ArrayList<>();
    private ImageView backButton, signoutButton;
    private Spinner spinnerStandard;
    private EditText searchBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_confirmation);

        recyclerView = findViewById(R.id.recycler_view_students_payment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        spinnerStandard = findViewById(R.id.spinner_standard);
        backButton = findViewById(R.id.back_button);
        searchBar = findViewById(R.id.search_bar);

        backButton.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        setupStandardSpinner();
        setupSearchBar();

        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());

        fetchChildrenData();

        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.admin_navigation_home) {
            Intent homeIntent = new Intent(PaymentConfirmationActivity.this, AdminDashboardActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_chat) {
            Intent homeIntent = new Intent(PaymentConfirmationActivity.this, AdminActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_profile) {
            Intent profileIntent = new Intent(PaymentConfirmationActivity.this, AdminNavigationProfile.class);
            startActivity(profileIntent);
            finish();
            return true;
        }
        return false;
    }

    private void setupStandardSpinner() {
        String[] standards = {"All", "Standard 1", "Standard 2", "Standard 3", "Standard 4", "Standard 5", "Standard 6"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, standards);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStandard.setAdapter(spinnerAdapter);

        spinnerStandard.setSelection(0);

        spinnerStandard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, android.view.View selectedItemView, int position, long id) {
                String selectedStandard = (String) parentView.getItemAtPosition(position);
                filterByStandard(selectedStandard);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    private void fetchChildrenData() {
        db.collection("childrenProfiles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studentList.clear();
                        allStudents.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String childName = document.getString("childName");
                            String studentDocumentId = document.getId();

                            Integer age = null;
                            if (document.contains("age")) {
                                Object ageObject = document.get("age");
                                if (ageObject instanceof Number) {
                                    age = ((Number) ageObject).intValue();
                                } else if (ageObject instanceof String) {
                                    try {
                                        age = Integer.parseInt((String) ageObject);
                                    } catch (NumberFormatException e) {
                                        Log.e("Firestore", "Invalid age format for child: " + childName, e);
                                    }
                                }
                            }

                            if (age != null && age >= 7 && age <= 12) {
                                String standard = getStandardFromAge(age);
                                fetchPaymentData(studentDocumentId, childName, standard);
                            } else {
                                Log.w("Firestore", "Skipping child with invalid or missing age: " + childName);
                            }
                        }
                        filterByStandard("Standard 1");
                    }
                });
    }

    private void fetchPaymentData(String studentDocumentId, String childName, String standard) {
        db.collection("billingDetails")
                .document(studentDocumentId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String paymentStatus = document.getString("paymentStatus");
                        if (!"confirmPaid".equals(paymentStatus)) {
                            boolean alreadyExists = false;
                            for (Student student : studentList) {
                                if (student.getDocumentId().equals(studentDocumentId)) {
                                    alreadyExists = true;
                                    break;
                                }
                            }
                            if (!alreadyExists) {
                                boolean isPaid = paymentStatus != null && paymentStatus.equals("Paid");
                                Student student = new Student(childName, standard, isPaid, studentDocumentId);
                                studentList.add(student);
                                allStudents.add(student);

                                updateRecyclerView(studentList);
                            }
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchChildrenData();
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterBySearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void filterBySearch(String query) {
        List<Student> filteredStudents = new ArrayList<>();

        for (Student student : allStudents) {
            if (student.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredStudents.add(student);
            }
        }

        updateRecyclerView(filteredStudents);
    }

    private String getStandardFromAge(int age) {
        switch (age) {
            case 7: return "Standard 1";
            case 8: return "Standard 2";
            case 9: return "Standard 3";
            case 10: return "Standard 4";
            case 11: return "Standard 5";
            case 12: return "Standard 6";
            default: return "Unknown";
        }
    }

    private void filterByStandard(String selectedStandard) {
        List<Student> filteredStudents = new ArrayList<>();

        if (selectedStandard.equals("All")) {
            filteredStudents.addAll(allStudents);
        } else {
            for (Student student : allStudents) {
                if (student.getStandard().equals(selectedStandard)) {
                    filteredStudents.add(student);
                }
            }
        }

        updateRecyclerView(filteredStudents);
    }

    private void updateRecyclerView(List<Student> studentList) {
        if (paymentAdapter == null) {
            paymentAdapter = new PaymentAdapter(studentList, this);
            recyclerView.setAdapter(paymentAdapter);
        } else {
            paymentAdapter.updateList(studentList);
        }
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(PaymentConfirmationActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
