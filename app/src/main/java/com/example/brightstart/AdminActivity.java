package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brightstart.adapter.UserAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdminActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<String> userList;
    private Map<String, Boolean> unreadMessageStatus; // Track unread messages per user
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private final String adminEmail = "aa@gmail.com";
    private ImageView signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        unreadMessageStatus = new HashMap<>();

        userAdapter = new UserAdapter(userList, unreadMessageStatus, this);
        recyclerViewUsers.setAdapter(userAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.AdminbottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.admin_navigation_chat);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.admin_navigation_home) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            } else if (itemId == R.id.admin_navigation_chat) {
                return true;
            } else if (itemId == R.id.admin_navigation_profile) {
                startActivity(new Intent(this, NavigationProfile.class));
                return true;
            }
            return false;
        });

        ImageView ivBack = findViewById(R.id.back_button);
        ivBack.setOnClickListener(v -> finish());

        signoutButton = findViewById(R.id.ivSignOut);
        signoutButton.setOnClickListener(v -> signOutUser());

        loadUserMessages();
    }

    private void loadUserMessages() {
        // Fetch all messages sent to the admin, including read status
        firestore.collection("chats")
                .whereEqualTo("receiver", adminEmail)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("AdminActivity", "Listen failed.", e);
                            return;
                        }

                        Set<String> users = new HashSet<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String sender = doc.getString("sender");
                            Boolean isRead = doc.getBoolean("read");

                            // Set unread status to false if the "read" field does not exist
                            if (isRead == null) {
                                doc.getReference().update("read", false);
                                isRead = false;
                            }

                            if (sender != null && !sender.equals(adminEmail)) {
                                users.add(sender);
                                unreadMessageStatus.put(sender, !isRead); // Only show green dot if message is unread
                            }
                        }

                        userList.clear();
                        userList.addAll(users);
                        userAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onUserClick(String userEmail) {
        Intent intent = new Intent(AdminActivity.this, AdminChatActivity.class);
        intent.putExtra("userEmail", userEmail);
        startActivity(intent);

        // Mark all unread messages from this user as read
        firestore.collection("chats")
                .whereEqualTo("sender", userEmail)
                .whereEqualTo("receiver", adminEmail)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().update("read", true); // Update to mark as read in Firestore
                    }
                    unreadMessageStatus.put(userEmail, false); // Update local status as well
                    userAdapter.notifyDataSetChanged();
                });
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
