package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        // Set up password visibility toggles
        setupPasswordToggle(R.id.etPassword, R.id.tvTogglePassword);  // For password
        setupPasswordToggle(R.id.etConfirmPassword, R.id.tvToggleConfirmPassword);  // For confirm password

        // Set up sign-up button click
        btnSignUp.setOnClickListener(v -> signUpUser());
        // "Log in" TextView for navigation
        TextView tvLogin = findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
    // Method to toggle visibility of password field
    private void setupPasswordToggle(int passwordFieldId, int toggleTextViewId) {
        EditText passwordField = findViewById(passwordFieldId);
        TextView toggleTextView = findViewById(toggleTextViewId);

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
            // Move cursor to the end of the text
            passwordField.setSelection(passwordField.getText().length());
        });
    }

    // Method to handle sign-up process and validations
    private void signUpUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (!isValidEmail(email)) {
            etEmail.setError("Invalid email format");
            etEmail.requestFocus();
            return;
        }
        if (!isValidPhoneNumber(phone)) {
            etPhone.setError("Invalid phone number");
            etPhone.requestFocus();
            return;
        }
        if (password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters long");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // If all inputs are valid, proceed with Firebase authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success, save user details in Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserDataToFirestore(user.getUid(), name, email, phone, "User");  // Add userType as "User"
                    } else {
                        // If sign-up fails, display a message to the user.
                        Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to save user data to Firestore, including userType as "User"
    private void saveUserDataToFirestore(String uid, String name, String email, String phone, String userType) {
        // Create a new user object
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("userType", userType);  // Automatically set userType to "User"

        // Add a new document with the user's UID
        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // Successfully added to Firestore
                    Toast.makeText(SignUpActivity.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();

                    // Navigate to ParentsProfileActivity after successful sign-up
                    Intent intent = new Intent(SignUpActivity.this, ParentsProfileActivity.class);
                    startActivity(intent);
                    finish(); // Close the SignUpActivity
                })
                .addOnFailureListener(e -> {
                    // Failed to add user to Firestore
                    Toast.makeText(SignUpActivity.this, "Error adding user to Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Helper method to validate phone number (checking if it's numeric and has a valid length)
    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("\\d{8,}"); // Ensure phone has only digits and is at least 8 digits long
    }
}
