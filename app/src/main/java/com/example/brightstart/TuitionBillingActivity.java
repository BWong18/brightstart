package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TuitionBillingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchBar;
    private Spinner spinnerStandard;
    private FirebaseFirestore db;
    private ImageView signoutButton;
    private List<Student> allStudents = new ArrayList<>(); // To hold all students for search and filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuition_billing);

        recyclerView = findViewById(R.id.recycler_view_students);
        searchBar = findViewById(R.id.search_bar);
        spinnerStandard = findViewById(R.id.spinner_standard);

        // Back button for navigation
        ImageView backButton = findViewById(R.id.back_button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch data from Firestore
        fetchChildrenData();

        // Set up Spinner for standards
        setupStandardSpinner();

        // Set up search functionality
        setupSearchBar();

        signoutButton = findViewById(R.id.ivSignOut);

        signoutButton.setOnClickListener(v -> signOutUser());


        // Set up Back Button functionality
        backButton.setOnClickListener(v -> {
            // Navigate back to Admin Billing Page
            Intent intent = new Intent(TuitionBillingActivity.this, AdminBillingActivity.class);
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
            Intent homeIntent = new Intent(TuitionBillingActivity.this, AdminDashboardActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_chat) {
            Intent homeIntent = new Intent(TuitionBillingActivity.this, AdminActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_profile) {
            // Navigate to AdminNavigationProfile activity
            Intent profileIntent = new Intent(TuitionBillingActivity.this, AdminNavigationProfile.class);
            startActivity(profileIntent);
            finish();
            return true;
        }
        return false;
    }

    private void setupStandardSpinner() {
        String[] standards = {"Standard 1", "Standard 2", "Standard 3", "Standard 4", "Standard 5", "Standard 6"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, standards);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStandard.setAdapter(spinnerAdapter);

        spinnerStandard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedStandard = (String) parentView.getItemAtPosition(position);
                filterByStandard(selectedStandard);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });
    }

    private void fetchChildrenData() {
        db.collection("childrenProfiles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Student> studentList = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            String childName = document.getString("childName");
                            String documentId = document.getId();

                            Object ageObj = document.get("age");
                            int age = -1;
                            if (ageObj instanceof Long) {
                                age = ((Long) ageObj).intValue();
                            } else if (ageObj instanceof Integer) {
                                age = (Integer) ageObj;
                            } else if (ageObj instanceof String) {
                                try {
                                    age = Integer.parseInt((String) ageObj);
                                } catch (NumberFormatException e) {
                                    Log.w("Firestore", "Invalid age format for document: " + document.getId());
                                }
                            }

                            if (age >= 7 && age <= 12) {
                                String standard = getStandardFromAge(age);
                                studentList.add(new Student(childName, standard, false, documentId));
                            }
                        }

                        allStudents = studentList;
                        filterByStandard("Standard 1");
                    } else {
                        Log.w("Firestore", "Error getting childrenProfiles", task.getException());
                    }
                });
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

    private void updateRecyclerView(List<Student> studentList) {
        StudentAdapter studentAdapter = new StudentAdapter(studentList, this);
        recyclerView.setAdapter(studentAdapter);
    }

    private void filterByStandard(String standard) {
        List<Student> filteredStudents = new ArrayList<>();

        for (Student student : allStudents) {
            if (student.getStandard().equals(standard)) {
                filteredStudents.add(student);
            }
        }

        updateRecyclerView(filteredStudents);
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterBySearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) { }
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

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(TuitionBillingActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
