package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminTimetableActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TimetableAdapter timetableAdapter;
    private TextView dateTextView;
    private ImageView backButton, signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_timetable);

        recyclerView = findViewById(R.id.timetable_recycler_view);
        dateTextView = findViewById(R.id.date_text);
        backButton = findViewById(R.id.back_button);

        // Set the current date at the top
        setDate();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<TimetableItem> timetableItems = getTimetableForToday();
        timetableAdapter = new TimetableAdapter(timetableItems, this);
        recyclerView.setAdapter(timetableAdapter);

        // Set up back button click listener to navigate back to AdminDashboardActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminTimetableActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        signoutButton = findViewById(R.id.ivSignOut);

        signoutButton.setOnClickListener(v -> signOutUser());

        // Initialize and set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.admin_navigation_home) {
            // Navigate to AdminDashboardActivity
            Intent homeIntent = new Intent(AdminTimetableActivity.this, AdminDashboardActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_chat) {
            Intent homeIntent = new Intent(AdminTimetableActivity.this, AdminActivity.class);
            startActivity(homeIntent);
            finish();
            return true;
        } else if (itemId == R.id.admin_navigation_profile) {
            // Navigate to AdminNavigationProfile activity
            Intent profileIntent = new Intent(AdminTimetableActivity.this, AdminNavigationProfile.class);
            startActivity(profileIntent);
            finish();
            return true;
        }
        return false;
    }

    private void setDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM");
        String currentDate = dateFormat.format(calendar.getTime());
        dateTextView.setText(currentDate);
    }

    private List<TimetableItem> getTimetableForToday() {
        List<TimetableItem> timetableItems = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                timetableItems.add(new TimetableItem("8:00 a.m. - 9:30 a.m.", "Homework Coaching Session"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 1 Tuition (Science)"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 2 Tuition (English)"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 3 Tuition (Bahasa Melayu)"));
                timetableItems.add(new TimetableItem("11:30 a.m. - 12:30 p.m.", "Lunch Time"));
                timetableItems.add(new TimetableItem("12:30 p.m. - 1:00 p.m.", "Get Ready To School (Standard 1,2 & 3)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 4 Tuition (Chinese)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 5 Tuition (English)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 6 Tuition (Maths)"));
                timetableItems.add(new TimetableItem("3:30 p.m. - 5:00 p.m.", "Homework Coaching Session"));
                break;

            case Calendar.TUESDAY:
                timetableItems.add(new TimetableItem("8:00 a.m. - 9:30 a.m.", "Homework Coaching Session"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 1 Tuition (Science)"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 2 Tuition (English)"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 3 Tuition (Bahasa Melayu)"));
                timetableItems.add(new TimetableItem("11:30 a.m. - 12:30 p.m.", "Lunch Time"));
                timetableItems.add(new TimetableItem("12:30 p.m. - 1:00 p.m.", "Get Ready To School (Standard 1,2 & 3)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 4 Tuition (Chinese)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 5 Tuition (English)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 6 Tuition (Maths)"));
                timetableItems.add(new TimetableItem("3:30 p.m. - 5:00 p.m.", "Homework Coaching Session"));
                break;
            case Calendar.WEDNESDAY:
                timetableItems.add(new TimetableItem("8:00 a.m. - 9:30 a.m.", "Homework Coaching Session"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 1 Tuition (Science)"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 2 Tuition (English)"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 3 Tuition (Bahasa Melayu)"));
                timetableItems.add(new TimetableItem("11:30 a.m. - 12:30 p.m.", "Lunch Time"));
                timetableItems.add(new TimetableItem("12:30 p.m. - 1:00 p.m.", "Get Ready To School (Standard 1,2 & 3)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 4 Tuition (Chinese)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 5 Tuition (English)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 6 Tuition (Maths)"));
                timetableItems.add(new TimetableItem("3:30 p.m. - 5:00 p.m.", "Homework Coaching Session"));
                break;
            case Calendar.THURSDAY:
                timetableItems.add(new TimetableItem("8:00 a.m. - 9:30 a.m.", "Homework Coaching Session"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 1 Tuition (Science)"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 2 Tuition (English)"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 3 Tuition (Bahasa Melayu)"));
                timetableItems.add(new TimetableItem("11:30 a.m. - 12:30 p.m.", "Lunch Time"));
                timetableItems.add(new TimetableItem("12:30 p.m. - 1:00 p.m.", "Get Ready To School (Standard 1,2 & 3)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 4 Tuition (Chinese)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 5 Tuition (English)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 6 Tuition (Maths)"));
                timetableItems.add(new TimetableItem("3:30 p.m. - 5:00 p.m.", "Homework Coaching Session"));
                break;
            case Calendar.FRIDAY:
                timetableItems.add(new TimetableItem("8:00 a.m. - 9:30 a.m.", "Homework Coaching Session"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 1 Tuition (Science)"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 2 Tuition (English)"));
                timetableItems.add(new TimetableItem("9:30 a.m. - 11:30 a.m.", "Standard 3 Tuition (Bahasa Melayu)"));
                timetableItems.add(new TimetableItem("11:30 a.m. - 12:30 p.m.", "Lunch Time"));
                timetableItems.add(new TimetableItem("12:30 p.m. - 1:00 p.m.", "Get Ready To School (Standard 1,2 & 3)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 4 Tuition (Chinese)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 5 Tuition (English)"));
                timetableItems.add(new TimetableItem("2:00 p.m. - 3:30 p.m.", "Standard 6 Tuition (Maths)"));
                timetableItems.add(new TimetableItem("3:30 p.m. - 5:00 p.m.", "Homework Coaching Session"));
                break;

            default:
                timetableItems.add(new TimetableItem("No classes", ""));
                break;
        }

        return timetableItems;
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(AdminTimetableActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
