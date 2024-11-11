package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvTogglePassword, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvTogglePassword = findViewById(R.id.tvTogglePassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Set up password visibility toggle
        setupPasswordToggle(R.id.etPassword, R.id.tvTogglePassword);

        // Link to SignUpActivity when "Don't have an account? Sign Up" text is clicked
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SignUpActivity
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Handle "Forgot Password?" text click
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ForgotPasswordActivity
                Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        // Handle login button click to authenticate and go to DashboardActivity
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Validate email and password
                if (email.isEmpty()) {
                    etEmail.setError("Email is required");
                    etEmail.requestFocus();
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Enter a valid email");
                    etEmail.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    etPassword.setError("Password is required");
                    etPassword.requestFocus();
                    return;
                }
                if (password.length() < 8) {
                    etPassword.setError("Password must be at least 8 characters");
                    etPassword.requestFocus();
                    return;
                }
                // Authenticate with Firebase
                loginUser(email, password);
            }
        });
    }

    // Method to toggle visibility of password field
    private void setupPasswordToggle(int passwordFieldId, int toggleTextViewId) {
        EditText passwordField = findViewById(passwordFieldId);
        TextView toggleTextView = findViewById(toggleTextViewId);

        if (passwordField != null && toggleTextView != null) {
            toggleTextView.setOnClickListener(v -> {
                if (passwordField.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    // Show password
                    passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    toggleTextView.setText("Hide");
                } else {
                    // Hide password
                    passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    toggleTextView.setText("Show");
                }

                // Move the cursor to the end of the text
                passwordField.setSelection(passwordField.getText().length());
            });
        }
    }

    // Method to log in the user with Firebase and check user type
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, get user type from Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            db.collection("users").document(user.getUid()).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String userType = documentSnapshot.getString("userType");
                                            if (userType != null) {
                                                if (userType.equals("Admin")) {
                                                    // Navigate to Admin Dashboard
                                                    Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    // Navigate to User Dashboard
                                                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this, "User type not found", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(LoginActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(LoginActivity.this, "Error retrieving user data", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // If sign-in fails, display a message to the user
                        Toast.makeText(LoginActivity.this, "Authentication failed. Invalid email or password.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
