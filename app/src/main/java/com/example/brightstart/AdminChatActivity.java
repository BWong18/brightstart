package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brightstart.adapter.ChatAdapter;
import com.example.brightstart.data.ChatMessage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

public class AdminChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private ImageView signoutButton;
    private EditText editTextMessage;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String currentUserEmail;
    private String selectedUserEmail;
    private final String adminEmail = "aa@gmail.com"; // Admin's email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Get selected user email from intent
        selectedUserEmail = getIntent().getStringExtra("userEmail");
        if (selectedUserEmail == null) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView ivBack = findViewById(R.id.back_button);
        ivBack.setOnClickListener(v -> {
            // Navigate back or finish the activity
            finish();
        });

        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());

        // Set the header title if needed
        TextView tvHeaderTitle = findViewById(R.id.tvChat);
        tvHeaderTitle.setText("Chat with User"); // Or dynamically set the user's name

        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);

        // Set the selected item
        bottomNavigationView.setSelectedItemId(R.id.admin_navigation_chat);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.admin_navigation_home) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            } else if (itemId == R.id.admin_navigation_chat) {
                // Already in AdminChatActivity
                return true;
            } else if (itemId == R.id.admin_navigation_profile) {
                startActivity(new Intent(this, NavigationProfile.class));
                return true;
            } else {
                return false;
            }
        });

        // Initialize Views
        recyclerViewMessages = findViewById(R.id.recyclerViewMessagesAdminChat);
        editTextMessage = findViewById(R.id.editTextMessageAdminChat);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, adminEmail);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(chatAdapter);

        // Get admin's email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.getEmail().equals(adminEmail)) {
            Toast.makeText(this, "Admin access only", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserEmail = currentUser.getEmail();

        // Listen for new messages between admin and selected user
        firestore.collection("chats").whereEqualTo("sender", selectedUserEmail).whereEqualTo("receiver", adminEmail).orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("AdminChatActivity", "Listen failed.", e);

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

        // Listen for admin's replies to the selected user
        firestore.collection("chats").whereEqualTo("sender", adminEmail).whereEqualTo("receiver", selectedUserEmail).orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("AdminChatActivity", "Listen failed.", e);

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

    public void sendMessageAdmin(View view) {
        String message = editTextMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            ChatMessage chatMessage = new ChatMessage(message, adminEmail, selectedUserEmail, System.currentTimeMillis());

            firestore.collection("chats").add(chatMessage).addOnSuccessListener(documentReference -> {
                Log.d("AdminChatActivity", "Message sent successfully");
                editTextMessage.setText("");
            }).addOnFailureListener(e -> {
                Log.e("AdminChatActivity", "Failed to send message", e);
                Toast.makeText(AdminChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Auth
        Intent intent = new Intent(AdminChatActivity.this, LoginActivity.class); // Navigate to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
