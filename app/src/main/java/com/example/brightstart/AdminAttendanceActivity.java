package com.example.brightstart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminAttendanceActivity extends AppCompatActivity {

    private static final String TAG = "AdminAttendanceActivity";

    private RecyclerView recyclerViewChildren;
    private ChildAdapter childAdapter;
    private List<ChildProfile> childList;
    private List<ChildProfile> allChildren;

    private FirebaseFirestore firestore;
    private Spinner spinnerStandard;
    private EditText searchBar;

    private ImageView signoutButton;
    private TextView tvAdminAttendanceTitle, dateTextView;
    private ImageView ivBack;

    private static final String LAST_UPDATE_DATE_KEY = "last_update_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_attendance);

        firestore = FirebaseFirestore.getInstance();

        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));

        childList = new ArrayList<>();
        allChildren = new ArrayList<>();
        childAdapter = new ChildAdapter(childList);
        recyclerViewChildren.setAdapter(childAdapter);

        tvAdminAttendanceTitle = findViewById(R.id.ivAdminAttendance);
        ivBack = findViewById(R.id.back_button);

        ivBack.setOnClickListener(v -> onBackPressed());

        spinnerStandard = findViewById(R.id.spinner_standard);
        setupStandardSpinner();

        searchBar = findViewById(R.id.search_bar);
        setupSearchBar();

        dateTextView = findViewById(R.id.date_text);
        setDate();

        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());

        // Check if attendance reset is needed on a new day
        checkAndResetAttendanceForNewDay();
        fetchChildren();

        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.admin_navigation_home) {
                openActivity(AdminDashboardActivity.class);
                return true;
            } else if (itemId == R.id.admin_navigation_chat) {
                openActivity(AdminActivity.class);
                return true;
            } else if (itemId == R.id.admin_navigation_profile) {
                openActivity(AdminNavigationProfile.class);
                return true;
            }

            return false;
        });
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(AdminAttendanceActivity.this, activityClass);
        startActivity(intent);
    }

    private void setupStandardSpinner() {
        String[] standards = {"All Standards", "Standard 1", "Standard 2", "Standard 3", "Standard 4", "Standard 5", "Standard 6"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, standards);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStandard.setAdapter(spinnerAdapter);

        spinnerStandard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void fetchChildren() {
        firestore.collection("childrenProfiles")
                .orderBy("age", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    allChildren.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            ChildProfile child = new ChildProfile();
                            child.setId(doc.getId());

                            Integer ageValue = doc.contains("age") ? doc.getLong("age").intValue() : null;
                            if (ageValue != null) {
                                child.setAge(ageValue);
                            } else {
                                Log.w(TAG, "Age field is missing or invalid for document ID: " + doc.getId());
                                continue;
                            }

                            String childName = doc.getString("childName");
                            if (childName != null) {
                                child.setChildName(childName);
                            } else {
                                Log.w(TAG, "Child name is missing for document ID: " + doc.getId());
                                continue;
                            }

                            child.setAttendanceStatus(doc.getString("attendanceStatus"));
                            allChildren.add(child);
                        } catch (Exception e) {
                            Log.e(TAG, "Unexpected error processing document ID: " + doc.getId(), e);
                        }
                    }
                    applyFilters(); // Filter and display list after fetching
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to load data", e);
                    Toast.makeText(AdminAttendanceActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
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

    private void applyFilters() {
        String selectedStandard = (String) spinnerStandard.getSelectedItem();
        String searchQuery = searchBar.getText().toString().trim();

        List<ChildProfile> filteredChildren = new ArrayList<>();

        for (ChildProfile child : allChildren) {
            String childStandard = getStandardFromAge(child.getAge());
            String childName = child.getChildName() != null ? child.getChildName() : "";

            boolean matchesStandard = selectedStandard.equals("All Standards") || selectedStandard.equals(childStandard);
            boolean matchesSearch = searchQuery.isEmpty() || childName.toLowerCase().contains(searchQuery.toLowerCase());

            if (matchesStandard && matchesSearch) {
                filteredChildren.add(child);
            }
        }

        if (filteredChildren.isEmpty()) {
            Log.w(TAG, "No children found after applying filters");
        }

        childList.clear();
        childList.addAll(filteredChildren);
        childAdapter.notifyDataSetChanged();
    }

    private void setDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());
        dateTextView.setText(currentDate);
    }

    private void checkAndResetAttendanceForNewDay() {
        SharedPreferences preferences = getSharedPreferences("admin_attendance_prefs", MODE_PRIVATE);
        String lastUpdateDate = preferences.getString(LAST_UPDATE_DATE_KEY, "");

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        if (!currentDate.equals(lastUpdateDate)) {
            resetAllAttendance();

            preferences.edit().putString(LAST_UPDATE_DATE_KEY, currentDate).apply();
        }
    }

    private void resetAllAttendance() {
        firestore.collection("childrenProfiles").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().update("attendanceStatus", "Absent")
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Attendance reset for child ID: " + doc.getId()))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to reset attendance for child ID: " + doc.getId(), e));
                    }
                    Toast.makeText(this, "Attendance reset for a new day.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to reset attendance.", e));
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(AdminAttendanceActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
