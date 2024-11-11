package com.example.brightstart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.brightstart.decorators.EventDecorator;
import com.example.brightstart.data.Event;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminCalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private Button buttonAddEvent;
    private FirebaseFirestore firestore;
    private CollectionReference eventsRef;
    private ListenerRegistration eventsListener;
    private List<Event> eventList = new ArrayList<>();
    private List<CalendarDay> eventDays = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private ImageView signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_calendar);

        // Uncomment the following line if you want to set padding for system bars manually
        // setPaddingForSystemBars();
        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());
        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        eventsRef = firestore.collection("events");

        // Initialize CalendarView
        calendarView = findViewById(R.id.calendarView);

        // Initialize Add Event Button
        buttonAddEvent = findViewById(R.id.buttonAddEvent);
        buttonAddEvent.setOnClickListener(v -> showAddEventDialog());

        ImageView ivBack = findViewById(R.id.back_button);
        TextView tvHeaderTitle = findViewById(R.id.ivAdminCalendar);
        tvHeaderTitle.setText("Admin Calendar"); // Set the title if needed

        ivBack.setOnClickListener(v -> finish());

        // Initialize Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.admin_navigation_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.admin_navigation_home) {
                Intent homeIntent = new Intent(AdminCalendarActivity.this, AdminDashboardActivity.class);
                startActivity(homeIntent);
                finish();  // Close current activity
                return true;
            } else if (itemId == R.id.admin_navigation_chat) {
                Intent homeIntent = new Intent(AdminCalendarActivity.this, AdminActivity.class);
                startActivity(homeIntent);
                finish();  // Close current activity
                return true;
            } else if (itemId == R.id.admin_navigation_profile) {
                Intent homeIntent = new Intent(AdminCalendarActivity.this, NavigationProfile.class);
                startActivity(homeIntent);
                finish();  // Close current activity
                return true;
            } else {
                return false;
            }
        });

        // Set Date Selected Listener
        calendarView.setOnDateChangedListener((widget, date, selected) -> showEventsForDate(date));

        // Fetch and display events
        fetchEvents();
    }

    /**
     * Fetches events from Firestore and updates the calendar.
     */
    private void fetchEvents() {
        if (eventsListener != null) {
            eventsListener.remove();
        }

        eventsListener = eventsRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w("AdminCalendarActivity", "Listen failed.", e);

                return;
            }

            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                Event event = dc.getDocument().toObject(Event.class);
                event.setId(dc.getDocument().getId());

                switch (dc.getType()) {
                    case ADDED:
                        eventList.add(event);
                        addEventDecorator(event);
                        break;
                    case MODIFIED:
                        updateEventDecorator(event);
                        break;
                    case REMOVED:
                        removeEventDecorator(event);
                        break;
                }
            }
        });
    }

    private void addEventDecorator(Event event) {
        try {
            Date eventDate = dateFormat.parse(event.getDate());
            CalendarDay day = CalendarDay.from(eventDate);
            eventDays.add(day);
            calendarView.addDecorator(new EventDecorator(eventDays, getResources().getColor(R.color.teal_700)));
        } catch (ParseException e) {
            Log.e("AdminCalendarActivity", "Invalid date format for event ID: " + event.getId(), e);
        }
    }

    private void updateEventDecorator(Event event) {
        removeEventDecorator(event);
        addEventDecorator(event);
    }

    private void removeEventDecorator(Event event) {
        try {
            Date eventDate = dateFormat.parse(event.getDate());
            CalendarDay day = CalendarDay.from(eventDate);
            eventDays.remove(day);
            calendarView.removeDecorators();
            calendarView.addDecorator(new EventDecorator(eventDays, getResources().getColor(R.color.teal_700)));
        } catch (ParseException e) {
            Log.e("AdminCalendarActivity", "Invalid date format for event ID: " + event.getId(), e);
        }
    }

    private void showAddEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Event");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        EditText editTextTitle = dialogView.findViewById(R.id.editTextTitle);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        EditText editTextDate = dialogView.findViewById(R.id.editTextDate);

        editTextDate.setInputType(InputType.TYPE_CLASS_DATETIME);

        editTextDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select Event Date").build();
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            datePicker.addOnPositiveButtonClickListener(selection -> editTextDate.setText(dateFormat.format(new Date(selection))));
        });

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String date = editTextDate.getText().toString().trim();

            if (title.isEmpty() || date.isEmpty()) {
                Toast.makeText(AdminCalendarActivity.this, "Title and Date are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Event newEvent = new Event();
            newEvent.setTitle(title);
            newEvent.setDescription(description);
            newEvent.setDate(date);

            eventsRef.add(newEvent)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(AdminCalendarActivity.this, "Event added", Toast.LENGTH_SHORT).show();
                        Log.d("AdminCalendarActivity", "Event added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AdminCalendarActivity.this, "Failed to add event", Toast.LENGTH_SHORT).show();
                        Log.e("AdminCalendarActivity", "Error adding event", e);
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEventsForDate(CalendarDay date) {
        List<Event> eventsForDate = new ArrayList<>();
        String selectedDateStr = dateFormat.format(date.getDate());

        for (Event event : eventList) {
            if (selectedDateStr.equals(event.getDate())) {
                eventsForDate.add(event);
            }
        }

        if (eventsForDate.isEmpty()) {
            Toast.makeText(this, "No events for this date", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder eventDetails = new StringBuilder();
        for (Event event : eventsForDate) {
            eventDetails.append("Title: ").append(event.getTitle()).append("\n")
                    .append("Description: ").append(event.getDescription()).append("\n\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Events on " + selectedDateStr)
                .setMessage(eventDetails.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventsListener != null) {
            eventsListener.remove();
        }
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(AdminCalendarActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
