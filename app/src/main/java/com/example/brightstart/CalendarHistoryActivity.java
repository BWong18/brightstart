package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.brightstart.adapter.CalendarAdapter;
import com.example.brightstart.data.CalendarEvent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CalendarHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private List<CalendarEvent> calendarEvents;
    private ImageView signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_history);

        // Initialize Firestore and RecyclerView
        db = FirebaseFirestore.getInstance();
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);

        // Set up RecyclerView
        calendarEvents = new ArrayList<>();
        calendarAdapter = new CalendarAdapter(calendarEvents);
        calendarRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        calendarRecyclerView.setAdapter(calendarAdapter);

        // Fetch calendar events from Firestore
        fetchCalendarEvents();

        // Handle back button click
        findViewById(R.id.back_button).setOnClickListener(this::goBack);


        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(CalendarHistoryActivity.this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.navigation_chat) {
                startActivity(new Intent(CalendarHistoryActivity.this, ChatActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(CalendarHistoryActivity.this, NavigationProfile.class));
                return true;
            }
            return false;
        });
    }

    // Go back to the previous activity
    public void goBack(View view) {
        finish();
    }

    private void fetchCalendarEvents() {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        calendarEvents.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String date = document.getString("date");
                            String description = document.getString("description");

                            // Create CalendarEvent object and add it to the list
                            CalendarEvent event = new CalendarEvent(title, date, description);
                            calendarEvents.add(event);
                        }
                        calendarAdapter.notifyDataSetChanged();
                    } else {
                        // Optional: Handle error
                    }
                });
    }


    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(CalendarHistoryActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
