package com.example.brightstart;

import android.app.AlertDialog;
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
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.brightstart.data.Event;
import com.example.brightstart.decorators.EventDecorator;
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

public class CalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private FirebaseFirestore firestore;
    private CollectionReference eventsRef;
    private ListenerRegistration eventsListener;
    private List<Event> eventList = new ArrayList<>();
    private List<CalendarDay> eventDays = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private ImageView signoutButton, notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view before accessing layout elements
        setContentView(R.layout.activity_calendar);

        signoutButton = findViewById(R.id.ivSignOut);
        notification = findViewById(R.id.ivNotification);

        signoutButton.setOnClickListener(v -> signOutUser());
        notification.setOnClickListener(v -> openActivity(NotificationMenuActivity.class));

        // Enable edge-to-edge mode without EdgeToEdge class
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Apply window insets padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        eventsRef = firestore.collection("events");

        // Initialize CalendarView
        calendarView = findViewById(R.id.calendarView);

        ImageView ivBack = findViewById(R.id.back_button);
        TextView tvHeaderTitle = findViewById(R.id.ivCalendar);
        tvHeaderTitle.setText("Calendar"); // Set the title if needed

        ivBack.setOnClickListener(v -> finish());

        // Initialize Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home); // Set the appropriate selected item

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.navigation_chat) {
                startActivity(new Intent(this, ChatActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, NavigationProfile.class));
                return true;
            } else {
                return false;
            }
        });

        // Set Date Selected Listener
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                showEventsForDate(date);
            }
        });

        // Fetch and display events
        fetchEvents();
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(CalendarActivity.this, activityClass);
        startActivity(intent);
    }

    private void fetchEvents() {
        if (eventsListener != null) {
            eventsListener.remove();
        }

        eventsListener = eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("CalendarActivity", "Listen failed.", e);
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
            Log.e("CalendarActivity", "Invalid date format for event ID: " + event.getId(), e);
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
            Log.e("CalendarActivity", "Invalid date format for event ID: " + event.getId(), e);
        }
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
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(CalendarActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
