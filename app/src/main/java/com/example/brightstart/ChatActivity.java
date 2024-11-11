package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brightstart.adapter.ChatAdapter;
import com.example.brightstart.data.ChatMessage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String currentUserEmail;
    private final String receiverEmail = "aa@gmail.com"; // Receiver is always aa@gmail.com

    private ImageView ivBack, ivNotification, ivSignOut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Initialize Firebase explicitly
        setContentView(R.layout.activity_chat);

        // Initialize Views
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);

        // In onCreate()
        ImageView ivBack = findViewById(R.id.back_button);
        ivNotification = findViewById(R.id.ivNotification);
        ivSignOut = findViewById(R.id.ivSignOut);
        ivBack = findViewById(R.id.back_button);

        ivBack.setOnClickListener(v -> {
            // Navigate back or finish the activity
            finish();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(R.id.navigation_chat);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.navigation_chat) {
                // Already in ChatActivity
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, NavigationProfile.class));
                return true;
            } else {
                return false;
            }
        });

        ivNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(NotificationMenuActivity.class);
            }
        });

        ivSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSignOut();
            }
        });


        // Initialize FirebaseAuth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not authenticated, redirect to LoginActivity
            Toast.makeText(this, "Please login to use the chat", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Retrieve current user's email
        currentUserEmail = currentUser.getEmail();

        // Initialize chat messages list
        chatMessages = new ArrayList<>();

        // Initialize ChatAdapter with chatMessages and currentUserEmail
        chatAdapter = new ChatAdapter(chatMessages, currentUserEmail);

        // Set up RecyclerView
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(chatAdapter);

        // Set onClickListener for the send button
        findViewById(R.id.buttonSend).setOnClickListener(v -> sendMessage());

        // Listen for messages sent by the current user to aa@gmail.com
        firestore.collection("chats").whereEqualTo("sender", currentUserEmail).whereEqualTo("receiver", receiverEmail).orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("ChatActivity", "Listen failed.", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        ChatMessage chatMessage = dc.getDocument().toObject(ChatMessage.class);
                        chatMessages.add(chatMessage);
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerViewMessages.scrollToPosition(chatMessages.size() - 1);
                    }
                }
            }
        });

        // Listen for messages sent by aa@gmail.com to the current user
        firestore.collection("chats").whereEqualTo("sender", receiverEmail).whereEqualTo("receiver", currentUserEmail).orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("ChatActivity", "Listen failed.", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        ChatMessage chatMessage = dc.getDocument().toObject(ChatMessage.class);
                        chatMessages.add(chatMessage);
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerViewMessages.scrollToPosition(chatMessages.size() - 1);
                    }
                }
            }
        });
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(ChatActivity.this, activityClass);
        startActivity(intent);
    }

    private void handleSignOut() {
        mAuth.signOut(); // Sign out from Firebase
        Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close ChatActivity
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String senderEmail = currentUser.getEmail();
                String receiverEmail = "aa@gmail.com"; // Receiver is always aa@gmail.com

                ChatMessage chatMessage = new ChatMessage(message, senderEmail, receiverEmail, System.currentTimeMillis());

                firestore.collection("chats").add(chatMessage).addOnSuccessListener(documentReference -> {
                    Log.d("ChatActivity", "Message sent successfully");
                    editTextMessage.setText("");
                }).addOnFailureListener(e -> {
                    Log.e("ChatActivity", "Failed to send message", e);
                    Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }
}
